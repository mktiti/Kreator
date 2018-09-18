package hu.mktiti.kreator.sample

import hu.mktiti.kreator.annotation.InjectionException
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.api.injectOpt
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GeneratorTest {

    @Test
    fun `test default generator success`() {
        val generator: IntGenerator = inject()
        assert(generator.javaClass == SequentialGenerator::class.java) { "Default injected generator is not SequentialGenerator" }
    }

    @Test
    fun `test non singleton generator success`() {
        val generator1: IntGenerator = inject()
        generator1.generate()

        val generator2: IntGenerator = inject()
        val gen: Int = generator2.generate()

        assertEquals(0, gen, "Non-singleton generator same instance")
    }

    @Test
    fun `test singleton generator success`() {
        val generator1: StaticSequentialGenerator = inject()
        generator1.generate()

        val generator2: StaticSequentialGenerator = inject()
        val gen: Int = generator2.generate()

        assertEquals(1, gen, "Singleton generator not the same instance")
    }

    @Test
    fun `test tagged generator success`() {
        val fibGenerator: IntGenerator = inject("fib")
        assert(fibGenerator.javaClass == FibonacciGenerator::class.java) { "Fib tagged injected generator is not FibonacciGenerator" }

        val randGenerator: IntGenerator = inject("rand")
        assert(randGenerator.javaClass == RandomGenerator::class.java) { "Fib tagged injected generator is not RandomGenerator" }
    }

    @Test
    fun `test non-existent generator exception`() {
        assertThrows<InjectionException> {
            inject<IntGenerator>("non-existing")
        }
    }

    @Test
    fun `test non-existent generator optional success`() {
        val noGenerator: IntGenerator? = injectOpt("non-existent")
        assert(noGenerator == null) { "Non-existing generator is injected" }
    }

    @Test
    fun `test test generator success`() {
        inject<IntGenerator>("zero")
    }
}
