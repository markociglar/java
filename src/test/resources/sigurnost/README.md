# Sigurnosni testovi i Postman

Pokreni Keycloak iz korijena Java projekta: `docker compose up -d`.
Realm je `ulaznice`. Za lokalno testiranje tokena koristi javni klijent `ulaznice-dev-cli`:

```http
POST http://localhost:8180/realms/ulaznice/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=ulaznice-dev-cli&username=ana&password=Ana12345%21
```

Vrijednost `access_token` stavi u `Authorization: Bearer <access_token>` za zahtjeve na
`http://localhost:8080`. Korisnici su navedeni u `KAKO-POKRENUTI.md`.

Automatizirani Java testovi ne traže Docker. Koriste RS256 potpisane testne tokene s Keycloak
claimovima i provjeravaju potpis, izdavatelja, audience, istek i uloge kroz Spring Security.
Pokreni ih s `./mvnw test`.
