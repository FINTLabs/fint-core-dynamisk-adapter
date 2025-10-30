package no.fintlabs.dynamiskadapter.util.general

fun createPersonNumber(): String =
    (1..11)
        .map { (0..9).random() }
        .joinToString("")
