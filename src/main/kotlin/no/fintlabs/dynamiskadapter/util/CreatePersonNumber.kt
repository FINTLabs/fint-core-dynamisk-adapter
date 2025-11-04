package no.fintlabs.dynamiskadapter.util

fun createPersonNumber(): String =
    (1..11)
        .map { (0..9).random() }
        .joinToString("")
