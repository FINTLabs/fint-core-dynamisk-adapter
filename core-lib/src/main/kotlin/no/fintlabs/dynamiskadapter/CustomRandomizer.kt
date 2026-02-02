package no.fintlabs.dynamiskadapter

import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource

class CustomRandomizer {
    fun firstname(): String = firstnameList.random()

    fun lastname(): String = lastnameList.random()

    fun fullName(): String = firstname() + " " + lastname()

    fun funnyName(): String = funnyNameList.random()

    fun quote(): String = quoteList.random()

    fun uniqueFunnyName(): String = funnyNameList.random() + personNumber()

    fun shortNumber(): Int = (1..42).random()

    fun personNumber(): String =
        (1..11)
            .map { (0..9).random() }
            .joinToString("")

    fun createAddress(): AdresseResource =
        AdresseResource().apply {
            adresselinje = List<String>(size = 1) { streetNameList.random() + " ," + shortNumber().toString() }
            postnummer = "237"
            poststed = cityNameList.random()
        }

    fun advancedString(name: String): String =
        when {
            "beskrivelse" in name || "kommentar" in name -> {
                quote()
            }

            "tittel" in name || "brukernavn" in name || "feidenavn" in name -> {
                uniqueFunnyName()
            }

            "nummer" in name || "kode" in name || "id" in name -> {
                personNumber()
            }

            "bilde" in name -> {
                "https://bildeURL/${personNumber()}"
            }

            else -> {
                fullName()
            }
        }

    private val firstnameList: List<String> =
        listOf(
            "Albert",
            "Anders",
            "Appa",
            "Bernt",
            "Carl",
            "David",
            "Daniel",
            "Egil",
            "Frank",
            "Gabriel",
            "Henrik",
            "Ingrid",
            "Jennifer",
            "Joakim",
            "Kristine",
            "Linn",
            "Lucifer",
            "Martin",
            "Morpheus",
            "Nora",
            "Nyx",
            "Neo",
            "Ola",
            "Per",
            "Qwerty",
            "Rolf",
            "Sander",
            "Trond",
            "Uda",
            "Vulf",
        )

    private val lastnameList: List<String> =
        listOf(
            "Abernathy",
            "Andeby",
            "Alderson",
            "Anderson",
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
            "Rysjedal",
            "Siljan",
            "Stormcloak",
            "Tveten",
            "Uvær",
            "Vinje",
        )

    private val funnyNameList: List<String> =
        listOf(
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
            "JaquesHass",
            "KingGizzardAndTheLizardWizardFan",
            "LordFarquaad",
            "MrRobot",
            "NameNotFound",
            "OllieTabooger",
            "REDACTED",
            "SeymourButz",
            "Skadoosh",
            "SueFlay",
            "WayneKerr",
            "ZoltanPepper",
        )

    private val quoteList: List<String> =
        listOf(
            "Are all the bats in the belfry again?",
            "Any remaining interest my friends have in me, it's just; Hey, this animal can talk. ",
            "Bubblegum dog, tell me what the truth is.",
            "Blinded by the light, revved up like a deuce, another runner in the night. ",
            "Top marks for not trying.",
            "They've sped up to the point where they provoke the punchline before they have told the joke.",
            "There's a starman waiting in the sky. He'd like to come and meet us, but he thinks he'd blow our minds.",
            "Did you ever hear the tragedy of Darth Plagueis The Wise?",
            "Do or do not, there is no try.",
            "Do you think God stays in heaven because he too lives in fear of what he has created?",
            "How was I supposed to know how to use a tube amp?",
            "How was I supposed to know how to ride a bike without hurting myself?",
            "It's over Anakin! I have the high ground!",
            "I suppose sanity is easy to lose and hard to find.",
            "I am your child, you are my mother and I'm sitting here dying",
            "I can go anywhere that I want. I just got to turn myself inside out and back to front.",
            "I'll stay home forever where two and two always makes up five.",
            "I'm just a pizza guy.",
            "I've got a bad feeling about this!",
            "Karma Police arrest this man, he talks in maths.",
            "Life is like a box of chocolates, you never know what you'll get.",
            "Nothing in life is certain except death and taxes.",
            "Nå må vi si ifra, vi vil ha ferie fra denna regjeringa!",
            "Not quite my tempo.",
            "Sailors fighting in the dance halls. Oh man, look at those cavemen go. It's the freakiest show!",
            "See you later, elevator.",
            "So you're saying there's a chance.",
            "The dripping tap won't be turned off by the suits in charge of the world, and our future's hanging on by a thread with our heads in the sand.",
            "Well sorry sunshine it doesn't exist, it wasn't in the top 100 list.",
            "With great power comes great responsibility.",
            "When they make a dollar, I make a dime. That's why I sh#t on company time.",
            "Where we're going we don't need roads.",
            "Where softer souls do sleep in the hush of morning's glow. I rest not, I can not, I just go.",
            "You are by far the worst pirate I have ever heard of.",
            "You shall not pass!",
        )

    private val cityNameList: List<String> =
        listOf(
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
            "Whiterun",
        )

    private val streetNameList: List<String> =
        listOf(
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
            "Markeveien",
            "Melkeveien",
            "Neptunveien",
            "Porsgrunnsgata",
            "Rådhusgata",
            "Storgata",
            "Torggata",
        )
}
