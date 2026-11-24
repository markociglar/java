package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import hr.span.akademija.ulaznica.ApiTestBaza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class RacuniFunkcionalniTest extends ApiTestBaza {

    @Test
    @DisplayName("Moji računi sadrže račun iz početnih podataka")
    void mojiRacuni() {
        String token = token("ana");

        Odgovor odgovor = get("/api/racuni/moji", token);

        assertThat(odgovor.status()).isEqualTo(200);
        boolean pronaden = false;
        for (JsonNode racun : odgovor.json()) {
            assertThat(racun.get("kupac").asText()).isEqualTo("ana");
            pronaden |= racun.get("datoteka").asText().equals("racun-ana-2026-0001.txt");
        }
        assertThat(pronaden).isTrue();
    }

    @Test
    @DisplayName("Preuzimanje vlastite datoteke računa")
    void preuzimanjeVlastiteDatoteke() {
        String token = token("ana");

        Odgovor odgovor = get("/api/racuni/preuzmi?datoteka=racun-ana-2026-0001.txt", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.tijelo()).contains("Kupac: ana").contains("Gibonni");
    }

    @Test
    @DisplayName("Račun u JSON formatu sadrži podatke i sadržaj")
    void racunJson() {
        String token = token("marko");

        Odgovor odgovor = get("/api/racuni/2?format=json", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.json().get("narudzbaId").asLong()).isEqualTo(2);
        assertThat(odgovor.json().get("sadrzaj").asText()).contains("Hamlet");
    }

    @Test
    @DisplayName("Račun u PDF formatu")
    void racunPdf() {
        String token = token("ana");

        Odgovor odgovor = get("/api/racuni/1?format=pdf", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.zaglavlje("Content-Type")).startsWith("application/pdf");
        assertThat(odgovor.tijelo()).startsWith("%PDF");
    }

    @Test
    @DisplayName("Bez parametra format račun se vraća kao JSON")
    void zadaniFormat() {
        String token = token("ana");

        Odgovor odgovor = get("/api/racuni/1", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.json().get("sadrzaj").asText()).contains("Gibonni");
    }
}
