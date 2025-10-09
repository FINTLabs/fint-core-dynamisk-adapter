package no.fintlabs.dynamiskadapter.constructors.dynamic

import no.fint.model.FintModelObject
import no.fint.model.utdanning.elev.Basisgruppe
import no.fint.model.utdanning.elev.Basisgruppemedlemskap
import no.fint.model.utdanning.elev.Elev
import no.fint.model.utdanning.elev.Elevforhold
import no.fint.model.utdanning.elev.Elevtilrettelegging
import no.fint.model.utdanning.kodeverk.Avbruddsarsak
import no.fint.model.utdanning.kodeverk.Betalingsstatus
import no.fint.model.utdanning.kodeverk.Bevistype
import no.fint.model.utdanning.kodeverk.Brevtype
import no.fint.model.utdanning.kodeverk.Eksamensform
import no.fint.model.utdanning.kodeverk.Elevkategori
import no.fint.model.utdanning.larling.AvlagtProve
import no.fint.model.utdanning.timeplan.Eksamen
import no.fint.model.utdanning.timeplan.Fag
import no.fint.model.utdanning.vurdering.Anmerkninger
import no.fint.model.utdanning.vurdering.Eksamensgruppe
import no.fint.model.utdanning.vurdering.Eksamensgruppemedlemskap
import no.fint.model.utdanning.vurdering.Eksamensvurdering
import no.fint.model.utdanning.vurdering.Elevfravar
import no.fint.model.utdanning.vurdering.Elevvurdering

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
}
