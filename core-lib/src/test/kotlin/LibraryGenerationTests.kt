import no.fintlabs.library.ResourceFactory
import no.fintlabs.library.ResourceFactoryConfig
import no.novari.fint.model.resource.utdanning.elev.ElevResource
import no.novari.fint.model.resource.utdanning.timeplan.FaggruppeResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue


class LibraryGenerationTests {

    @Test
    fun `generate a single resource with specified value`() {
        val factory = ResourceFactory(config = ResourceFactoryConfig(seed = ""))

        val einar = factory.createWithSingleSpecifiedValue(
            ElevResource::class.java,
            "brukernavn",
            "einar",
        )
        val einarResource = einar.first() as ElevResource

        assertTrue { einar.size == 1 }
        assertEquals("einar", einarResource.brukernavn.identifikatorverdi)
    }

    @Test
    fun `generate resource with specified value applies to all but otherwise unique`() {
        val factory = ResourceFactory(config = ResourceFactoryConfig(seed = ""))

        val faggrupper = factory.createWithSingleSpecifiedValue(
            FaggruppeResource::class.java,
            "navn",
            "gruppa",
            5,
        ).map { it as FaggruppeResource }

        assertTrue(faggrupper.all { it.navn == "gruppa" })
        assertNotEquals(faggrupper[0], faggrupper[1])
    }

}