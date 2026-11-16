package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class NarudzbeFunkcionalniTest extends ApiTestBaza {

    /** Događaj 5 je rezerviran za posebne provjere i ne koristi se u ovim testovima. */
    private static final long IZUZETI_DOGADJAJ = 5;

    private long noviDogadjaj(int kapacitet) {
        String organizator = prijava("organizator", "Ovlasteni12345!");
        String xml = "<dogadjaji><dogadjaj><naziv>Dodatni koncert</naziv><mjesto>Dom sportova</mjesto>"
                + "<datum>2027-02-01T20:00</datum><cijena>20.00</cijena><kapacitet>" + kapacitet
                + "</kapacitet></dogadjaj></dogadjaji>";
        Odgovor odgovor = posalji("POST", "/api/dogadjaji/uvoz", organizator, "application/xml", xml);
        assertThat(odgovor.status()).isEqualTo(200);
        return odgovor.json().get("dogadjaji").get(0).get("id").asLong();
    }

    private JsonNode dogadjaj(String token, long id) {
        for (JsonNode d : get("/api/dogadjaji", token).json()) {
            if (d.get("id").asLong() == id) {
                return d;
            }
        }
        throw new AssertionError("Događaj " + id + " nije pronađen");
    }

    private List<Long> mojeNarudzbe(String token) {
        List<Long> id = new ArrayList<>();
        for (JsonNode n : get("/api/narudzbe/moje", token).json()) {
            id.add(n.get("id").asLong());
        }
        return id;
    }

    private Odgovor naruci(String token, long dogadjajId, int kolicina, String ime) {
        return postJson("/api/narudzbe", token,
                Map.of("dogadjajId", dogadjajId, "kolicina", kolicina, "imeNaUlaznici", ime, "oib", "48291736504"));
    }

    @Test
    @DisplayName("Nova narudžba vraća 201 i vidljiva je u mojim narudžbama")
    void novaNarudzba() {
        String token = prijava("ana", "Ana12345!");

        Odgovor odgovor = naruci(token, 3, 2, "Ana Anić");

        assertThat(odgovor.status()).isEqualTo(201);
        JsonNode narudzba = odgovor.json();
        assertThat(narudzba.get("kolicina").asInt()).isEqualTo(2);
        assertThat(narudzba.get("imeNaUlaznici").asText()).isEqualTo("Ana Anić");
        assertThat(narudzba.get("iznos").asDouble()).isEqualTo(36.0);
        assertThat(mojeNarudzbe(token)).contains(narudzba.get("id").asLong());
    }

    @Test
    @DisplayName("Narudžba izdaje račun vidljiv kupcu")
    void narudzbaIzdajeRacun() {
        String token = prijava("marko", "Marko12345!");
        long id = naruci(token, 4, 1, "Marko Marić").json().get("id").asLong();

        boolean postoji = false;
        for (JsonNode racun : get("/api/racuni/moji", token).json()) {
            postoji |= racun.get("narudzbaId").asLong() == id;
        }

        assertThat(postoji).isTrue();
    }

    @Test
    @DisplayName("Količina izvan raspona 1–4 vraća 400")
    void neispravnaKolicina() {
        String token = prijava("ana", "Ana12345!");

        assertThat(naruci(token, 3, 5, "Ana Anić").status()).isEqualTo(400);
        assertThat(naruci(token, 3, 0, "Ana Anić").status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Narudžba rasprodanog događaja vraća 409")
    void rasprodanDogadjaj() {
        long dogadjajId = noviDogadjaj(1);
        String ana = prijava("ana", "Ana12345!");
        String marko = prijava("marko", "Marko12345!");
        assertThat(naruci(ana, dogadjajId, 1, "Ana Anić").status()).isEqualTo(201);

        Odgovor odgovor = naruci(marko, dogadjajId, 1, "Marko Marić");

        assertThat(odgovor.status()).isEqualTo(409);
        assertThat(odgovor.json().get("greska").asText()).isEqualTo("Nema slobodnih mjesta.");
    }

    @Test
    @DisplayName("Kupac može stornirati vlastitu narudžbu")
    void stornoVlastiteNarudzbe() {
        String token = prijava("ana", "Ana12345!");
        long dogadjajId = noviDogadjaj(50);
        long id = naruci(token, dogadjajId, 3, "Ana Anić").json().get("id").asLong();
        int prodanoPrije = dogadjaj(token, dogadjajId).get("prodano").asInt();

        Odgovor odgovor = delete("/api/narudzbe/" + id, token);

        assertThat(odgovor.status()).isEqualTo(204);
        assertThat(mojeNarudzbe(token)).doesNotContain(id);
        assertThat(dogadjaj(token, dogadjajId).get("prodano").asInt()).isEqualTo(prodanoPrije - 3);
    }

    @Test
    @DisplayName("Ulaznica za ispis sadrži ime na ulaznici")
    void ispisUlaznice() {
        String token = prijava("ana", "Ana12345!");
        long id = naruci(token, 6, 1, "Ana i Marko").json().get("id").asLong();

        Odgovor odgovor = get("/ispis/narudzbe/" + id, token);

        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.zaglavlje("Content-Type")).startsWith("text/html");
        assertThat(odgovor.tijelo()).contains("Ana i Marko").contains("Božićni koncert");
    }

    @Test
    @DisplayName("Narudžba nepostojećeg događaja vraća 404")
    void nepostojeciDogadjaj() {
        String token = prijava("ana", "Ana12345!");

        assertThat(naruci(token, 999_999, 1, "Ana Anić").status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Događaj 5 se ne koristi u funkcionalnim testovima")
    void izuzetiDogadjajPostoji() {
        String token = prijava("ana", "Ana12345!");

        assertThat(dogadjaj(token, IZUZETI_DOGADJAJ).get("kapacitet").asInt()).isEqualTo(100);
    }
}
