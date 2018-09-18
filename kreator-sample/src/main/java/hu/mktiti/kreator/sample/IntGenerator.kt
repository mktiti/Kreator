package hu.mktiti.kreator.sample

import hu.mktiti.kreator.annotation.Injectable
import hu.mktiti.kreator.annotation.InjectableArity
import hu.mktiti.kreator.annotation.InjectableType
import hu.mktiti.kreator.annotation.TestInjectable
import hu.mktiti.kreator.api.inject
import java.util.*

@InjectableType
interface GeneratorLogger {

    fun logNextHundred()

}

@Injectable(tags = ["db"])
class DatabaseLogger(
        private val generator: IntGenerator = inject()
) : GeneratorLogger {

    override fun logNextHundred() {
        // TODO
        // Connection ...
    }

}

@Injectable(default = true)
class StandardGeneratorLogger(
        private val generator: IntGenerator = inject()
) : GeneratorLogger {

    override fun logNextHundred() {
        for (i in 0..100) {
            println(generator.generate())
        }
    }
}

@InjectableType
interface IntGenerator {
    fun generate(): Int

    fun getName(): String
}

@Injectable(default = true)
open class SequentialGenerator : IntGenerator {
    private var count = 0

    override fun generate(): Int = count++

    override fun getName(): String = "Sequential Generator"
}

@Injectable(arity = InjectableArity.SINGLETON)
class StaticSequentialGenerator : SequentialGenerator() {
    override fun getName() = "Static Sequential Generator"
}

@TestInjectable(tags = ["zero"])
class ZeroGenerator : IntGenerator {
    override fun generate() = 0

    override fun getName() = "Zero Generator [test]"
}

@Injectable(tags = ["fib"])
class FibonacciGenerator : IntGenerator {
    private var current: Int = 1
    private var last: Int = 0

    override fun generate(): Int {
        val temp = current + last
        last = current
        current = temp
        return current
    }

    override fun getName() = "Fibonacci Generator"
}

@Injectable(tags = ["rand"])
class RandomGenerator : IntGenerator {
    private val random = Random()

    override fun generate(): Int = random.nextInt()

    override fun getName() = "Random Generator"
}