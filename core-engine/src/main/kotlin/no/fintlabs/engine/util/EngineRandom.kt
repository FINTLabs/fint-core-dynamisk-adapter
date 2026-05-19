package no.fintlabs.engine.util

import no.fintlabs.engine.config.DynaEngineConfig
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class EngineRandom(
    private val props: DynaEngineConfig,
) {
    private var random: Random = createRandom()

    fun reset() {
        random = createRandom()
    }

    fun fromRange(range: IntRange): Int =
        range.random(random)

    private fun createRandom(): Random =
        if (props.seed.isBlank()) Random.Default else Random(props.seed.hashCode())
}