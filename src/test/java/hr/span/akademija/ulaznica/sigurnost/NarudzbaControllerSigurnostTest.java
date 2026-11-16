package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("sigurnost")
class NarudzbaControllerSigurnostTest extends ApiTestBaza {

    private long novaNarudzba(String token, long dogadjajId) {
        Odgovor odgovor = postJson("/api/narudzbe", token,
                Map.of("dogadjajId", dogadjajId, "kolicina", 1,
                        "imeNaUlaznici", "Sigurnosni test", "oib", "48291736504"));
        assertThat(odgovor.status()).isEqualTo(201);
        return odgovor.json().get("id").asLong();
    }

    @Test
    @DisplayName("Korisnik ne može stornirati tuđu narudžbu")
    void stornoTudjeNarudzbeJeOdbijen() {
        String ana = prijava("ana", "Ana12345!");
        String marko = prijava("marko", "Marko12345!");
        long id = novaNarudzba(ana, 3);

        Odgovor odgovor = delete("/api/narudzbe/" + id, marko);

        assertThat(odgovor.status()).isIn(403, 404);
        assertThat(get("/api/narudzbe/moje", ana).json().findValuesAsText("id"))
                .contains(Long.toString(id));
    }

    @Test
    @DisplayName("Zahtjev s kolačićem s drugog origina ne može stornirati narudžbu")
    void csrfStornoJeOdbijen() {
        String ana = prijava("ana", "Ana12345!");
        long id = novaNarudzba(ana, 3);
        Odgovor webPrijava = postForma("/prijava",
                Map.of("username", "ana", "password", "Ana12345!"));
        assertThat(webPrijava.status()).isEqualTo(302);
        String kolacic = webPrijava.zaglavlje("Set-Cookie").split(";")[0];

        Odgovor odgovor = posalji("DELETE", "/api/narudzbe/" + id, null, null, null,
                "Cookie", kolacic, "Origin", "http://localhost:18081");

        assertThat(odgovor.status()).isIn(401, 403);
        assertThat(get("/api/narudzbe/moje", ana).json().findValuesAsText("id"))
                .contains(Long.toString(id));
    }

    @Test
    @DisplayName("Usporedne kupnje ne mogu premašiti kapacitet događaja")
    void usporedneKupnjeNePrelazeKapacitet() throws Exception {
        String organizator = prijava("organizator", "Ovlasteni12345!");
        String xml = """
                <dogadjaji><dogadjaj><naziv>Test zadnjeg mjesta</naziv><mjesto>Test</mjesto>
                <datum>2027-02-01T20:00</datum><cijena>1.00</cijena><kapacitet>1</kapacitet>
                </dogadjaj></dogadjaji>
                """;
        Odgovor uvoz = posalji("POST", "/api/dogadjaji/uvoz", organizator, "application/xml", xml);
        assertThat(uvoz.status()).isEqualTo(200);
        long dogadjajId = uvoz.json().get("dogadjaji").get(0).get("id").asLong();
        String ana = prijava("ana", "Ana12345!");

        int brojKupaca = 4;
        ExecutorService dretve = Executors.newFixedThreadPool(brojKupaca);
        CountDownLatch spremni = new CountDownLatch(brojKupaca);
        CountDownLatch pocetak = new CountDownLatch(1);
        try {
            List<Future<Integer>> odgovori = new ArrayList<>();
            for (int i = 0; i < brojKupaca; i++) {
                odgovori.add(dretve.submit(() -> {
                    spremni.countDown();
                    pocetak.await();
                    return postJson("/api/narudzbe", ana,
                            Map.of("dogadjajId", dogadjajId, "kolicina", 1,
                                    "imeNaUlaznici", "Usporedna kupnja", "oib", "48291736504")).status();
                }));
            }
            assertThat(spremni.await(5, TimeUnit.SECONDS)).isTrue();
            pocetak.countDown();
            List<Integer> statusi = new ArrayList<>();
            for (Future<Integer> odgovor : odgovori) {
                statusi.add(odgovor.get(20, TimeUnit.SECONDS));
            }

            assertThat(statusi).containsOnly(201, 409);
            assertThat(statusi.stream().filter(s -> s == 201).count()).isEqualTo(1);
            JsonNode dogadjaj = null;
            for (JsonNode zapis : get("/api/dogadjaji", ana).json()) {
                if (zapis.get("id").asLong() == dogadjajId) {
                    dogadjaj = zapis;
                    break;
                }
            }
            assertThat(dogadjaj).isNotNull();
            assertThat(dogadjaj.get("prodano").asInt()).isEqualTo(1);
        } finally {
            pocetak.countDown();
            dretve.shutdownNow();
        }
    }
}
