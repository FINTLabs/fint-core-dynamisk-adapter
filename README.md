# Fint Core Dynamisk Adapter

## V1 Complete: 

Dette er en tjeneste designet og utviklet for intern bruk av utviklere i Novari IKS, 
for å tilfredsstille ett relativt lett ønske. 

Denne tjenesten har per nå evnen til å produsere mock data av: 
- utdanning-elev
- utdanning-elev-person
- utdanning-elev-elevforhold
- utdanning-vurdering-elevfravær
- utdanning-vurdering-fravarsregistrering

Hovedgruppene er:

- utdanning-elev
- utdanning-vurdering-fravarsregistrering

Når disse er etterspurt produseres resursen du etterspør (av disse hovedgruppene) 
og resursene de avhenger av. I UI-en har du mulighet til å styre: 
- orgId
- domainContext
- antall resurser
- resurs

Under produksjon av Mock data blir disse tatt i betraktning hvert steg av veien, og all data sendes 
til riktig Kafka Topic. 
Linker blir produsert i prosessen, og oppdaterer begge resursene som skal linkes mellom med en riktig 
link til den andre. 

Du kan ikke produsere fravarsregistreringer uten elever. Når du har produsert elever vil hver fravarsregistrering 
kobles til en tilfeldig elev du har produsert, og eleven vil oppdateres til å linke til fravarsregistreringen (indirekte gjennom elevfravar og elevforhold).

Over tid kan den utvikles til å forhåpentligvis støtte alle de største dataene vi håndterer i FINT.
Støtte for forskjellige Informasjonsmodell versjoner kan komme senere. Enn så lenge følger den v3.19.0

### HOW TO RUN: 

- ha docker daemon kjørende på maskinen.
- importer og bygg applikasjonen.
- kjør Application.kt.

## Fremtidige planer: 

- Produsere mock data dynamisk fra informasjonsmodellen ( ikke hardkodede constructors )

Denne skal da kunne produsere hva som helst fra informasjonsmodellen etter etterspørsel. 

- Basic og Recursive

Skal kunne (etter ønske) produsere enten kun etterspurte resurser, 
eller produsere etterspurt resurs plus relaterte resurser. 

- Bibliotek

Separere funksjonene fra nåværende UI, så den funksjonale delen kan importeres i hvilket som helst 
prosjekt og ta nytte av konstruksjonslogikken for mock data i tester. 



