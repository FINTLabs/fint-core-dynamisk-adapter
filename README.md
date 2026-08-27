# **DYNA: Dynamisk Adapter**

En tjeneste til å dekke ønsket om relevant mock-data for oss utviklere som jobber med FINT
informasjonsmodellen.

Det som startet med ett ønske om ett *relativt enkelt* blbliotek er nå en multi-modul monolitt med evnen til å
etterligne en ekte levende fylkeskommune. Ikke nok med det, men den kan konfigureres og styres for å fylle
hver eneste use-case du kan ønske å teste for eller med.*

*bortsett fra EVENTS. Det er ikke implementert per dags dato.

## **Tjenesten består enn så lenge av 2 hoved-moduler:**

### - [core-lib](#core-lib-data-generering)

Ren kotlin, kun ren, direkte returnert datagenerering.

### Nyeste versjon: 3.21.16

### - [core-api](#core-api-rest-eksponering)

Resten av Dynamisk Adapter applikasjonen kjøres opp fra core-api modulen.
Det er også i denne modulens application.yaml fil all konfigurasjon spesifiseres*.
Ved oppstart

*Environment variabler må også settes opp utenom disse for at applikasjonen skal kunne kjøre uten
`fint.adapter.offline-mode = true`.
Se intern dokumentasjon for hvordan å fylle inn disse.

# **core-lib: Enkelt implementerbart bibliotek for ressursgenerering**

core-lib inneholder ResourceFactory klassen, som har **create()** funksjonen som gjør alt dette mulig.
Denne kan alene importeres inn i og brukes av hvilke som helst prosjekt.

`ResourceFactory.create()` tar inn 2 inputs:

- `resource: Class<out FintResource>` = FintResource java klassen du ønsker å generere. f.eks
  `FravarsregistreringResource::class.java`.
- `count: Int` = antallet ressurser du vil generere.
- `logging: Boolean = false` er også en optional variabel som kan settes som `true` dersom du føler det kan være
  hjelpsomt.

Funksjonen returnerer en liste med ønsket antall dynamisk genererte resurser. `List<FintResource>`

*navnet på hovedklassen ble i oppdatering `dwasd` byttet fra `DynamicAdapterService` til `ResourceFactory`*

## Implimentasjon i tester

build.gradle.kts:

```
repositories {
    mavenCentral()
    maven("https://repo.fintlabs.no/releases")
}

dependencies {
// andre dependencies //

testImplementation("no.fintlabs:dynamisk-adapter-core-lib:latest.release")

}

```

Test implimentasjon eksempel:

```
val factory = ResourceFactory()

val tolvElever = factory.create(ElevResource::class.java, 12)
```

*navnet på hovedklassen ble i oppdatering `dwasd` byttet fra `DynamicAdapterService` til `ResourceFactory`.*

Ønsker du å spesifisere en verdi i ressursen du genererer er dette også mulig:

```
val sivert = factory
      .createWithSingleSpecifiedValue(
          resource = ElevResource::class.java, 
          fieldName = "brukernavn",
          fieldValue = "Sivert", 
          amount = 1)
```

Dette kan gjøres for felter av typene: `String`, `Int`, `Long` og `Identifikator`.
Feltverdien må sendes inn i form av en `String`, men gjøres om til den korrekte typen under genereringen.
Etterspør du flere ressurser vil kun spesifiserte feltet være likt. Alle andre vil være tilfeldig generert.