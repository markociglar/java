package hr.span.akademija.ulaznica;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.annotation.Import;
import hr.span.akademija.ulaznica.keycloak.TestniKeycloak;

/**
 * Zajednička baza za testove koji aplikaciju pokreću na nasumičnom portu i šalju stvarne HTTP zahtjeve
 * ({@link java.net.http.HttpClient}, bez automatskog praćenja preusmjeravanja).
 *
 * Dokumenti i poruke e-pošte spremaju se u privremenu mapu, a ne u {@code ./data}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestniKeycloak.class)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:ulaznica-test;DB_CLOSE_DELAY=-1")
public abstract class ApiTestBaza {

    protected static final ObjectMapper JSON = new ObjectMapper();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Path RADNA_MAPA = privremenaMapa();

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void mape(DynamicPropertyRegistry registry) {
        registry.add("app.storage-dir", () -> mapaDokumenata().toString());
    }

    protected static Path mapaDokumenata() {
        return RADNA_MAPA.resolve("dokumenti");
    }

    /** HTTP odgovor: status, zaglavlja i tijelo kao tekst. */
    public record Odgovor(int status, HttpHeaders zaglavlja, String tijelo) {

        public String zaglavlje(String naziv) {
            return zaglavlja.firstValue(naziv).orElse(null);
        }

        public JsonNode json() {
            try {
                return JSON.readTree(tijelo);
            } catch (JsonProcessingException e) {
                throw new AssertionError("Odgovor nije JSON (status " + status + "): " + tijelo, e);
            }
        }
    }

    /** Šalje zahtjev; {@code zaglavlja} su parovi naziv, vrijednost. */
    protected Odgovor posalji(String metoda, String put, String token, String tipSadrzaja, String tijelo,
                              String... zaglavlja) {
        HttpRequest.Builder zahtjev = HttpRequest.newBuilder(URI.create("http://localhost:" + port + put))
                .timeout(Duration.ofSeconds(30));
        if (token != null) {
            zahtjev.header("Authorization", "Bearer " + token);
        }
        if (tipSadrzaja != null) {
            zahtjev.header("Content-Type", tipSadrzaja);
        }
        for (int i = 0; i + 1 < zaglavlja.length; i += 2) {
            zahtjev.header(zaglavlja[i], zaglavlja[i + 1]);
        }
        zahtjev.method(metoda, tijelo == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(tijelo, StandardCharsets.UTF_8));
        try {
            HttpResponse<String> odgovor = HTTP.send(zahtjev.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new Odgovor(odgovor.statusCode(), odgovor.headers(), odgovor.body());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    protected Odgovor get(String put, String token) {
        return posalji("GET", put, token, null, null);
    }

    protected Odgovor delete(String put, String token) {
        return posalji("DELETE", put, token, null, null);
    }

    protected Odgovor postJson(String put, String token, Object tijelo) {
        return posalji("POST", put, token, "application/json", uJson(tijelo));
    }

    protected Odgovor postForma(String put, Map<String, String> polja) {
        String tijelo = polja.entrySet().stream()
                .map(e -> enc(e.getKey()) + "=" + enc(e.getValue()))
                .collect(Collectors.joining("&"));
        return posalji("POST", put, null, "application/x-www-form-urlencoded", tijelo);
    }

    /** Potpisani access token s istim claimovima kao Keycloak realm. */
    protected String token(String username) {
        return TestniKeycloak.token(username);
    }

    protected static String enc(String vrijednost) {
        return URLEncoder.encode(vrijednost, StandardCharsets.UTF_8).replace("+", "%20");
    }

    protected static String uJson(Object vrijednost) {
        try {
            return JSON.writeValueAsString(vrijednost);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Path privremenaMapa() {
        try {
            return Files.createTempDirectory("ulaznica-test-");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
