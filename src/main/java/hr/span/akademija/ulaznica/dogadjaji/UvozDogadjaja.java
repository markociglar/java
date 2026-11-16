package hr.span.akademija.ulaznica.dogadjaji;

import hr.span.akademija.ulaznica.platforma.ApiException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Čita XML s događajima koji šalju organizatori:
 * <pre>
 * &lt;dogadjaji&gt;&lt;dogadjaj&gt;&lt;naziv&gt;Koncert&lt;/naziv&gt;&lt;mjesto&gt;Lisinski&lt;/mjesto&gt;
 * &lt;datum&gt;2026-12-12T20:00&lt;/datum&gt;&lt;cijena&gt;25.00&lt;/cijena&gt;&lt;kapacitet&gt;300&lt;/kapacitet&gt;
 * &lt;/dogadjaj&gt;&lt;/dogadjaji&gt;
 * </pre>
 */
@Component
public class UvozDogadjaja {

    public record Stavka(String naziv, String mjesto, LocalDateTime datum, BigDecimal cijena, int kapacitet) {
    }

    public List<Stavka> procitaj(byte[] xml) {
        Document dokument;
        try {
            DocumentBuilder builder = tvornica().newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());
            dokument = builder.parse(new ByteArrayInputStream(xml));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw ApiException.neispravno("Neispravan XML.");
        }
        NodeList elementi = dokument.getElementsByTagName("dogadjaj");
        List<Stavka> stavke = new ArrayList<>();
        for (int i = 0; i < elementi.getLength(); i++) {
            Element dogadjaj = (Element) elementi.item(i);
            try {
                stavke.add(new Stavka(
                        tekst(dogadjaj, "naziv"),
                        tekst(dogadjaj, "mjesto"),
                        LocalDateTime.parse(tekst(dogadjaj, "datum")),
                        new BigDecimal(tekst(dogadjaj, "cijena")),
                        Integer.parseInt(tekst(dogadjaj, "kapacitet"))));
            } catch (RuntimeException e) {
                throw ApiException.neispravno("Neispravni podaci u događaju " + (i + 1) + ".");
            }
        }
        return stavke;
    }

    private static DocumentBuilderFactory tvornica() {
        DocumentBuilderFactory tvornica = DocumentBuilderFactory.newInstance();
        tvornica.setNamespaceAware(true);
        /*try {
            tvornica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            tvornica.setFeature("http://xml.org/sax/features/external-general-entities", false);
            tvornica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            tvornica.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            tvornica.setXIncludeAware(false);
            tvornica.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Sigurna XML obrada nije dostupna.", e);
        }*/
        return tvornica;
    }

    private static String tekst(Element roditelj, String naziv) {
        NodeList elementi = roditelj.getElementsByTagName(naziv);
        String vrijednost = elementi.getLength() == 0 ? null : elementi.item(0).getTextContent().trim();
        if (vrijednost == null || vrijednost.isEmpty()) {
            throw new IllegalArgumentException("Nedostaje element " + naziv);
        }
        return vrijednost;
    }
}
