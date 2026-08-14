package no.fintlabs.contract.data

// Categories to put all resources in to generate prod-like amounts.
// GROUPING = Groups of core resources, like Skole and Klasse.
// CORE = Core resources that have 1-1 && 1-1 with each other, like Elev and Elevforhold.
// DEPENDANT = Resources that require CORE, but core does not require, like Fravarsregistrering.
// UNKNOWN = self-explanatory. Default is same as Grouping, which it should be.


enum class AmountTier {
    GROUPING,
    CORE,
    DEPENDANT,
    UNKNOWN,
}