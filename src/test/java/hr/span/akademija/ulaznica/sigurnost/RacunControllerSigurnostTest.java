package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("sigurnost")
class RacunControllerSigurnostTest extends ApiTestBaza {

    @Test
    @DisplayName("Nepoznat format ne otkriva tuđi račun")
    void nepoznatFormatNePreskaceProvjeruVlasnistva() {
        String marko = prijava("marko", "Marko12345!");
        Odgovor odgovor = get("/api/racuni/1?format=nepoznat", marko);

        assertThat(odgovor.status()).isIn(403, 404);
        assertThat(odgovor.tijelo()).doesNotContain("Kupac: ana");
    }
}
