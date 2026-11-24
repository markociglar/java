package hr.span.akademija.ulaznica.platforma;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Public landing page. Authentication is performed by Keycloak. */
@Controller
public class PrijavaController {
    private final AppProperties postavke;

    public PrijavaController(AppProperties postavke) {
        this.postavke = postavke;
    }

    @GetMapping("/")
    public ResponseEntity<String> pocetna() {
        String tijelo = """
                <h1>%s</h1>
                <p>Ulaznice za koncerte i predstave: kupnja, računi i ispis ulaznica.</p>
                <ul>
                <li><a href="http://localhost:8180/realms/ulaznice/account">Prijava</a></li>
                <li><a href="/api/health">Stanje sustava</a></li>
                </ul>
                """.formatted(Html.escape(postavke.naziv()));
        return Html.odgovor(Html.stranica(postavke.naziv(), tijelo));
    }
}
