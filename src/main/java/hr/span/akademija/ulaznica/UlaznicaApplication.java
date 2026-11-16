package hr.span.akademija.ulaznica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class UlaznicaApplication {

    public static void main(String[] args) {
        SpringApplication.run(UlaznicaApplication.class, args);
    }
}
