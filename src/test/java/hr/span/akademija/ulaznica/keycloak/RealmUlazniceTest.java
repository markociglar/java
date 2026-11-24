package hr.span.akademija.ulaznica.keycloak;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RealmUlazniceTest {
    @Test
    void realmImaTrazeneKorisnikeIUloge() throws Exception {
        JsonNode realm = new ObjectMapper().readTree(Files.readString(Path.of("realm-ulaznice.json")));
        assertThat(realm.path("realm").asText()).isEqualTo("ulaznice");

        Map<String, String[]> korisnici = Map.of(
                "ana", new String[] {"Ana12345!", "KORISNIK"},
                "marko", new String[] {"Marko12345!", "KORISNIK"},
                "organizator", new String[] {"Ovlasteni12345!", "ORGANIZATOR"},
                "admin", new String[] {"Admin12345!", "ADMIN"});
        assertThat(realm.path("users").size()).isEqualTo(korisnici.size());
        for (JsonNode user : realm.path("users")) {
            String[] ocekivano = korisnici.get(user.path("username").asText());
            assertThat(ocekivano).isNotNull();
            assertThat(user.path("credentials").get(0).path("value").asText()).isEqualTo(ocekivano[0]);
            assertThat(user.path("realmRoles").toString()).contains(ocekivano[1]);
        }
        assertThat(realm.path("clients").toString())
                .contains("ulaznice-api", "ulaznice-spa", "ulaznice-dev-cli", "audience-ulaznice-api");
    }
}
