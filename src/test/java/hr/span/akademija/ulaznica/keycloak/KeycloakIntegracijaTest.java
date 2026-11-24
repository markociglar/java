package hr.span.akademija.ulaznica.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

/** Runs against the real imported realm when local Keycloak is available. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:ulaznica-keycloak-live;DB_CLOSE_DELAY=-1")
class KeycloakIntegracijaTest {
    private static final String TOKEN_URL =
            "http://localhost:8180/realms/ulaznice/protocol/openid-connect/token";
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    @LocalServerPort int port;

    @Test
    void svaCetiriKorisnikaDobivajuTokenKojiApiPrihvaca() throws Exception {
        HttpRequest discovery = HttpRequest.newBuilder(URI.create(TestniKeycloak.ISSUER
                + "/.well-known/openid-configuration")).timeout(Duration.ofSeconds(2)).build();
        try {
            assertThat(HTTP.send(discovery, HttpResponse.BodyHandlers.discarding()).statusCode()).isEqualTo(200);
        } catch (ConnectException e) {
            Assumptions.assumeTrue(false, "Lokalni Keycloak nije dostupan");
        } catch (java.net.http.HttpTimeoutException e) {
            Assumptions.assumeTrue(false, "Lokalni Keycloak nije dostupan");
        }

        Map<String, String[]> korisnici = Map.of(
                "ana", new String[] {"Ana12345!", "KORISNIK"},
                "marko", new String[] {"Marko12345!", "KORISNIK"},
                "organizator", new String[] {"Ovlasteni12345!", "ORGANIZATOR"},
                "admin", new String[] {"Admin12345!", "ADMIN"});
        for (var entry : korisnici.entrySet()) {
            String token = token(entry.getKey(), entry.getValue()[0]);
            HttpRequest zahtjev = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/ja"))
                    .header("Authorization", "Bearer " + token).build();
            HttpResponse<String> odgovor = HTTP.send(zahtjev, HttpResponse.BodyHandlers.ofString());
            assertThat(odgovor.statusCode()).isEqualTo(200);
            JsonNode profil = new ObjectMapper().readTree(odgovor.body());
            assertThat(profil.path("username").asText()).isEqualTo(entry.getKey());
            assertThat(profil.path("uloga").asText()).isEqualTo(entry.getValue()[1]);
        }
        HttpRequest pogresnaLozinka = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=password&client_id=ulaznice-dev-cli&username=ana&password=pogresno"))
                .build();
        HttpResponse<String> odbijenaPrijava = HTTP.send(pogresnaLozinka, HttpResponse.BodyHandlers.ofString());
        assertThat(odbijenaPrijava.statusCode()).isEqualTo(400);
        assertThat(new ObjectMapper().readTree(odbijenaPrijava.body()).path("error").asText())
                .isEqualTo("invalid_grant");
    }

    private static String token(String username, String password) throws Exception {
        String tijelo = "grant_type=password&client_id=ulaznice-dev-cli&username="
                + URLEncoder.encode(username, StandardCharsets.UTF_8)
                + "&password=" + URLEncoder.encode(password, StandardCharsets.UTF_8);
        HttpRequest zahtjev = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tijelo)).build();
        HttpResponse<String> odgovor = HTTP.send(zahtjev, HttpResponse.BodyHandlers.ofString());
        assertThat(odgovor.statusCode()).isEqualTo(200);
        return new ObjectMapper().readTree(odgovor.body()).path("access_token").asText();
    }
}
