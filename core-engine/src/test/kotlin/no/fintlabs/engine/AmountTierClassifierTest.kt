package no.fintlabs.engine

import no.fintlabs.contract.data.AmountTier
import no.novari.metamodel.ComponentBuilder
import no.novari.metamodel.MetamodelService
import no.novari.metamodel.ReflectionService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class AmountTierClassifierTest {
    private val metamodelService = MetamodelService(ComponentBuilder(ReflectionService()))
    private val metadataService = MetadataService(metamodelService)
    private val amountTierClassifier = AmountTierClassifier()

    @Test
    fun `should classify amount tiers for all utdanning resources`() {
        val metadata = metadataService
            .generateMetadataFromDomain("utdanning")
            .toMutableList()
        assertThat(metadata)
            .describedAs("Expected Utdanning domain to contain resources")
            .isNotEmpty

        amountTierClassifier.classify(metadata)

        println()
        println("AmountTier classification for Utdanning:")
        println("----------------------------------------")

        metadata
            .sortedBy { it.key }
            .forEach {
                if (it.key.contains("kodeverk")) {
                    return@forEach
                }
                println("${it.key.padEnd(50)} -> ${it.amountTier}")
            }
        assertTrue(metadata.isNotEmpty(), "Expected Utdanning metadata to contain resources")

        assertTrue(
            metadata.all { it.amountTier != null },
            "Expected all Utdanning resources to receive an AmountTier"
        )

        assertTrue(
            metadata.any { it.amountTier == AmountTier.CORE },
            "Expected at least one CORE resource in Utdanning"
        )

    }

}