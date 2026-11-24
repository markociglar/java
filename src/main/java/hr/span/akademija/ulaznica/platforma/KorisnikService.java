package hr.span.akademija.ulaznica.platforma;

import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

/** Application profile data; credentials and roles are managed by Keycloak. */
@Service
public class KorisnikService {
    private static final RowMapper<Korisnik> MAPPER = (rs, i) -> new Korisnik(
            rs.getLong("id"), rs.getString("username"), rs.getString("email"),
            rs.getString("ime"), rs.getString("prezime"), rs.getString("oib"));

    private final JdbcTemplate jdbc;

    public KorisnikService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Korisnik(long id, String username, String email, String ime, String prezime, String oib) {
    }

    public Optional<Korisnik> pronadi(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return jdbc.query("SELECT id, username, email, ime, prezime, oib FROM korisnici WHERE username = ?",
                MAPPER, username).stream().findFirst();
    }

    public long kreiraj(String username, String email, String ime, String prezime, String oib) {
        return JdbcPomoc.umetni(jdbc,
                "INSERT INTO korisnici (username, email, ime, prezime, oib) VALUES (?, ?, ?, ?, ?)",
                username, email, ime, prezime, oib);
    }
}
