package hr.span.akademija.ulaznica.narudzbe;

import hr.span.akademija.ulaznica.narudzbe.NarudzbaService.Narudzba;
import hr.span.akademija.ulaznica.narudzbe.NarudzbaService.NovaNarudzba;
import hr.span.akademija.ulaznica.platforma.ApiException;
import hr.span.akademija.ulaznica.platforma.PrijavljeniKorisnik;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/narudzbe")
public class NarudzbaController {

    private final NarudzbaService narudzbe;

    public NarudzbaController(NarudzbaService narudzbe) {
        this.narudzbe = narudzbe;
    }

    @PostMapping
    public ResponseEntity<Narudzba> kreiraj(@RequestBody NovaNarudzba zahtjev, PrijavljeniKorisnik korisnik) {
        long id = narudzbe.kreiraj(zahtjev, korisnik);
        Narudzba narudzba = narudzbe.pronadi(id)
                .orElseThrow(() -> ApiException.nijePronadeno("Narudžba nije pronađena."));
        return ResponseEntity.status(HttpStatus.CREATED).body(narudzba);
    }

    @GetMapping("/moje")
    public List<Narudzba> moje(PrijavljeniKorisnik korisnik) {
        return narudzbe.narudzbeKupca(korisnik.username());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> storniraj(@PathVariable long id, PrijavljeniKorisnik korisnik) {
        narudzbe.storniraj(id, korisnik);
        return ResponseEntity.noContent().build();
    }
}
