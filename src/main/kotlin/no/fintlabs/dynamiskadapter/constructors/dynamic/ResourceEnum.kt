package no.fintlabs.dynamiskadapter.constructors.dynamic

import no.fint.model.FintModelObject
import no.fint.model.utdanning.elev.Basisgruppe
import no.fint.model.utdanning.elev.Basisgruppemedlemskap
import no.fint.model.utdanning.elev.Elev
import no.fint.model.utdanning.elev.Elevforhold
import no.fint.model.utdanning.elev.Elevtilrettelegging
import no.fint.model.utdanning.elev.Kontaktlarergruppe
import no.fint.model.utdanning.elev.Kontaktlarergruppemedlemskap
import no.fint.model.utdanning.elev.Persongruppe
import no.fint.model.utdanning.elev.Persongruppemedlemskap
import no.fint.model.utdanning.elev.Skoleressurs
import no.fint.model.utdanning.elev.Undervisningsforhold
import no.fint.model.utdanning.kodeverk.Avbruddsarsak
import no.fint.model.utdanning.kodeverk.Betalingsstatus
import no.fint.model.utdanning.kodeverk.Bevistype
import no.fint.model.utdanning.kodeverk.Brevtype
import no.fint.model.utdanning.kodeverk.Eksamensform
import no.fint.model.utdanning.kodeverk.Elevkategori
import no.fint.model.utdanning.kodeverk.Fagmerknad
import no.fint.model.utdanning.kodeverk.Fagstatus
import no.fint.model.utdanning.kodeverk.Fravarstype
import no.fint.model.utdanning.kodeverk.Fullfortkode
import no.fint.model.utdanning.kodeverk.Karakterskala
import no.fint.model.utdanning.kodeverk.Karakterstatus
import no.fint.model.utdanning.kodeverk.OtEnhet
import no.fint.model.utdanning.kodeverk.OtStatus
import no.fint.model.utdanning.kodeverk.Provestatus
import no.fint.model.utdanning.kodeverk.Skolear
import no.fint.model.utdanning.kodeverk.Skoleeiertype
import no.fint.model.utdanning.kodeverk.Termin
import no.fint.model.utdanning.kodeverk.Tilrettelegging
import no.fint.model.utdanning.kodeverk.Varseltype
import no.fint.model.utdanning.kodeverk.Vitnemalsmerknad
import no.fint.model.utdanning.larling.AvlagtProve
import no.fint.model.utdanning.larling.Larling
import no.fint.model.utdanning.ot.OtUngdom
import no.fint.model.utdanning.timeplan.Eksamen
import no.fint.model.utdanning.timeplan.Fag
import no.fint.model.utdanning.timeplan.Faggruppe
import no.fint.model.utdanning.timeplan.Faggruppemedlemskap
import no.fint.model.utdanning.timeplan.Rom
import no.fint.model.utdanning.timeplan.Time
import no.fint.model.utdanning.timeplan.Undervisningsgruppe
import no.fint.model.utdanning.timeplan.Undervisningsgruppemedlemskap
import no.fint.model.utdanning.utdanningsprogram.Arstrinn
import no.fint.model.utdanning.utdanningsprogram.Programomrade
import no.fint.model.utdanning.utdanningsprogram.Programomrademedlemskap
import no.fint.model.utdanning.utdanningsprogram.Skole
import no.fint.model.utdanning.utdanningsprogram.Utdanningsprogram
import no.fint.model.utdanning.vurdering.Anmerkninger
import no.fint.model.utdanning.vurdering.Eksamensgruppe
import no.fint.model.utdanning.vurdering.Eksamensgruppemedlemskap
import no.fint.model.utdanning.vurdering.Eksamensvurdering
import no.fint.model.utdanning.vurdering.Elevfravar
import no.fint.model.utdanning.vurdering.Elevvurdering
import no.fint.model.utdanning.vurdering.Fravarsoversikt
import no.fint.model.utdanning.vurdering.Fravarsregistrering
import no.fint.model.utdanning.vurdering.Halvarsfagvurdering
import no.fint.model.utdanning.vurdering.Halvarsordensvurdering
import no.fint.model.utdanning.vurdering.Karakterhistorie
import no.fint.model.utdanning.vurdering.Karakterverdi
import no.fint.model.utdanning.vurdering.Sensor
import no.fint.model.utdanning.vurdering.Sluttfagvurdering
import no.fint.model.utdanning.vurdering.Sluttordensvurdering
import no.fint.model.utdanning.vurdering.Underveisfagvurdering
import no.fint.model.utdanning.vurdering.Underveisordensvurdering

