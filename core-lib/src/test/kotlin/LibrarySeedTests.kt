import no.fintlabs.library.ResourceFactory
import no.fintlabs.library.ResourceFactoryConfig
import no.novari.fint.model.resource.utdanning.elev.ElevResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class LibrarySeedTests {

    @Test
    fun `same seed generates same data`() {
        val firstRun = ResourceFactory(config = ResourceFactoryConfig(seed = "test-seed"))
            .create(ElevResource::class.java, 1)

        val secondRun = ResourceFactory(config = ResourceFactoryConfig(seed = "test-seed"))
            .create(ElevResource::class.java, 1)

        assertEquals(firstRun.toString(), secondRun.toString())
    }

    @Test
    fun `individual generated resources in seed are not equal`() {
        val resources = ResourceFactory(config = ResourceFactoryConfig(seed = "test-seed"))
            .create(ElevResource::class.java, 2)

        assertNotEquals(resources[0].toString(), resources[1].toString())
    }

    @Test
    fun `different seed generates different data`() {
        val firstRun = ResourceFactory(config = ResourceFactoryConfig(seed = "seed-one"))
            .create(ElevResource::class.java, amount = 1)

        val secondRun = ResourceFactory(config = ResourceFactoryConfig(seed = "seed-two"))
            .create(ElevResource::class.java, amount = 1)

        assertNotEquals(
            firstRun.toString(),
            secondRun.toString(),
        )
    }

    @Test
    fun `empty seed generates non deterministic data`() {
        val firstRun = ResourceFactory(config = ResourceFactoryConfig(seed = ""))
            .create(ElevResource::class.java, amount = 1)

        val secondRun = ResourceFactory(config = ResourceFactoryConfig(seed = ""))
            .create(ElevResource::class.java, amount = 1)

        assertNotEquals(
            firstRun.toString(),
            secondRun.toString(),
        )
    }

    @Test
    fun `seed does not reset on every run`() {
        val factory = ResourceFactory(config = ResourceFactoryConfig(seed = "test-seed"))

        val firstRun = factory.create(ElevResource::class.java, amount = 1)

        val secondRun = factory.create(ElevResource::class.java, amount = 1)

        assertNotEquals(firstRun.toString(), secondRun.toString())
    }

    @Test
    fun `seed reset generates same data`() {
        val factory = ResourceFactory(config = ResourceFactoryConfig(seed = "test-seed"))

        val firstRun = factory.create(ElevResource::class.java, amount = 1)

        factory.resetSeed()

        val secondRun = factory.create(ElevResource::class.java, amount = 1)

        assertEquals(firstRun.toString(), secondRun.toString())
    }


}