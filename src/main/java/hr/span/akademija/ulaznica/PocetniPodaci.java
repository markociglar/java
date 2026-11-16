package hr.span.akademija.ulaznica;

import hr.span.akademija.ulaznica.platforma.KorisnikService;
import hr.span.akademija.ulaznica.platforma.Pohrana;
import hr.span.akademija.ulaznica.racuni.RacunService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Početni podaci (baza je u memoriji pa se puni pri svakom pokretanju). Lozinke se spremaju istom
 * metodom kao pri registraciji.
 */
@Component
public class PocetniPodaci implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PocetniPodaci.class);

    private final JdbcTemplate jdbc;
    private final KorisnikService korisnici;
    private final Pohrana pohrana;

    public PocetniPodaci(JdbcTemplate jdbc, KorisnikService korisnici, Pohrana pohrana) {
        this.jdbc = jdbc;
        this.korisnici = korisnici;
        this.pohrana = pohrana;
    }

    @Override
    public void run(String... args) throws Exception {
        pohrana.pripremi();
        pohrana.zapisiAkoNePostoji("racun-ana-2026-0001.txt", RacunService.sadrzaj(1, "ana",
                "Gibonni – akustični koncert", 1, new BigDecimal("35.00"), "AUTH-5000"));
        pohrana.zapisiAkoNePostoji("racun-marko-2026-0002.txt", RacunService.sadrzaj(2, "marko",
                "Hamlet", 2, new BigDecimal("44.00"), "AUTH-5001"));
        Integer postojeci = jdbc.queryForObject("SELECT COUNT(*) FROM korisnici", Integer.class);
        if (postojeci != null && postojeci > 0) {
            return;
        }
        korisnici.kreiraj("ana", "Ana12345!", "ana@primjer.hr", "Ana", "Anić", "KORISNIK", "48291736504");
        korisnici.kreiraj("marko", "Marko12345!", "marko@primjer.hr", "Marko", "Marić", "KORISNIK", "73019485267");
        korisnici.kreiraj("organizator", "Ovlasteni12345!", "organizator@primjer.hr", "Nina", "Novak",
                "ORGANIZATOR", "50617283940");
        korisnici.kreiraj("admin", "Admin12345!", "admin@primjer.hr", "Ivana", "Admin", "ADMIN", "61728394051");

        dogadjaj("Gibonni – akustični koncert", "Lisinski, Zagreb", "2026-12-05T20:00", "35.00", 1800, 1);
        dogadjaj("Hamlet", "HNK Zagreb", "2026-12-08T19:30", "22.00", 700, 2);
        dogadjaj("Jazz večer u Tvornici", "Tvornica kulture, Zagreb", "2026-12-10T21:00", "18.00", 600, 0);
        dogadjaj("Orašar – balet", "HNK Split", "2026-12-15T19:00", "28.00", 650, 0);
        dogadjaj("Zagrebačka filharmonija – zadnja ulaznica", "Lisinski, Zagreb", "2026-12-18T19:30", "30.00",
                100, 99);
        dogadjaj("Božićni koncert", "Katedrala, Đakovo", "2026-12-20T18:00", "15.00", 400, 0);
        dogadjaj("Stand-up večer", "Kerempuh, Zagreb", "2027-01-10T20:00", "20.00", 300, 0);
        dogadjaj("Novogodišnji koncert", "Arena Pula", "2026-12-31T22:00", "45.00", 5000, 0);

        narudzba(1, "ana", 1, "Ana Anić", "48291736504", "35.00", "AUTH-5000");
        narudzba(2, "marko", 2, "Marko Marić", "73019485267", "44.00", "AUTH-5001");

        racun(1, "ana", "racun-ana-2026-0001.txt");
        racun(2, "marko", "racun-marko-2026-0002.txt");
        log.info("Početni podaci su učitani");
    }

    private void dogadjaj(String naziv, String mjesto, String datum, String cijena, int kapacitet, int prodano) {
        jdbc.update("INSERT INTO dogadjaji (naziv, mjesto, datum, cijena, kapacitet, prodano) VALUES (?, ?, ?, ?, ?, ?)",
                naziv, mjesto, LocalDateTime.parse(datum), new BigDecimal(cijena), kapacitet, prodano);
    }

    private void narudzba(long dogadjajId, String kupac, int kolicina, String imeNaUlaznici, String oib,
                          String iznos, String autorizacija) {
        jdbc.update("INSERT INTO narudzbe (dogadjaj_id, kupac, kolicina, ime_na_ulaznici, oib, iznos, kreirano, "
                        + "autorizacija) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                dogadjajId, kupac, kolicina, imeNaUlaznici, oib, new BigDecimal(iznos), LocalDateTime.now(),
                autorizacija);
    }

    private void racun(long narudzbaId, String kupac, String datoteka) {
        jdbc.update("INSERT INTO racuni (narudzba_id, kupac, datoteka) VALUES (?, ?, ?)", narudzbaId, kupac, datoteka);
    }
}
