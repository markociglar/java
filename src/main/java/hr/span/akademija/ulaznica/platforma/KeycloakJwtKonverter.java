package hr.span.akademija.ulaznica.platforma;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/** Maps Keycloak realm roles to Spring Security authorities. */
public class KeycloakJwtKonverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String ime = jwt.getClaimAsString("preferred_username");
        return new JwtAuthenticationToken(jwt, uloge(jwt), ime != null ? ime : jwt.getSubject());
    }

    static Collection<GrantedAuthority> uloge(Jwt jwt) {
        Map<String, Object> pristup = jwt.getClaimAsMap("realm_access");
        if (pristup == null || !(pristup.get("roles") instanceof Collection<?> uloge)) {
            return List.of();
        }
        return uloge.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(u -> List.of("KORISNIK", "ORGANIZATOR", "ADMIN").contains(u))
                .map(u -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + u))
                .toList();
    }
}
