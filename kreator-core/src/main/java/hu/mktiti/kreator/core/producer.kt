package hu.mktiti.kreator.core

import java.util.function.Supplier

class InjectionProducer<T>(
        val environment: InjectEnvironment,
        val tags: List<String>,
        val default: Boolean,
        private val producer: Supplier<T>
) {

    fun create(): T = producer.get()

}