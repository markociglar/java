package hr.span.akademija.ulaznica.sigurnost;

import static org.assertj.core.api.Assertions.assertThat;

import hr.span.akademija.ulaznica.ApiTestBaza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("sigurnost")
class PostavkeControllerSigurnostTest extends ApiTestBaza {

    @Test
    @DisplayName("YAML ne konstruira proizvoljnu Java klasu prije validacije")
    void yamlNeKonstruiraJavaObjekte() {
        YamlConstructionMarker.KONSTRUIRAN.set(false);
        String yaml = """
                podsjetnik: true
                satiPrije: 24
                kanal: !!hr.span.akademija.ulaznica.sigurnost.YamlConstructionMarker {}
                """;

        String token = token("ana");
        Odgovor odgovor = posalji("POST", "/api/postavke", token, "application/yaml", yaml);

        assertThat(odgovor.status()).isEqualTo(400);
        assertThat(YamlConstructionMarker.KONSTRUIRAN.get()).isFalse();
    }
}
