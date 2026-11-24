package hr.span.akademija.ulaznica.platforma;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/** Parametar tipa {@link PrijavljeniKorisnik} u kontroleru znači da endpoint traži prijavu (inače 401). */
@Component
public class PrijavljeniKorisnikResolver implements HandlerMethodArgumentResolver {

    private final KorisnikService korisnici;

    public PrijavljeniKorisnikResolver(KorisnikService korisnici) {
        this.korisnici = korisnici;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return PrijavljeniKorisnik.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Object korisnik = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt) {
            String uloga = auth.getAuthorities().stream().map(a -> a.getAuthority())
                    .filter(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_ORGANIZATOR")
                            || a.equals("ROLE_KORISNIK"))
                    .map(a -> a.substring(5)).findFirst().orElse("");
            korisnik = korisnici.pronadi(auth.getName())
                    .map(k -> new PrijavljeniKorisnik(k.id(), auth.getName(), uloga)).orElse(null);
        }
        if (korisnik == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Potrebna je prijava.");
        }
        return korisnik;
    }
}
