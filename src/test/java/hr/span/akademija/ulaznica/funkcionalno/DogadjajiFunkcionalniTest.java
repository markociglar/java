package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import hr.span.akademija.ulaznica.ApiTestBaza;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class DogadjajiFunkcionalniTest extends ApiTestBaza {

    private List<String> polje(Odgovor odgovor, String naziv) {
        List<String> vrijednosti = new ArrayList<>();
        for (JsonNode dogadjaj : odgovor.json()) {
            vrijednosti.add(dogadjaj.get(naziv).asText());
        }
        return vrijednosti;
    }

    @Test
    @DisplayName("Događaji sortirani po datumu uzlazno")
    void poDatumuUzlazno() {
        String token = prijava("ana", "Ana12345!");

        Odgovor odgovor = get("/api/dogadjaji?sort=datum&smjer=asc", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(polje(odgovor, "datum")).isSorted().hasSizeGreaterThanOrEqualTo(8);
        assertThat(polje(odgovor, "naziv")).contains("Hamlet", "Božićni koncert");
    }

    @Test
    @DisplayName("Događaji sortirani po nazivu silazno")
    void poNazivuSilazno() {
        String token = prijava("ana", "Ana12345!");

        Odgovor odgovor = get("/api/dogadjaji?sort=naziv&smjer=desc", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(polje(odgovor, "naziv")).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    @DisplayName("Događaji sortirani po cijeni i po mjestu")
    void poCijeniIMjestu() {
        String token = prijava("ana", "Ana12345!");

        Odgovor poCijeni = get("/api/dogadjaji?sort=cijena&smjer=asc", token);
        Odgovor poMjestu = get("/api/dogadjaji?sort=mjesto&smjer=asc", token);

        assertThat(polje(poCijeni, "cijena").stream().map(BigDecimal::new).toList()).isSorted();
        assertThat(polje(poMjestu, "mjesto")).isSorted();
    }

    @Test
    @DisplayName("Zadano sortiranje je po datumu uzlazno")
    void zadanoSortiranje() {
        String token = prijava("ana", "Ana12345!");

        Odgovor odgovor = get("/api/dogadjaji", token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(polje(odgovor, "datum")).isSorted();
    }

    @Test
    @DisplayName("Popis odbija nedopuštene vrijednosti sortiranja")
    void odbijaNedopustenoSortiranje() {
        String token = prijava("ana", "Ana12345!");

        assertThat(get("/api/dogadjaji?sort=naziv%20DESC", token).status()).isEqualTo(400);
        assertThat(get("/api/dogadjaji?sort=naziv&smjer=asc%2C%20id", token).status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Popis događaja prikazuje slobodna mjesta")
    void slobodnaMjesta() {
        String token = prijava("ana", "Ana12345!");

        for (JsonNode d : get("/api/dogadjaji", token).json()) {
            assertThat(d.get("slobodno").asInt()).isEqualTo(d.get("kapacitet").asInt() - d.get("prodano").asInt());
        }
    }

    @Test
    @DisplayName("Popis događaja traži prijavu")
    void traziPrijavu() {
        assertThat(get("/api/dogadjaji", null).status()).isEqualTo(401);
    }
}
