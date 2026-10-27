package no.fintlabs.engine.util

import no.fintlabs.engine.config.DynaEngineConfig
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

@Component
class EngineRandom(
    private val props: DynaEngineConfig,
) {
    private var random: Random = createRandom()
    private val seed = AtomicReference<String>(props.seed)

    fun reset() {
        random = createRandom()
    }

    fun fromRange(range: IntRange): Int =
        range.random(random)

    fun setSeed(newSeed: String? = null) = seed.set(newSeed ?: props.seed)

    private fun createRandom(): Random =
        if (seed.get().isBlank()) Random.Default else Random(seed.get().hashCode())
}