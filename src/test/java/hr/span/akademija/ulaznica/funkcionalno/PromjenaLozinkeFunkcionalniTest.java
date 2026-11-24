package hr.span.akademija.ulaznica.funkcionalno;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("funkcionalno")
class PromjenaLozinkeFunkcionalniTest extends ApiTestBaza {
    @Test
    @DisplayName("Aplikacija ne izdaje lokalni token za reset lozinke")
    void lokalniResetJeUklonjen() {
        Odgovor zahtjev = postJson("/api/auth/reset-zahtjev", token("ana"), Map.of("username", "ana"));
        assertThat(zahtjev.status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Stari reset endpoint ne mijenja vjerodajnice")
    void stariResetJeUklonjen() {
        Odgovor odgovor = postJson("/api/auth/reset", token("ana"),
                Map.of("token", "neki-token", "novaLozinka", "Nova12345!"));
        assertThat(odgovor.status()).isEqualTo(404);
        assertThat(get("/api/ja", token("ana")).status()).isEqualTo(200);
    }
}
