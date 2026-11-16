package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import hr.span.akademija.ulaznica.ApiTestBaza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class UvozDogadjajaFunkcionalniTest extends ApiTestBaza {

    private static final String XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <dogadjaji>
              <dogadjaj>
                <naziv>Klapa u Lisinskom</naziv>
                <mjesto>Lisinski, Zagreb</mjesto>
                <datum>2026-12-12T20:00</datum>
                <cijena>25.00</cijena>
                <kapacitet>300</kapacitet>
              </dogadjaj>
            </dogadjaji>
            """;

    @Test
    @DisplayName("Organizator uvozi događaje iz XML-a")
    void organizatorUvozi() {
        String token = prijava("organizator", "Ovlasteni12345!");

        Odgovor odgovor = posalji("POST", "/api/dogadjaji/uvoz", token, "application/xml", XML);

        assertThat(odgovor.status()).isEqualTo(200);
        JsonNode tijelo = odgovor.json();
        assertThat(tijelo.get("uvezeno").asInt()).isEqualTo(1);
        assertThat(tijelo.get("dogadjaji").get(0).get("naziv").asText()).isEqualTo("Klapa u Lisinskom");
        long id = tijelo.get("dogadjaji").get(0).get("id").asLong();
        boolean vidljiv = false;
        for (JsonNode d : get("/api/dogadjaji", token).json()) {
            vidljiv |= d.get("id").asLong() == id;
        }
        assertThat(vidljiv).isTrue();
    }

    @Test
    @DisplayName("Administrator može uvoziti događaje")
    void administratorUvozi() {
        String token = prijava("admin", "Admin12345!");

        assertThat(posalji("POST", "/api/dogadjaji/uvoz", token, "application/xml", XML).status()).isEqualTo(200);
    }

    @Test
    @DisplayName("Korisnik s ulogom KORISNIK ne može uvoziti događaje")
    void korisnikNeMozeUvoziti() {
        String token = prijava("ana", "Ana12345!");

        assertThat(posalji("POST", "/api/dogadjaji/uvoz", token, "application/xml", XML).status()).isEqualTo(403);
    }

    @Test
    @DisplayName("Neispravan XML vraća 400")
    void neispravanXml() {
        String token = prijava("organizator", "Ovlasteni12345!");

        Odgovor odgovor = posalji("POST", "/api/dogadjaji/uvoz", token, "application/xml", "<dogadjaji><dogadjaj>");

        assertThat(odgovor.status()).isEqualTo(400);
    }
}
