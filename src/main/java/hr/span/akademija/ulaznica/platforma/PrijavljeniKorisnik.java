package hr.span.akademija.ulaznica.platforma;

import java.util.Arrays;

/** User authenticated by a Keycloak Bearer token. */
public record PrijavljeniKorisnik(long id, String username, String uloga) {

    public boolean isAdmin() {
        return "ADMIN".equals(uloga);
    }

    public boolean imaUlogu(String... uloge) {
        return Arrays.asList(uloge).contains(uloga);
    }
}
