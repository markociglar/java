package hr.span.akademija.ulaznica.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import org.junit.jupiter.api.Test;

class KeycloakApiTest extends ApiTestBaza {
    @Test
    void bezTokenaIKrivotvoreniTokenSuOdbijeni() {
        assertThat(get("/api/narudzbe/moje", null).status()).isEqualTo(401);
        assertThat(get("/api/narudzbe/moje", "krivotvoren").status()).isEqualTo(401);
    }

    @Test
    void realmUlogeOdredujuPristupUvozu() {
        assertThat(posalji("POST", "/api/dogadjaji/uvoz", token("ana"),
                "application/xml", "<dogadjaji/>").status()).isEqualTo(403);
        assertThat(posalji("POST", "/api/dogadjaji/uvoz", token("organizator"),
                "application/xml", "<dogadjaji/>").status()).isEqualTo(200);
    }

    @Test
    void identitetDolaziIzJwtA() {
        Odgovor odgovor = get("/api/ja", token("ana"));
        assertThat(odgovor.status()).isEqualTo(200);
        assertThat(odgovor.json().get("username").asText()).isEqualTo("ana");
    }
}
