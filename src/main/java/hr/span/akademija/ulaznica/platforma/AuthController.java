package hr.span.akademija.ulaznica.platforma;

import hr.span.akademija.ulaznica.platforma.KorisnikService.Korisnik;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final KorisnikService korisnici;

    public AuthController(KorisnikService korisnici) {
        this.korisnici = korisnici;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    @GetMapping("/ja")
    public Map<String, Object> ja(PrijavljeniKorisnik prijavljeni) {
        Korisnik k = korisnici.pronadi(prijavljeni.username())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Potrebna je prijava."));
        Map<String, Object> podaci = new LinkedHashMap<>();
        podaci.put("username", k.username());
        podaci.put("ime", k.ime());
        podaci.put("prezime", k.prezime());
        podaci.put("email", k.email());
        podaci.put("uloga", prijavljeni.uloga());
        return podaci;
    }
}
