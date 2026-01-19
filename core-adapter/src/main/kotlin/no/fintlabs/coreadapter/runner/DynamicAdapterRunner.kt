package no.fintlabs.coreadapter.runner

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.store.ResourceStore
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class DynamicAdapterRunner(
    private val storage: ResourceStore,
    private val engine: DynamicAdapterEngine,
    private val props: DynamicAdapterProperties,
    private val publisher: DynamicAdapterPublisher,
    private val adapterProps: AdapterProperties,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        if (props.initialDataSets.isEmpty()) {
            println("No initial dataset found. Shutting down...")
        }
        // Generate Capabilities for Adapter registration
        val capabilities: MutableSet<AdapterCapability> = mutableSetOf()
        for (it in props.initialDataSets) {
            val capability =
                AdapterCapability(
                    it.component.substringBefore("."),
                    it.component.substringAfter("."),
                    it.resource,
                    1,
                    AdapterCapability.DeltaSyncInterval.IMMEDIATE,
                )
            capabilities.add(capability)
        }

        engine.executeInitialDataset()
        engine.relateInitialDataset()

        publisher.register(capabilities)

        // Publisher.publish

        // Temporarily printing every resource
//        for (metadata in engine.metadataList) {
//            val data = storage.getAll(metadata.key)
//            for (i in data) {
//                println(i)
//            }
//        }
    }
}
