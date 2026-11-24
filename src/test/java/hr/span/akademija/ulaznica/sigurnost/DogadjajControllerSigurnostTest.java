package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("sigurnost")
class DogadjajControllerSigurnostTest extends ApiTestBaza {

    @Test
    @DisplayName("Uvoz događaja odbija XML s vanjskim entitetom")
    void uvozOdbijaVanjskeXmlEntitete() throws Exception {
        Path tajna = Files.createTempFile("ulaznica-xxe-", ".txt");
        try {
            Files.writeString(tajna, "XXE_TEST_MARKER", StandardCharsets.UTF_8);
            String xml = """
                    <?xml version="1.0"?>
                    <!DOCTYPE dogadjaji [<!ENTITY xxe SYSTEM "%s">]>
                    <dogadjaji><dogadjaj><naziv>&xxe;</naziv><mjesto>Test</mjesto>
                    <datum>2027-01-01T20:00</datum><cijena>1.00</cijena><kapacitet>1</kapacitet>
                    </dogadjaj></dogadjaji>
                    """.formatted(tajna.toUri());

            String token = token("organizator");
            Odgovor odgovor = posalji("POST", "/api/dogadjaji/uvoz", token, "application/xml", xml);

            assertThat(odgovor.status()).isEqualTo(400);
            assertThat(odgovor.tijelo()).doesNotContain("XXE_TEST_MARKER");
        } finally {
            Files.deleteIfExists(tajna);
        }
    }

    @Test
    @DisplayName("Nepoznat Origin ne dobiva pristup API-ju s vjerodajnicama")
    void corsOdbijaNepoznatOrigin() {
        String token = token("ana");
        Odgovor odgovor = posalji("GET", "/api/dogadjaji", token, null, null,
                "Origin", "https://napadac.example");

        assertThat(odgovor.zaglavlje("Access-Control-Allow-Origin")).isNull();
        assertThat(odgovor.zaglavlje("Access-Control-Allow-Credentials")).isNull();
    }
}
