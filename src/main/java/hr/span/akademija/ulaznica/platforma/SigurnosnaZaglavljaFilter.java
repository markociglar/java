package hr.span.akademija.ulaznica.platforma;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Sigurnosna zaglavlja za HTML stranice i API odgovore. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SigurnosnaZaglavljaFilter extends OncePerRequestFilter {

    private static final String CSP = "default-src 'none'; style-src 'unsafe-inline'; "
            + "form-action 'self'; base-uri 'none'; frame-ancestors 'none'";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Content-Security-Policy", CSP);
        chain.doFilter(request, response);
    }
}
