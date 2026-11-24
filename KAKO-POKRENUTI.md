# Ulaznica – kako pokrenuti

Web trgovina ulaznicama za koncerte i predstave: kupnja ulaznica, računi i ispis ulaznica.
Spring Boot 4.1, Java 21, H2 baza u memoriji (podaci se pune pri svakom pokretanju).

## Preduvjeti

- JDK 21 (npr. Eclipse Temurin 21)
- Docker za lokalni Keycloak
- Maven nije potreban: koristi se Maven Wrapper (`./mvnw`, na Windowsu `mvnw.cmd`)
- IntelliJ IDEA ili VS Code (Extension Pack for Java): otvori mapu projekta kao Maven projekt

## Pokretanje

```bash
docker compose up -d
./mvnw spring-boot:run
```

- početna stranica: http://localhost:8080/
- Keycloak korisnička konzola: http://localhost:8180/realms/ulaznice/account
- provjera rada: http://localhost:8080/api/health

Primjeri zahtjeva za REST API nalaze se u `http/ulaznica.http` (IntelliJ HTTP Client, VS Code REST Client).

## Testovi

```bash
./mvnw test                          # svi testovi
./mvnw test -Dgroups=funkcionalno    # funkcionalni testovi
./mvnw test -Dgroups=primjer         # primjer testa
```

Izvještaj o pokrivenosti koda: `target/site/jacoco/index.html`.

Vlastite testove možeš temeljiti na klasi `ApiTestBaza` (pokreće aplikaciju na nasumičnom portu i nudi
potpisane testne JWT-ove te metode za slanje zahtjeva) – pogledaj `keycloak/KeycloakApiTest`.
Testovi rade bez pokrenutog Keycloaka: testni RSA ključ i decoder provjeravaju potpis, `iss`, `aud` i istek.
`KeycloakIntegracijaTest` dodatno provjerava stvarno izdavanje tokena za sva četiri korisnika i pristup API-ju.
Ako lokalni Keycloak nije dostupan, taj se test preskače. Pokreni ga nakon `docker compose up -d`.

## Alati za analizu (neobavezno)

```bash
./mvnw -Psast compile spotbugs:check       # SpotBugs + Find Security Bugs
./mvnw -Psast compile spotbugs:gui         # isto, s grafičkim sučeljem
./mvnw -Psca cyclonedx:makeAggregateBom    # popis ovisnosti (SBOM) u target/bom.json
```

`target/bom.json` možeš provjeriti alatima poput OSV-Scanner ili Trivy.

## Korisnici

| Korisničko ime | Lozinka | Uloga |
|---|---|---|
| ana | `Ana12345!` | KORISNIK |
| marko | `Marko12345!` | KORISNIK |
| organizator | `Ovlasteni12345!` | ORGANIZATOR |
| admin | `Admin12345!` | ADMIN |

API prihvaća Keycloak access token u zaglavlju `Authorization: Bearer <token>`. Provjeravaju se
potpis, izdavatelj (`http://localhost:8180/realms/ulaznice`), rok valjanosti i audience (`ulaznice-api`).
Uloge se čitaju iz `realm_access.roles`, a identitet iz `preferred_username`.
Lokalni `/api/auth/login`, registracija, reset lozinke i obrazac `/prijava` uklonjeni su.
Lozinke i njihove promjene vodi Keycloak; aplikacija čuva samo poslovne podatke korisnika.

Keycloak administracija: `http://localhost:8180/admin` (`kcadmin` / `kcadmin-samo-za-razvoj`).
Web klijent `ulaznice-spa` koristi Authorization Code + PKCE S256 s povratnom adresom
`http://localhost:5173/*`. Za lokalni razvoj postoji `ulaznice-dev-cli` s password grantom:

```bash
curl -X POST http://localhost:8180/realms/ulaznice/protocol/openid-connect/token \
  -d grant_type=password -d client_id=ulaznice-dev-cli \
  -d username=ana --data-urlencode 'password=Ana12345!'
```

Vrijednost `access_token` iz odgovora pošalji u `Authorization` zaglavlju. Keycloak podaci u
`realm-ulaznice.json` služe samo za lokalni razvoj.

## Konfiguracija (`src/main/resources/application.properties`)

| Ključ | Značenje | Zadano |
|---|---|---|
| `app.storage-dir` | mapa s datotekama računa | `./data/dokumenti` |
| `app.cors.allowed-origins` | origini frontend aplikacije | `http://localhost:5173` |
| `spring.datasource.url` | baza | H2 u memoriji |
