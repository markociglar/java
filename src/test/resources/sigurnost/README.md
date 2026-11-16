# Sigurnosni testovi i Postman upute

Svaka `.txt` datoteka opisuje jedan test iz paketa `hr.span.akademija.ulaznica.sigurnost`.
Pokreći aplikaciju samo lokalno. U Postmanu napravi environment s `baseUrl = http://localhost:8080`.

API prijava: `POST {{baseUrl}}/api/auth/login`, Body → raw → JSON:

```json
{"username":"ana","password":"Ana12345!"}
```

U odgovoru kopiraj `token`. Za zaštićene zahtjeve postavi Authorization → Bearer Token i zalijepi token.
Za `marko` koristi `Marko12345!`, a za `organizator` `Ovlasteni12345!`.
Možeš spremiti token u aktivni Postman environment u Scripts → Post-response:

```javascript
pm.environment.set("anaToken", pm.response.json().token);
```

Zatim u Bearer Token polje unesi `{{anaToken}}`. Slično spremi `markoToken` i `organizatorToken`.
Na zahtjevima gdje se želi provjeriti prijava kolačićem postavi Authorization → No Auth.

Pokretanje samo sigurnosnih Java testova iz mape `java/`:

```text
./mvnw -Dtest=*SigurnostTest test
```

Na Windowsu koristi `mvnw.cmd`. Potreban je JDK 21 (`JAVA_HOME` mora pokazivati na JDK 21).
Na izvornom ranjivom kodu testovi namjerno padaju; nakon popravka trebaju proći.
Za obične funkcionalne testove koristi `./mvnw test -Dgroups=funkcionalno`.

Postman upute su pisane prema službenoj dokumentaciji:
- https://learning.postman.com/docs/use/send-requests/authorization/authorization
- https://learning.postman.com/docs/use/send-requests/create-requests/parameters
- https://learning.postman.com/docs/use/send-requests/variables/environment-variables
- https://learning.postman.com/docs/tests-and-scripts/write-scripts/postman-sandbox-reference/pm-send-request

Postman sam po sebi ne prikazuje vrijednosti u bazi ni nastanak Java objekta tijekom deserijalizacije.
Te dvije tvrdnje provjeravaju automatizirani Java testovi, što je posebno naznačeno u pripadajućim uputama.
