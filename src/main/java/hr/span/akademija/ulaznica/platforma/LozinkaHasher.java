package hr.span.akademija.ulaznica.platforma;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/** Izračun i provjera sažetka lozinke (registracija, prijava, promjena lozinke, početni podaci). */
@Component
public class LozinkaHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String lozinka) {
        return encoder.encode(lozinka);
    }

    public boolean provjeri(String lozinka, String spremljeniHash) {
        if (spremljeniHash == null) {
            return false;
        }
        return encoder.matches(lozinka, spremljeniHash);
    }
}
