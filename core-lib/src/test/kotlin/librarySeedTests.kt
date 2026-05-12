import no.fintlabs.library.ResourceFactory
import no.novari.fint.model.resource.utdanning.elev.ElevResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


class librarySeedTests {

    @Test
    fun `same seed generates same data`() {
        val firstRun = ResourceFactory(seed = "test-seed")
            .create(ElevResource::class.java, 2)

        val secondRun = ResourceFactory(seed = "test-seed")
            .create(ElevResource::class.java, 2)

        assertEquals(firstRun.toString(), secondRun.toString())
    }

    @Test
    fun `individual generated resources in seed are still random`() {
        val resources = ResourceFactory(seed = "test-seed")
            .create(ElevResource::class.java, 2)

        assertNotEquals(resources[0].toString(), resources[1].toString())
    }

    @Test
    fun `different seed generates different data`() {
        val firstRun = ResourceFactory(seed = "seed-one")
            .create(ElevResource::class.java, amount = 5)

        val secondRun = ResourceFactory(seed = "seed-two")
            .create(ElevResource::class.java, amount = 5)

        assertNotEquals(
            firstRun.toString(),
            secondRun.toString(),
        )
    }

    @Test
    fun `empty seed generates non deterministic data`() {
        val firstRun = ResourceFactory(seed = "")
            .create(ElevResource::class.java, amount = 5)

        val secondRun = ResourceFactory(seed = "")
            .create(ElevResource::class.java, amount = 5)

        assertNotEquals(
            firstRun.toString(),
            secondRun.toString(),
        )
    }
}