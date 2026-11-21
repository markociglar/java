package hr.span.akademija.ulaznica.dogadjaji;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dogadjaji")
public class DogadjajZapis {

    @Id
    private Long id;
    private String naziv;
    private String mjesto;
    private LocalDateTime datum;
    private BigDecimal cijena;
    private int kapacitet;
    private int prodano;

    protected DogadjajZapis() {
    }

    public long getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getMjesto() {
        return mjesto;
    }

    public LocalDateTime getDatum() {
        return datum;
    }

    public BigDecimal getCijena() {
        return cijena;
    }

    public int getKapacitet() {
        return kapacitet;
    }

    public int getProdano() {
        return prodano;
    }
}
