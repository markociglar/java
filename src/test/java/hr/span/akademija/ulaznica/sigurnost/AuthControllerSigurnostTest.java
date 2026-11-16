package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Tag("sigurnost")
class AuthControllerSigurnostTest extends ApiTestBaza {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("Reset lozinke ne prihvaća token izračunat iz korisničkog imena i datuma")
    void predvidljivResetToken() throws Exception {
        String username = noviKorisnik("resetaudit", "StaraLozinka123!");
        assertThat(postJson("/api/auth/reset-zahtjev", null, Map.of("username", username)).status())
                .isEqualTo(202);

        String izracunatiToken = HexFormat.of().formatHex(MessageDigest.getInstance("MD5")
                .digest((username + LocalDate.now()).getBytes(StandardCharsets.UTF_8)));
        Odgovor reset = postJson("/api/auth/reset", null,
                Map.of("token", izracunatiToken, "novaLozinka", "NovaLozinka123!"));

        assertThat(reset.status()).isEqualTo(400);
        assertThat(postJson("/api/auth/login", null,
                Map.of("username", username, "password", "StaraLozinka123!")).status()).isEqualTo(200);
    }

    @Test
    @DisplayName("Dvije jednake lozinke imaju različite pohranjene sažetke")
    void lozinkeImajuJedinstvenuSol() {
        String prvi = noviKorisnik("hashaudit", "IstaLozinka123!");
        String drugi = noviKorisnik("hashaudit", "IstaLozinka123!");

        String prviHash = jdbc.queryForObject("SELECT password_hash FROM korisnici WHERE username = ?",
                String.class, prvi);
        String drugiHash = jdbc.queryForObject("SELECT password_hash FROM korisnici WHERE username = ?",
                String.class, drugi);

        assertThat(prviHash).isNotBlank().isNotEqualTo(drugiHash);
        assertThat(prijava(prvi, "IstaLozinka123!")).isNotBlank();
        assertThat(prijava(drugi, "IstaLozinka123!")).isNotBlank();
    }
}