enum class ResourceEnum(
    val clazz: Class<out FintModelObject>,
) {
    UTDANNING_VURDERING_ANMERKNINGER(Anmerkninger::class.java),
    UTDANNING_KODEVERK_AVBRUDDSARSAK(Avbruddsarsak::class.java),
    UTDANNINV_LARLING_AVLAGTPROVE(AvlagtProve::class.java),
    UTDANNING_ELEV_BASISGRUPPE(Basisgruppe::class.java),
    UTDANNING_ELEV_BASISGRUPPEMEDLEMSKAP(Basisgruppemedlemskap::class.java),
    UTDANNING_KODEVERK_BETALINGSSTATUS(Betalingsstatus::class.java),
    UTDANNING_KODEVERK_BEVISTYPE(Bevistype::class.java),
    UTDANNING_KODEVERK_BREVTYPE(Brevtype::class.java),
    UTDANNING_TIMEPLAN_EKSAMEN(Eksamen::class.java),
    UTDANNING_KODEVERK_EKSAMENSFORM(Eksamensform::class.java),
    UTDANNING_VURDERING_EKSAMENSGRUPPE(Eksamensgruppe::class.java),
    UTDANNING_VURDERING_EKSAMENSGRUPPEMEDLEMSKAP(Eksamensgruppemedlemskap::class.java),
    UTDANNING_VURDERING_EKSAMENSVURDERING(Eksamensvurdering::class.java),
    UTDANNING_ELEV_ELEV(Elev::class.java),
    UTDANNING_ELEV_ELEVFORHOLD(Elevforhold::class.java),
    UTDANNING_VURDERING_ELEVFRAVAR(Elevfravar::class.java),
    UTDANNING_KODEVERK_ELEVKATEGORI(Elevkategori::class.java),
    UTDANNING_ELEV_ELEVTILRETTELEGGING(Elevtilrettelegging::class.java),
    UTDANNING_VURDERING_ELEVVURDERING(Elevvurdering::class.java),
    UTDANNING_TIMEPLAN_FAG(Fag::class.java),
    UTDANNING_TIMEPLAN_FAGGRUPPE(Faggruppe::class.java),
    UTDANNING_TIMEPLAN_FAGGRUPPEMEDLEMSKAP(Faggruppemedlemskap::class.java),
    UTDANNING_KODEVERK_FAGMERKNAD(Fagmerknad::class.java),
    UTDANNING_KODEVERK_FAGSTATUS(Fagstatus::class.java),
    UTDANNING_VURDERING_FRAVARSOVERSIKT(Fravarsoversikt::class.java),
    UTDANNING_VURDERING_FRAVARSREGISTRERING(Fravarsregistrering::class.java),
    UTDANNING_KODEVERK_FRAVARSTYPE(Fravarstype::class.java),
    UTDANNING_KODEVERK_FULLFORTKODE(Fullfortkode::class.java),
    UTDANNING_VURDERING_HALVARSFAGVURDERING(Halvarsfagvurdering::class.java),
    UTDANNING_VURDERING_HALVARSORDENSVURDERING(Halvarsordensvurdering::class.java),
    UTDANNING_VURDERING_KARAKTERHISTORIE(Karakterhistorie::class.java),
    UTDANNING_KODEVERK_KARAKTERSKALA(Karakterskala::class.java),
    UTDANNING_KODEVERK_KARAKTERSTATUS(Karakterstatus::class.java),
    UTDANNING_VURDERING_KARAKTERVERDI(Karakterverdi::class.java),
    UTDANNING_ELEV_KONTAKTLARERGRUPPE(Kontaktlarergruppe::class.java),
    UTDANNING_ELEV_KONTAKTLARERGRUPPEMEDLEMSKAP(Kontaktlarergruppemedlemskap::class.java),
    UTDANNING_LARLING_LARLING(Larling::class.java),
    UTDANNING_KODEVERK_OTENHET(OtEnhet::class.java),
    UTDANNING_KODEVERK_OTSTATUS(OtStatus::class.java),
    UTDANNING_OT_OTUNGDOM(OtUngdom::class.java),
    UTDANNING_ELEV_PERSONGRUPPE(Persongruppe::class.java),
    UTDANNING_ELEV_PERSONGRUPPEMEDLEMSKAP(Persongruppemedlemskap::class.java),
    UTDANNING_UTDANNINGSPROGRAM_PROGRAMOMRADE(Programomrade::class.java),
    UTDANNING_UTDANNINGSPROGRAM_PROGRAMOMRADEMEDLEMSKAP(Programomrademedlemskap::class.java),
    UTDANNING_KODEVERK_PROVESTATUS(Provestatus::class.java),
    UTDANNING_TIMEPLAN_ROM(Rom::class.java),
    UTDANNING_VURDERING_SENSOR(Sensor::class.java),
    UTDANNING_UTDANNINGSPROGRAM_SKOLE(Skole::class.java),
    UTDANNING_KODEVERK_SKOLEEIERTYPE(Skoleeiertype::class.java),
    UTDANNING_ELEV_SKOLERESSURS(Skoleressurs::class.java),
    UTDANNING_KODEVERK_SKOLEAR(Skolear::class.java),
    UTDANNING_VURDERING_SLUTTFAGVURDERING(Sluttfagvurdering::class.java),
    UTDANNING_VURDERING_SLUTTORDENSVURDERING(Sluttordensvurdering::class.java),
    UTDANNING_KODEVERK_TERMIN(Termin::class.java),
    UTDANNING_KODEVERK_TILRETTELEGGING(Tilrettelegging::class.java),
    UTDANNING_TIMEPLAN_TIME(Time::class.java),
    UTDANNING_VURDERING_UNDERVEISFAGVURDERING(Underveisfagvurdering::class.java),
    UTDANNING_VURDERING_UNDERVEISORDENSVURDERING(Underveisordensvurdering::class.java),
    UTDANNING_ELEV_UNDERVISNINGSFORHOLD(Undervisningsforhold::class.java),
    UTDANNING_TIMEPLAN_UNDERVISNINGSGRUPPE(Undervisningsgruppe::class.java),
    UTDANNING_TIMEPLAN_UNDERVISNINGSGRUPPEMEDLEMSKAP(Undervisningsgruppemedlemskap::class.java),
    UTDANNING_UTDANNINGSPROGRAM_UTDANNINGSPROGRAM(Utdanningsprogram::class.java),
    UTDANNING_KODEVERK_VARSELTYPE(Varseltype::class.java),
    UTDANNING_KODEVERK_VITNEMALSMERKNAD(Vitnemalsmerknad::class.java),
    UTDANNING_UTDANNINGSPROGRAM_ARSTRINN(Arstrinn::class.java),
}
