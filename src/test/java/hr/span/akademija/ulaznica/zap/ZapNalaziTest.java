package hr.span.akademija.ulaznica.zap;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Regresijske provjere nalaza iz ZAP izvjestaja za localhost:8080. */
@Tag("zap")
class ZapNalaziTest extends ApiTestBaza {

    @Test
    @DisplayName("HTML ispis ulaznice salje Content-Security-Policy")
    void ispisImaCsp() {
        Odgovor ispis = get("/ispis/narudzbe/1", token("ana"));

        assertThat(ispis.status()).isEqualTo(200);
        assertThat(ispis.zaglavlje("Content-Security-Policy")).isNotBlank();
    }

    @Test
    @DisplayName("HTML ispis ulaznice zabranjuje ugradnju u tudji okvir")
    void ispisSprjecavaClickjacking() {
        Odgovor ispis = get("/ispis/narudzbe/1", token("ana"));

        assertThat(ispis.status()).isEqualTo(200);
        String csp = ispis.zaglavlje("Content-Security-Policy");
        String xFrameOptions = ispis.zaglavlje("X-Frame-Options");
        assertThat("DENY".equalsIgnoreCase(xFrameOptions)
                || (csp != null && csp.matches("(?i)(?:^|.*;)\\s*frame-ancestors\\s+'none'\\s*(?:;.*|$)")))
                .as("Ocekivan je X-Frame-Options: DENY ili CSP frame-ancestors 'none'")
                .isTrue();
    }

    @Test
    @DisplayName("HTML i JSON odgovori salju X-Content-Type-Options nosniff")
    void odgovoriImajuNosniff() {
        String token = token("ana");
        Odgovor ispis = get("/ispis/narudzbe/1", token);
        Odgovor json = get("/api/racuni/1?format=json", token);

        assertThat(ispis.status()).isEqualTo(200);
        assertThat(json.status()).isEqualTo(200);
        assertThat(ispis.zaglavlje("X-Content-Type-Options")).isEqualToIgnoringCase("nosniff");
        assertThat(json.zaglavlje("X-Content-Type-Options")).isEqualToIgnoringCase("nosniff");
    }

    @Test
    @DisplayName("Spremljena skripta iz JSON odgovora nije aktivni HTML na ulaznici")
    void spremljeniXssJeKodiranPriIspisu() {
        String token = token("ana");
        String payload = "<script>alert(1);</script>";
        Odgovor kreirana = postJson("/api/narudzbe", token,
                Map.of("dogadjajId", 3, "kolicina", 1, "imeNaUlaznici", payload,
                        "oib", "48291736504"));
        assertThat(kreirana.status()).isEqualTo(201);
        long id = kreirana.json().get("id").asLong();

        Odgovor moje = get("/api/narudzbe/moje", token);
        assertThat(moje.status()).isEqualTo(200);
        assertThat(moje.zaglavlje("Content-Type")).startsWith("application/json");
        assertThat(moje.json().findValuesAsText("imeNaUlaznici")).contains(payload);

        Odgovor ispis = get("/ispis/narudzbe/" + id, token);
        assertThat(ispis.status()).isEqualTo(200);
        assertThat(ispis.tijelo()).contains("&lt;script&gt;alert(1);&lt;/script&gt;")
                .doesNotContain(payload);
    }
}
