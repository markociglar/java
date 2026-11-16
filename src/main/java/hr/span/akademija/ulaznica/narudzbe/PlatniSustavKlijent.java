package hr.span.akademija.ulaznica.narudzbe;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Klijent platnog sustava (autorizacija plaćanja karticom). U lokalnom okruženju platni sustav nije
 * dostupan pa klijent nakon uobičajenog vremena odziva vraća lokalno generiranu oznaku autorizacije.
 */
@Component
public class PlatniSustavKlijent {

    private static final Duration VRIJEME_ODZIVA = Duration.ofMillis(150);

    private final AtomicLong brojac = new AtomicLong(5000);

    public String autoriziraj(String kupac, BigDecimal iznos) {
        try {
            Thread.sleep(VRIJEME_ODZIVA);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Autorizacija plaćanja je prekinuta", e);
        }
        return "AUTH-" + brojac.incrementAndGet();
    }
}
