# **FINT Core Dynamisk Adapter**

En tjeneste til å dekke ønsket om relevant mock-data for oss utviklere som jobber med FINT
informasjonsmodellen.

## **Tjenesten består enn så lenge av 3 hoved-moduler:**

### - [core-lib](#core-lib-data-generering)

Ren kotlin. Kun hovedfunksjonen uten hjelpefunksjoner.

### - [core-api](#core-api-rest-eksponering)

Spring Boot REST api som gir tilgang til datagenereringsfunksjonen og hjelpefunksjoner fra metamodellen.

### - [core-ui](#)

AndroidX basert UI verktøy som åpner ett Java vindu på maskinen din, og gir deg tilgang til
hovedfunskjonalitet og metamodel- hjelpefunksjoner uten å trenge å skrive kode.

# **core-lib: Data Generering**

core-lib inneholder DynamiskAdapterService, som har **create()** funksjonen som gjør alt dette mulig.
Denne kan alene importeres inn i og brukes av hvilke som helst prosjekt.

`DynamiskAdapterService.create()` tar inn 2 inputs:

- `resource: Class<out FintResource>` = FintResource java klassen du ønsker å generere. f.eks
  `FravarsregistreringResource::class.java`.
- `count: Int` = antallet ressurser du vil generere.

Funksjonen returnerer en liste med ønsket antall dynamisk genererte resurser. `List<FintResource>`

I tester kan den for eksempel implimenteres slik:

```
val service = DynamicAdapterService()

val tolvElever = service.create(ElevResource::class.java, 12)
```

# **core-api: REST Eksponering**

core-api kjører opp ett simpelt REST api som gjør funksjonaliteten lettere tilgjengelig.
API-et kjøres opp på `localhost:8182`. Rest api-et tar nytte av
[fint-core-consumer-metamodel](https://github.com/FINTLabs/fint-core-consumer-metamodel).

## Gyldige API kall:

### - create: (POST)

- Parameter:
    - component (f.eks utdanning.vurdering)
    - resource (f.eks fravarsregistrering)
    - count (f.eks 2)

Bruker metamodellen til å finne rette FintResource klassen fra dette, og returnerer
listen med etterspurte ressurser direkte.

`http://localhost:8182/create?component=utdanning.vurdering&resource=fravarsregistrering&count=2`

### - getAllComponents (GET)

Trenger ingen parameter. Returnerer en liste av alle FINT Komponenter.

`http://localhost:8182/getAllComponents`

### - getResources (GET)

- Parameter:
    - component (f.eks utdanning.vurdering)

Tar inn komponent og returnerer liste av ressursene som er tilgjengelig for denne komponenten.

`http://localhost:8182/getResources?component=utdanning.vurdering`

### - Ping (GET)

Ingen parameter. Sjekker at backend er tilgjengelig. Returnerer `"ok"`

