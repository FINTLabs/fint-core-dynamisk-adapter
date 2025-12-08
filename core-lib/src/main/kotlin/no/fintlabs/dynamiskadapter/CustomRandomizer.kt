package no.fintlabs.dynamiskadapter

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource

class CustomRandomizer {

    fun fullName(): String = firstname() + " " + lastname()

    fun firstname(): String = firstnameList.random()

    fun lastname(): String = lastnameList.random()

    fun quote(): String = quoteList.random()

    fun funnyName(): String = funnyNameList.random()

    fun personNumber(): String =
        (1..11)
            .map { (0..9).random() }
            .joinToString("")

    fun createAddress(): AdresseResource {
        return AdresseResource().apply {
            adresselinje = List<String>(size = 1) { streetNameList.random() }
            postnummer = "237"
            poststed = cityNameList.random()
        }
    }

    private val firstnameList: List<String> = listOf(
        "Albert",
        "Bernt",
        "Carl",
        "David",
        "Egil",
        "Frank",
        "Gabriel",
        "Henrik",
        "Ingrid",
        "Jennifer",
        "Kristine",
        "Linn",
        "Martin",
        "Nora",
        "Nyx",
        "Ola",
        "Per",
        "Qwerty",
        "Rolf",
        "Sander",
        "Trond",
        "Uda",
        "Vulf"
    )

    private val lastnameList: List<String> = listOf(
        "Abernathy",
        "Ballestad",
        "Clarkson",
        "Dover",
        "Ekeberg",
        "Fleksnes",
        "Grandiosa",
        "Hosen",
        "Idun",
        "Jahr",
        "Karlsen",
        "Lifeson",
        "Monsen",
        "Nilsen",
        "Oppenheimer",
        "Person",
        "Qwerty",
        "Rysjedal",
        "Siljan",
        "Tveten",
        "Uvær",
        "Vinje"
    )

    private val funnyNameList: List<String> = listOf(
        "AmyStake",
        "BirdsAreNotReal",
        "BreadConsumer",
        "BenDover",
        "CrazyCatLady",
        "DestroyerOfToilets",
        "Earworm",
        "FloPeacock",
        "GuiltyPleasureDancer",
        "HandLotionDrinker",
        "HughJazz",
        "IsThisAValidName",
        "JackHoff",
        "KingGizzardAndTheLizardWizardFan",
        "LordFarquaad",
        "MrRobot",
        "NameNotFound",
        "OllieTabooger",
        "REDACTED",
        "SeymourButz",
        "SueFlay",
        "WayneKerr",
        "ZoltanPepper"
    )

    private val quoteList: List<String> = listOf(
        "Are all the bats in the belfry again?",
        "Bubblegum dog, tell me what the truth is.",
        "Do you think God stays in heaven because he too lives in fear of what he has created?",
        "Did you ever hear the tragedy of Darth Plagueis The Wise?",
        "I know who I am. I'm a dude playing a dude disguised as another dude!",
        "It's over Anakin! I have the high ground!",
        "I suppose sanity is easy to lose and hard to find.",
        "I am your child, you are my mother and I'm sitting here dying",
        "I can go anywhere that I want. I just got to turn myself inside out and back to front.",
        "I'm a priest god never paid.",
        "Life is like a box of chocolates, you never know what you'll get.",
        "Nothing in life is certain except death and taxes.",
        "Not quite my tempo.",
        "So you're saying there's a chance.",
        "The dripping tap won't be turned off by the suits in charge of the world, and our future's hanging on by a thread with our heads in the sand.",
        "When they make a dollar, I make a dime. That's why I sh#t on company time.",
        "Where we're going we don't need roads.",
        "Where softer souls do sleep in the hush of morning's glow. I rest not, I can not, I just go.",
        "You are by far the worst pirate I have ever heard of."
    )

    private val cityNameList: List<String> = listOf(
        "Aas",
        "Bergen",
        "Drammen",
        "Egersund",
        "Fredrikstad",
        "Gvarv",
        "Helvette",
        "Ingvarstead",
        "Jarlsberg",
        "Kristiansand",
        "Larvik",
        "Morrowind",
        "Nome",
        "Oslo",
        "Porsgrunn",
        "Rivendell",
        "Skien",
        "Troms",
        "Varda",
        "Whiterun"
    )

    private val streetNameList: List<String> = listOf(
        "Albertsgate",
        "Bjørnegata",
        "Chr. Bloms Gate",
        "Den gata",
        "Ekeberg",
        "Franklinstorget",
        "Gulsetringen",
        "Helt vanlig gatenavn",
        "Ingenmannsland",
        "Linus' Sving",
        "Porsgrunnsgata",
        "Rådhusgata",
        "Storgata",
        "Torggata",
    )
}