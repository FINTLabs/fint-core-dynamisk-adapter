package no.fintlabs.coreadapter.runner

import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.store.ResourceStore
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DynamicAdapterRunner(
    private val props: DynamicAdapterProperties,
    private val engine: DynamicAdapterEngine,
    private val storage: ResourceStore,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (props.initialDataSets.isEmpty()) {
            println("No initial dataset found. Shutting down...")
        }
        engine.executeInitialDataset()
        engine.relateInitialDataset()

        // Publisher.register
        // Publisher.publish

        for (metadata in engine.metadataList) {
            val data = storage.getAll(metadata.key)
            for (i in data) {
                println(i)
            }
        }
    }
}
