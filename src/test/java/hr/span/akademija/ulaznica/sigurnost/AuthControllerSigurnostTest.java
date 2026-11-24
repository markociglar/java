package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import hr.span.akademija.ulaznica.keycloak.TestniKeycloak;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("sigurnost")
class AuthControllerSigurnostTest extends ApiTestBaza {
    @Test
    @DisplayName("JWT iz pogrešnog realma nije prihvaćen")
    void pogresanIssuer() {
        String jwt = TestniKeycloak.token("ana", "http://localhost:8180/realms/drugi",
                TestniKeycloak.AUDIENCE, Instant.now().plusSeconds(300));
        assertThat(get("/api/ja", jwt).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("JWT za drugu aplikaciju nije prihvaćen")
    void pogresanAudience() {
        String jwt = TestniKeycloak.token("ana", TestniKeycloak.ISSUER,
                "drugi-api", Instant.now().plusSeconds(300));
        assertThat(get("/api/ja", jwt).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("Istekli JWT nije prihvaćen")
    void istekliToken() {
        String jwt = TestniKeycloak.token("ana", TestniKeycloak.ISSUER,
                TestniKeycloak.AUDIENCE, Instant.now().minusSeconds(120));
        assertThat(get("/api/ja", jwt).status()).isEqualTo(401);
    }

    @Test
    @DisplayName("Promjena JWT payloada bez novog potpisa nije prihvaćena")
    void krivotvoreniPotpis() {
        String jwt = token("ana");
        String[] dijelovi = jwt.split("\\.");
        char zamjena = dijelovi[1].charAt(0) == 'A' ? 'B' : 'A';
        dijelovi[1] = zamjena + dijelovi[1].substring(1);
        assertThat(get("/api/ja", String.join(".", dijelovi)).status()).isEqualTo(401);
    }
}
