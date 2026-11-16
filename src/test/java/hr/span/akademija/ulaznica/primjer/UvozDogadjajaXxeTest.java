package hr.span.akademija.ulaznica.primjer;

import com.sun.net.httpserver.HttpServer;
import hr.span.akademija.ulaznica.ApiTestBaza;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("funkcionalno")
class UvozDogadjajaXxeTest extends ApiTestBaza {

    @Test
    @DisplayName("Uvoz XML-a ne šalje zahtjev vanjskom entitetu")
    void vanjskiEntitetNePokreceHttpZahtjev() throws Exception {
        AtomicInteger brojZahtjeva = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/xxe-test", zahtjev -> {
            brojZahtjeva.incrementAndGet();
            byte[] odgovor = "XXE_PROBE".getBytes(StandardCharsets.UTF_8);
            zahtjev.sendResponseHeaders(200, odgovor.length);
            try (var tijelo = zahtjev.getResponseBody()) {
                tijelo.write(odgovor);
            }
        });
        server.start();

        try {
            String xml = """
                    <!DOCTYPE dogadjaji [
                      <!ENTITY xxe SYSTEM "http://127.0.0.1:%d/xxe-test">
                    ]>
                    <dogadjaji>
                      <dogadjaj>
                        <naziv>&xxe;</naziv>
                        <mjesto>Zagreb</mjesto>
                        <datum>2026-12-12T20:00</datum>
                        <cijena>25.00</cijena>
                        <kapacitet>300</kapacitet>
                      </dogadjaj>
                    </dogadjaji>
                    """.formatted(server.getAddress().getPort());
            String token = prijava("organizator", "Ovlasteni12345!");

            Odgovor rezultat = posalji("POST", "/api/dogadjaji/uvoz", token, "application/xml", xml);

            assertThat(rezultat.status()).isEqualTo(400);
            assertThat(brojZahtjeva.get())
                    .as("XML parser ne smije dohvatiti vanjski entitet preko HTTP-a")
                    .isZero();
        } finally {
            server.stop(0);
        }
    }
}