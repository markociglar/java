package hr.span.akademija.ulaznica.keycloak;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

/** Offline Keycloak-shaped JWTs with real RS256 signature verification. */
@TestConfiguration
public class TestniKeycloak {
    public static final String ISSUER = "http://localhost:8180/realms/ulaznice";
    public static final String AUDIENCE = "ulaznice-api";
    private static final KeyPair KLJUCEVI = kljucevi();

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) KLJUCEVI.getPublic()).build();
        OAuth2TokenValidator<Jwt> audience = jwt -> jwt.getAudience().contains(AUDIENCE)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Wrong audience", null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(ISSUER), audience));
        return decoder;
    }

    public static String token(String username) {
        return token(username, ISSUER, AUDIENCE, Instant.now().plusSeconds(300));
    }

    public static String token(String username, String issuer, String audience, Instant expiresAt) {
        String role = switch (username) {
            case "ana", "marko" -> "KORISNIK";
            case "organizator" -> "ORGANIZATOR";
            case "admin" -> "ADMIN";
            default -> throw new IllegalArgumentException("Unknown fixture user: " + username);
        };
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("test-" + username).issuer(issuer).audience(audience)
                .issueTime(Date.from(Instant.now().minusSeconds(5)))
                .expirationTime(Date.from(expiresAt))
                .claim("preferred_username", username)
                .claim("realm_access", Map.of("roles", List.of(role, "default-roles-ulaznice")))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        try {
            jwt.sign(new RSASSASigner(KLJUCEVI.getPrivate()));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static KeyPair kljucevi() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
