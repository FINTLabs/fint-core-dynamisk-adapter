package no.fintlabs.coreadapter.runner

import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DynamicAdapterRunner(
    private val props: DynamicAdapterProperties,
    private val engine: DynamicAdapterEngine
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (props.initialDataSets.isEmpty())
            println("No initial dataset found. Shutting down...")
        return
    }


}