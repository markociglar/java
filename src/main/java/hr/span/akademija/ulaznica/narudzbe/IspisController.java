package hr.span.akademija.ulaznica.narudzbe;

import hr.span.akademija.ulaznica.narudzbe.NarudzbaService.Narudzba;
import hr.span.akademija.ulaznica.platforma.ApiException;
import hr.span.akademija.ulaznica.platforma.AppProperties;
import hr.span.akademija.ulaznica.platforma.Html;
import hr.span.akademija.ulaznica.platforma.PrijavljeniKorisnik;
import java.time.format.DateTimeFormatter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Ulaznica za ispis (HTML). */
@Controller
public class IspisController {

    private static final DateTimeFormatter VRIJEME = DateTimeFormatter.ofPattern("dd.MM.yyyy. 'u' HH:mm");

    private final NarudzbaService narudzbe;
    private final AppProperties postavke;

    public IspisController(NarudzbaService narudzbe, AppProperties postavke) {
        this.narudzbe = narudzbe;
        this.postavke = postavke;
    }

    @GetMapping("/ispis/narudzbe/{id}")
    public ResponseEntity<String> ulaznica(@PathVariable long id, PrijavljeniKorisnik korisnik) {
        Narudzba n = narudzbe.pronadi(id)
                .filter(x -> korisnik.isAdmin() || x.kupac().equals(korisnik.username()))
                .orElseThrow(() -> ApiException.nijePronadeno("Narudžba nije pronađena."));
        String html = """
                <!DOCTYPE html>
                <html lang="hr">
                <head>
                <meta charset="UTF-8">
                <title>Ulaznica br. %d</title>
                <style>
                body { font-family: system-ui, sans-serif; max-width: 40rem; margin: 2rem auto; }
                .ulaznica { border: 2px dashed #333; padding: 1rem 1.5rem; }
                dt { font-weight: bold; margin-top: .5rem; }
                </style>
                </head>
                <body>
                <div class="ulaznica">
                <h1>%s</h1>
                <h2>%s</h2>
                <dl>
                <dt>Ime na ulaznici</dt><dd>%s</dd>
                <dt>Mjesto</dt><dd>%s</dd>
                <dt>Datum</dt><dd>%s</dd>
                <dt>Broj ulaznica</dt><dd>%d</dd>
                <dt>Broj narudžbe</dt><dd>%d</dd>
                </dl>
                <p>Ulaznicu pokažite na ulazu (ispisanu ili na mobitelu).</p>
                </div>
                </body>
                </html>
                """.formatted(n.id(), tekst(postavke.naziv()), tekst(n.dogadjaj()), tekst(n.imeNaUlaznici()),
                tekst(n.mjesto()), n.datum() == null ? "" : VRIJEME.format(n.datum()), n.kolicina(), n.id());
        return ResponseEntity.ok()
                .contentType(Html.TEXT_HTML_UTF8)
                .body(html);
    }

    private static String tekst(String vrijednost) {
        return Html.escape(vrijednost);
    }
}
