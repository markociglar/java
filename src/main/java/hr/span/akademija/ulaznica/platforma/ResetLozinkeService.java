package hr.span.akademija.ulaznica.platforma;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Zaboravljena lozinka: token se šalje e-poštom, a njime se postavlja nova lozinka. */
@Service
public class ResetLozinkeService {
    private static final SecureRandom SLUCAJNO = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final KorisnikService korisnici;
    private final PostaService posta;

    public ResetLozinkeService(JdbcTemplate jdbc, KorisnikService korisnici, PostaService posta) {
        this.jdbc = jdbc;
        this.korisnici = korisnici;
        this.posta = posta;
    }

    public void zatrazi(String username) {
        Optional<KorisnikService.Korisnik> korisnik = korisnici.pronadi(username);
        if (korisnik.isEmpty()) {
            return;
        }
        byte[] bajtovi = new byte[32];
        SLUCAJNO.nextBytes(bajtovi);
        String token = HexFormat.of().formatHex(bajtovi);
        jdbc.update("INSERT INTO reset_tokeni (username, token, istjece) VALUES (?, ?, ?)",
                username, token, LocalDateTime.now().plusHours(1));
        posta.posaljiUputeZaLozinku(korisnik.get(), token);
    }

    @Transactional
    public boolean postaviNovuLozinku(String token, String novaLozinka) {
        KorisnikService.provjeriLozinku(novaLozinka);
        if (token == null || token.isBlank()) {
            return false;
        }
        List<String> vlasnici = jdbc.queryForList(
                "SELECT username FROM reset_tokeni WHERE token = ? AND iskoristen = FALSE AND istjece > ?",
                String.class, token.trim(), LocalDateTime.now());
        if (vlasnici.isEmpty()) {
            return false;
        }
        if (jdbc.update("UPDATE reset_tokeni SET iskoristen = TRUE WHERE token = ? AND iskoristen = FALSE",
                token.trim()) != 1) {
            return false;
        }
        korisnici.promijeniLozinku(vlasnici.get(0), novaLozinka);
        return true;
    }
}
