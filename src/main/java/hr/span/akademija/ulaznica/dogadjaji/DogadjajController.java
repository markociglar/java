package hr.span.akademija.ulaznica.dogadjaji;

import hr.span.akademija.ulaznica.platforma.ApiException;
import hr.span.akademija.ulaznica.platforma.JdbcPomoc;
import hr.span.akademija.ulaznica.platforma.PrijavljeniKorisnik;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dogadjaji")
public class DogadjajController {

    private static final Logger log = LoggerFactory.getLogger(DogadjajController.class);

    public record Dogadjaj(long id, String naziv, String mjesto, LocalDateTime datum, BigDecimal cijena,
                           int kapacitet, int prodano, int slobodno) {
    }

    private static final RowMapper<Dogadjaj> MAPPER = (rs, i) -> new Dogadjaj(
            rs.getLong("id"), rs.getString("naziv"), rs.getString("mjesto"),
            rs.getObject("datum", LocalDateTime.class), rs.getBigDecimal("cijena"),
            rs.getInt("kapacitet"), rs.getInt("prodano"), rs.getInt("kapacitet") - rs.getInt("prodano"));

    private static final Map<String, String> STUPCI = Map.of(
            "naziv", "naziv",
            "mjesto", "mjesto",
            "datum", "datum",
            "cijena", "cijena");

    private final JdbcTemplate jdbc;
    private final UvozDogadjaja uvoz;

    public DogadjajController(JdbcTemplate jdbc, UvozDogadjaja uvoz) {
        this.jdbc = jdbc;
        this.uvoz = uvoz;
    }

    /** Popis događaja, npr. {@code GET /api/dogadjaji?sort=datum&smjer=asc}. */
    @GetMapping
    public List<Dogadjaj> popis(@RequestParam(defaultValue = "datum") String sort,
                                @RequestParam(defaultValue = "asc") String smjer,
                                PrijavljeniKorisnik korisnik) {
        String stupac = STUPCI.get(sort);
        String redoslijed = switch (smjer.toLowerCase(Locale.ROOT)) {
            case "asc" -> "ASC";
            case "desc" -> "DESC";
            default -> null;
        };
        if (stupac == null || redoslijed == null) {
            throw ApiException.neispravno("Neispravan parametar sortiranja.");
        }
        String sql = "SELECT id, naziv, mjesto, datum, cijena, kapacitet, prodano FROM dogadjaji ORDER BY "
                + stupac + " " + redoslijed + ", id";
        return jdbc.query(sql, MAPPER);
    }

    /** Uvoz događaja iz XML-a (organizator ili administrator). */
    @PostMapping("/uvoz")
    @Transactional
    public Map<String, Object> uvezi(@RequestBody byte[] xml, PrijavljeniKorisnik korisnik) {
        if (!korisnik.imaUlogu("ORGANIZATOR", "ADMIN")) {
            throw ApiException.zabranjeno();
        }
        List<UvozDogadjaja.Stavka> stavke = uvoz.procitaj(xml);
        for (UvozDogadjaja.Stavka s : stavke) {
            if (s.kapacitet() < 1 || s.kapacitet() > 100000) {
                throw ApiException.neispravno("Kapacitet mora biti između 1 i 100000.");
            }
            if (s.cijena().signum() < 0) {
                throw ApiException.neispravno("Cijena ne smije biti negativna.");
            }
        }
        List<Map<String, Object>> uvezeni = new ArrayList<>();
        for (UvozDogadjaja.Stavka s : stavke) {
            long id = JdbcPomoc.umetni(jdbc,
                    "INSERT INTO dogadjaji (naziv, mjesto, datum, cijena, kapacitet, prodano) VALUES (?, ?, ?, ?, ?, 0)",
                    s.naziv(), s.mjesto(), s.datum(), s.cijena(), s.kapacitet());
            Map<String, Object> dogadjaj = new LinkedHashMap<>();
            dogadjaj.put("id", id);
            dogadjaj.put("naziv", s.naziv());
            uvezeni.add(dogadjaj);
        }
        log.info("Korisnik {} uvezao je {} događaja", korisnik.username(), uvezeni.size());
        Map<String, Object> odgovor = new LinkedHashMap<>();
        odgovor.put("uvezeno", uvezeni.size());
        odgovor.put("dogadjaji", uvezeni);
        return odgovor;
    }
}
