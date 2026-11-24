package hr.span.akademija.ulaznica.platforma;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/** Document storage ({@code app.storage-dir}). */
@Component
public class Pohrana {

    private final Path dokumenti;

    public Pohrana(AppProperties postavke) {
        this.dokumenti = Path.of(postavke.storageDir());
    }

    public Path dokumenti() {
        return dokumenti;
    }

    public void pripremi() throws IOException {
        Files.createDirectories(dokumenti);
    }

    public void zapisiAkoNePostoji(String naziv, String sadrzaj) throws IOException {
        Path datoteka = dokumenti.resolve(naziv);
        if (!Files.exists(datoteka)) {
            Files.writeString(datoteka, sadrzaj, StandardCharsets.UTF_8);
        }
    }
}
