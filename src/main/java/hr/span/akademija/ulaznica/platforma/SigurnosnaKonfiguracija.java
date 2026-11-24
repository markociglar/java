package hr.span.akademija.ulaznica.platforma;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SigurnosnaKonfiguracija {

    @Bean
    SecurityFilterChain keycloak(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.GET, "/", "/api/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/dogadjaji/uvoz")
                            .hasAnyRole("ORGANIZATOR", "ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o.jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakJwtKonverter()))
                        .authenticationEntryPoint((req, res, ex) -> greska(res, 401, "Potrebna je prijava."))
                        .accessDeniedHandler((req, res, ex) -> greska(res, 403, "Nemate ovlasti za ovu radnju.")))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> greska(res, 401, "Potrebna je prijava."))
                        .accessDeniedHandler((req, res, ex) -> greska(res, 403, "Nemate ovlasti za ovu radnju.")))
                .build();
    }

    private static void greska(HttpServletResponse res, int status, String poruka) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        if (status == 401) {
            res.setHeader("WWW-Authenticate", "Bearer");
        }
        res.getWriter().write("{\"greska\":\"" + poruka + "\"}");
    }
}
