package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class PrijavaObrascemFunkcionalniTest extends ApiTestBaza {
    @Test
    @DisplayName("Početna stranica vodi na Keycloak korisničku konzolu")
    void pocetnaStranica() {
        Odgovor odgovor = get("/", null);
        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.tijelo()).contains("http://localhost:8180/realms/ulaznice/account");
    }

    @Test
    @DisplayName("Stari obrazac ne prihvaća lozinku i ne izdaje prijavnu sesiju")
    void lokalniObrazacJeUklonjen() {
        Odgovor odgovor = postForma("/prijava", Map.of("username", "ana", "password", "Ana12345!"));
        assertThat(odgovor.status()).isEqualTo(403);
        assertThat(odgovor.zaglavlje("Set-Cookie")).doesNotContain("JSESSIONID");
    }

    @Test
    @DisplayName("SESSION kolačić bez Bearer tokena ne daje pristup API-ju")
    void kolacicNeVrijediZaApi() {
        Odgovor odgovor = posalji("GET", "/api/ja", null, null, null, "Cookie", "SESSION=neki-token");
        assertThat(odgovor.status()).isEqualTo(401);
    }
}
