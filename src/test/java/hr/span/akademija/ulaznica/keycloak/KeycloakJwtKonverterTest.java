package hr.span.akademija.ulaznica.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.platforma.KeycloakJwtKonverter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class KeycloakJwtKonverterTest {

    @Test
    void preferredUsernameIRealmUloge() {
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "RS256")
                .subject("uuid").claim("preferred_username", "organizator")
                .claim("realm_access", Map.of("roles", List.of("ORGANIZATOR", "offline_access")))
                .build();
        var auth = new KeycloakJwtKonverter().convert(jwt);
        assertThat(auth.getName()).isEqualTo("organizator");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_ORGANIZATOR");
    }

    @Test
    void bezRealmUlogaNemaOvlasti() {
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "RS256").subject("uuid").build();
        var auth = new KeycloakJwtKonverter().convert(jwt);
        assertThat(auth.getName()).isEqualTo("uuid");
        assertThat(auth.getAuthorities()).isEmpty();
    }
}
