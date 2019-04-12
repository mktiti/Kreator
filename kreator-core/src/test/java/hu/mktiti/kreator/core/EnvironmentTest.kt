package hu.mktiti.kreator.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EnvironmentTest {

    @Test
    fun `test filter sort empty`() {
        val list = listOf<Pair<InjectEnvironment, String>>()

        val result = filterEnvironmentsSorted(list, InjectEnvironment("prod"))

        assertEquals(emptyList(), result)
    }

    @Test
    fun `test filter sort no match`() {
        val list = listOf(
                InjectEnvironment("test") to "tester",
                InjectEnvironment("test.unit") to "unit tester",
                InjectEnvironment("dev") to "developer"
        )

        val result = filterEnvironmentsSorted(list, InjectEnvironment("prod"))

        assertEquals(emptyList(), result)
    }

    @Test
    fun `test filter sort all match empty env`() {
        val list = listOf(
                InjectEnvironment("test") to "tester",
                InjectEnvironment("test.unit") to "unit tester",
                InjectEnvironment("dev") to "developer"
        )

        val result = filterEnvironmentsSorted(list, InjectEnvironment())

        assertEquals(3, result.size)
    }

    @Test
    fun `test filter sort empty env match`() {
        val list = listOf(
                InjectEnvironment() to "root"
        )

        val result = filterEnvironmentsSorted(list, InjectEnvironment())

        assertEquals(listOf("root"), result)
    }

    @Test
    fun `test filter sort different branch`() {
        val list = listOf(
                InjectEnvironment("test.unit") to "unit tester"
        )

        val result = filterEnvironmentsSorted(list, InjectEnvironment("test.integ"))

        assertEquals(emptyList(), result)
    }

    @Test
    fun `test filter sort order exact`() {
        val envs = listOf(
                "", "a", "a.b", "a.b.c", "a.b.c", "a.b.c.d", "x", "a.x", "a.b.x"
        )
        val list = envs.map { InjectEnvironment.safeParse(it) to it }

        val result = filterEnvironmentsSorted(list, InjectEnvironment.safeParse("a.b.c"))

        assertEquals(listOf("a.b.c", "a.b.c"), result)
    }

    @Test
    fun `test filter sort order subs`() {
        val envs = listOf(
                "", "a", "a.b", "a.b.c.d", "a.b.c.d.e", "a.b.c.d.e.f", "a.b.c.x", "x", "a.x", "a.b.x"
        )
        val list = envs.map { InjectEnvironment.safeParse(it) to it }

        val result = filterEnvironmentsSorted(list, InjectEnvironment.safeParse("a.b.c"))

        assertEquals(listOf("a.b.c.d", "a.b.c.x", "a.b.c.d.e", "a.b.c.d.e.f").sorted(), result.sorted())
    }

    @Test
    fun `test filter sort order first sups`() {
        val envs = listOf(
                "", "a", "a.b", "x", "a.x", "a.b.x"
        )
        val list = envs.map { InjectEnvironment.safeParse(it) to it }

        val result = filterEnvironmentsSorted(list, InjectEnvironment.safeParse("a.b.c"))

        assertEquals(listOf("a.b"), result)
    }

    @Test
    fun `test filter sort order second sups`() {
        val envs = listOf(
                "", "a", "x", "a.x", "a.b.x"
        )
        val list = envs.map { InjectEnvironment.safeParse(it) to it }

        val result = filterEnvironmentsSorted(list, InjectEnvironment.safeParse("a.b.c"))

        assertEquals(listOf("a"), result)
    }

    @Test
    fun `test filter sort order root sups`() {
        val envs = listOf(
                "", "b", "x", "a.x", "a.b.x"
        )
        val list = envs.map { InjectEnvironment.safeParse(it) to it }

        val result = filterEnvironmentsSorted(list, InjectEnvironment.safeParse("a.b.c"))

        assertEquals(listOf(""), result)
    }

}