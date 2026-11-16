package hr.span.akademija.ulaznica.racuni;

import hr.span.akademija.ulaznica.platforma.JdbcPomoc;
import hr.span.akademija.ulaznica.platforma.Pohrana;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** Izdavanje računa: tekstualna datoteka u {@code app.storage-dir} i zapis u tablici {@code racuni}. */
@Service
public class RacunService {

    private final JdbcTemplate jdbc;
    private final Pohrana pohrana;

    public RacunService(JdbcTemplate jdbc, Pohrana pohrana) {
        this.jdbc = jdbc;
        this.pohrana = pohrana;
    }

    public static String nazivDatoteke(String kupac, long narudzbaId) {
        return "racun-" + kupac + "-" + LocalDate.now().getYear() + "-" + String.format("%04d", narudzbaId) + ".txt";
    }

    public static String sadrzaj(long narudzbaId, String kupac, String dogadjaj, int kolicina, BigDecimal iznos,
                                 String autorizacija) {
        return """
                Ulaznica d.o.o. – račun za narudžbu %d
                Kupac: %s
                Događaj: %s
                Količina: %d
                Ukupno: %s EUR
                Autorizacija plaćanja: %s
                Hvala na kupnji!
                """.formatted(narudzbaId, kupac, dogadjaj, kolicina, iznos.toPlainString(), autorizacija);
    }

    public long izdaj(long narudzbaId, String kupac, String dogadjaj, int kolicina, BigDecimal iznos,
                      String autorizacija) {
        String datoteka = nazivDatoteke(kupac, narudzbaId);
        try {
            Files.writeString(pohrana.dokumenti().resolve(datoteka),
                    sadrzaj(narudzbaId, kupac, dogadjaj, kolicina, iznos, autorizacija), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return JdbcPomoc.umetni(jdbc, "INSERT INTO racuni (narudzba_id, kupac, datoteka) VALUES (?, ?, ?)",
                narudzbaId, kupac, datoteka);
    }
}
