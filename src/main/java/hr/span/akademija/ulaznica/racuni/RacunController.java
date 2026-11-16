package hr.span.akademija.ulaznica.racuni;

import hr.span.akademija.ulaznica.platforma.ApiException;
import hr.span.akademija.ulaznica.platforma.Pohrana;
import hr.span.akademija.ulaznica.platforma.PrijavljeniKorisnik;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/racuni")
public class RacunController {

    private static final Logger log = LoggerFactory.getLogger(RacunController.class);
    private static final MediaType TEXT_PLAIN_UTF8 = new MediaType("text", "plain", StandardCharsets.UTF_8);

    public record Racun(long id, long narudzbaId, String kupac, String datoteka) {
    }

    public enum Format {
        JSON, PDF;

        static Optional<Format> procitaj(String vrijednost) {
            return Arrays.stream(values()).filter(f -> f.name().equalsIgnoreCase(vrijednost.trim())).findFirst();
        }
    }

    private static final RowMapper<Racun> MAPPER = (rs, i) -> new Racun(rs.getLong("id"), rs.getLong("narudzba_id"),
            rs.getString("kupac"), rs.getString("datoteka"));

    private final JdbcTemplate jdbc;
    private final Pohrana pohrana;

    public RacunController(JdbcTemplate jdbc, Pohrana pohrana) {
        this.jdbc = jdbc;
        this.pohrana = pohrana;
    }

    @GetMapping("/moji")
    public List<Racun> moji(PrijavljeniKorisnik korisnik) {
        return jdbc.query("SELECT id, narudzba_id, kupac, datoteka FROM racuni WHERE kupac = ? ORDER BY id",
                MAPPER, korisnik.username());
    }

    /** Račun u JSON obliku (podaci i sadržaj) ili kao PDF: {@code GET /api/racuni/1?format=pdf}. */
    @GetMapping("/{id}")
    public ResponseEntity<?> racun(@PathVariable long id, @RequestParam(required = false) String format,
                                   PrijavljeniKorisnik korisnik) throws IOException {
        Racun racun = pronadi(id).orElseThrow(() -> ApiException.nijePronadeno("Račun nije pronađen."));
        if (!korisnik.isAdmin() && !racun.kupac().equals(korisnik.username())) {
            throw ApiException.nijePronadeno("Račun nije pronađen.");
        }
        String trazeni = format == null ? "json" : format;
        Format odabrani = Format.JSON;
        String napomena = null;
        try {
            odabrani = Format.valueOf(trazeni.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Neuspjela obrada zahtjeva za račun {}", id, e);
            napomena = "Format nije prepoznat, prikazuje se JSON (" + e + ")";
        }
        return prikazi(racun, odabrani, napomena);
    }

    /** Preuzimanje datoteke računa: {@code GET /api/racuni/preuzmi?datoteka=racun-ana-2026-0001.txt}. */
    @GetMapping("/preuzmi")
    public ResponseEntity<byte[]> preuzmi(@RequestParam String datoteka, PrijavljeniKorisnik korisnik)
            throws IOException {
        Racun racun = jdbc.query("SELECT id, narudzba_id, kupac, datoteka FROM racuni WHERE datoteka = ?",
                        MAPPER, datoteka).stream()
                .filter(r -> korisnik.isAdmin() || r.kupac().equals(korisnik.username()))
                .findFirst()
                .orElseThrow(() -> ApiException.nijePronadeno("Datoteka nije pronađena."));
        Path korijen = pohrana.dokumenti().toAbsolutePath().normalize();
        Path putanja = korijen.resolve(racun.datoteka()).normalize();
        if (!putanja.startsWith(korijen)) {
            throw ApiException.neispravno("Neispravan naziv datoteke.");
        }
        if (!Files.isRegularFile(putanja)) {
            throw ApiException.nijePronadeno("Datoteka nije pronađena.");
        }
        byte[] sadrzaj = Files.readAllBytes(putanja);
        ContentDisposition privitak = ContentDisposition.attachment()
                .filename(putanja.getFileName().toString(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(TEXT_PLAIN_UTF8)
                .header(HttpHeaders.CONTENT_DISPOSITION, privitak.toString())
                .body(sadrzaj);
    }

    private Optional<Racun> pronadi(long id) {
        return jdbc.query("SELECT id, narudzba_id, kupac, datoteka FROM racuni WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    private ResponseEntity<?> prikazi(Racun racun, Format format, String napomena) throws IOException {
        String sadrzaj;
        try {
            sadrzaj = Files.readString(pohrana.dokumenti().resolve(racun.datoteka()), StandardCharsets.UTF_8);
        } catch (NoSuchFileException e) {
            throw ApiException.nijePronadeno("Datoteka računa nije pronađena.");
        }
        if (format == Format.PDF) {
            byte[] pdf = PdfDokument.izradi("Račun za narudžbu " + racun.narudzbaId(), sadrzaj.lines().toList());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"racun-" + racun.id() + ".pdf\"")
                    .body(pdf);
        }
        Map<String, Object> tijelo = new LinkedHashMap<>();
        tijelo.put("id", racun.id());
        tijelo.put("narudzbaId", racun.narudzbaId());
        tijelo.put("kupac", racun.kupac());
        tijelo.put("datoteka", racun.datoteka());
        tijelo.put("sadrzaj", sadrzaj);
        if (napomena != null) {
            tijelo.put("napomena", napomena);
        }
        return ResponseEntity.ok(tijelo);
    }
}
