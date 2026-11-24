package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class AutentikacijaFunkcionalniTest extends ApiTestBaza {
    @Test
    @DisplayName("Health endpoint je javan")
    void healthBezPrijave() {
        assertThat(get("/api/health", null).json().get("status").asText()).isEqualTo("UP");
    }

    @Test
    @DisplayName("Bez Bearer tokena API vraća 401")
    void bezTokena() {
        Odgovor odgovor = get("/api/ja", null);
        assertThat(odgovor.status()).isEqualTo(401);
        assertThat(odgovor.zaglavlje("WWW-Authenticate")).contains("Bearer");
    }

    @Test
    @DisplayName("Keycloak username i uloga određuju prikaz profila")
    void podaciPrijavljenogKorisnika() {
        Odgovor odgovor = get("/api/ja", token("ana"));
        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.json().get("username").asText()).isEqualTo("ana");
        assertThat(odgovor.json().get("ime").asText()).isEqualTo("Ana");
        assertThat(odgovor.json().get("uloga").asText()).isEqualTo("KORISNIK");
    }

    @Test
    @DisplayName("Organizatorova realm uloga dolazi iz JWT-a")
    void organizatorImaUlogu() {
        Odgovor odgovor = get("/api/ja", token("organizator"));
        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.json().get("uloga").asText()).isEqualTo("ORGANIZATOR");
    }

    @Test
    @DisplayName("Lokalna prijava i registracija nisu API rute")
    void lokalniAuthJeUklonjen() {
        assertThat(postJson("/api/auth/login", null, Map.of("username", "ana", "password", "Ana12345!"))
                .status()).isEqualTo(401);
        assertThat(postJson("/api/auth/register", token("ana"), Map.of("username", "novi"))
                .status()).isEqualTo(404);
    }
}
