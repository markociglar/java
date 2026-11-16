package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("sigurnost")
class IspisControllerSigurnostTest extends ApiTestBaza {

    @Test
    @DisplayName("Ispis ulaznice ne umeće ime kupca kao HTML")
    void imeNaUlazniciJeSigurnoZaHtml() {
        String token = prijava("ana", "Ana12345!");
        String ime = "<img src=x onerror=alert(1)>";
        Odgovor kreirana = postJson("/api/narudzbe", token,
                Map.of("dogadjajId", 3, "kolicina", 1, "imeNaUlaznici", ime, "oib", "48291736504"));
        assertThat(kreirana.status()).isEqualTo(201);

        long id = kreirana.json().get("id").asLong();
        Odgovor ispis = get("/ispis/narudzbe/" + id, token);

        assertThat(ispis.status()).isEqualTo(200);
        assertThat(ispis.tijelo()).doesNotContain(ime).doesNotContain("<img");
    }
}
