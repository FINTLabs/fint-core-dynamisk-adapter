package no.fintlabs.engine

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.engine.util.generateIdMetadata
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.models.ResourceIdentifiers
import no.novari.metamodel.MetamodelService
import no.novari.metamodel.model.Resource
import org.springframework.stereotype.Component

@Component
class MetadataService(
    private val model: MetamodelService,
    private val tierClassifier: AmountTierClassifier,
) {
    private val metadataList: MutableList<ExpandedMetadata> = mutableListOf()
    private val capabilities: MutableSet<AdapterCapability> = mutableSetOf()

    fun getAllMetadata(): MutableList<ExpandedMetadata> = metadataList

    fun generateMetadataFromDomain(domain: String) {
        val generatedMetadata: MutableList<ExpandedMetadata> = mutableListOf()

        val resources = model.getResources().filter { it.component.domainName.equals(domain, ignoreCase = true) }

        if (resources.isNotEmpty()) {
            for (resource in resources) {
                val metadata = resource.generateMetadata()
                metadataList.add(metadata)
                generatedMetadata.add(metadata)
            }
        }
        tierClassifier.classify(metadataList)
    }

    fun generateMetadataFromIdentifiers(
        identifiers: List<ResourceIdentifiers>,
    ): MutableList<ExpandedMetadata> {
        val metadataList = mutableListOf<ExpandedMetadata>()
        for (it in identifiers) {
            val resource = model.getResource(it.domain, it.component, it.resource)
            val resourceKey = "${it.domain}/${it.component}/${it.resource}"
            if (resource != null) {
                val idMeta = resource.generateIdMetadata()
                val metadata = ExpandedMetadata(resource, resourceKey, amountTier = null, idMeta.prefix, idMeta.type)
                metadataList.add(metadata)
            } else println("Could not find resource: $resourceKey in metamodel.")
        }
        return metadataList
    }

    fun generateCapabilities(): MutableSet<AdapterCapability> {
        for (it in metadataList) {
            val keyParts = it.key.split("/")
            val capability =
                AdapterCapability(
                    keyParts[0],
                    keyParts[1],
                    keyParts[2],
                    1,
                    AdapterCapability.DeltaSyncInterval.IMMEDIATE,
                )
            capabilities.add(capability)
        }
        return capabilities
    }

    private fun Resource.generateMetadata(): ExpandedMetadata {
        val resourceKey = "${this.component.domainName}/${this.component.packageName}/${this.name}"
        val idMeta = this.generateIdMetadata()
        return ExpandedMetadata(this, resourceKey, amountTier = null, idMeta.prefix, idMeta.type)
    }
}