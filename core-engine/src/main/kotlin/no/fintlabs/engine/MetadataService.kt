package no.fintlabs.engine

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.engine.util.generateIdMetadata
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.models.ResourceIdentifiers
import no.novari.metamodel.MetamodelService
import no.novari.metamodel.model.Resource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MetadataService(
    @Autowired private val model: MetamodelService,
    private val tierClassifier: AmountTierClassifier,
) {
    val logger: Logger = LoggerFactory.getLogger(MetadataService::class.java)
    private val domainsGeneratedFor: MutableList<String> = mutableListOf()
    private val metadataList: MutableList<ExpandedMetadata> = mutableListOf()
    private val capabilities: MutableSet<AdapterCapability> = mutableSetOf()

    fun getAllMetadata(): MutableList<ExpandedMetadata> = metadataList

    fun getNamesOfCapabilities(): List<String> =
        capabilities.map { "${it.domainName}/${it.component}/${it.resourceName}" }

    fun generateMetadataFromDomain(domain: String) {
        if (domainsGeneratedFor.contains(domain)) {
            logger.trace("Metadata for $domain has already been generated.")
        } else {
            val generatedMetadata: MutableList<ExpandedMetadata> = mutableListOf()

            val resources = model.getResources().filter { it.component.domainName.equals(domain, ignoreCase = true) }

            if (resources.isNotEmpty()) {
                for (resource in resources) {
                    val metadata = resource.generateMetadata()
                    metadataList.add(metadata)
                    generatedMetadata.add(metadata)
                }
            } else logger.warn("No metadata found for $domain")
            domainsGeneratedFor.add(domain)
            tierClassifier.classify(metadataList)
        }
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

    fun getMetadataFor(identifier: ResourceIdentifiers): ExpandedMetadata? =
        metadataList.find { it.key == identifier.toKey() }


    private fun Resource.generateMetadata(): ExpandedMetadata {
        val resourceKey = "${this.component.domainName}/${this.component.packageName}/${this.name}"
        val idMeta = this.generateIdMetadata()
        return ExpandedMetadata(this, resourceKey, amountTier = null, idMeta.prefix, idMeta.type)
    }
}