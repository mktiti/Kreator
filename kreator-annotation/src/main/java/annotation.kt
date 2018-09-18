package hu.mktiti.kreator.annotation

import kotlin.reflect.KClass

enum class InjectableArity { SINGLETON, SINGLETON_AUTOSTART, PER_REQUEST }

@Target(AnnotationTarget.CLASS)
annotation class InjectableType

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Repeatable
annotation class Injectable(
        val types: Array<KClass<*>> = [],
        val environment: String = "",
        val tags: Array<String> = [],
        val arity: InjectableArity = InjectableArity.PER_REQUEST,
        val default: Boolean = false
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Repeatable
annotation class TestInjectable(
        val types: Array<KClass<*>> = [],
        val environment: String = "",
        val tags: Array<String> = [],
        val arity: InjectableArity = InjectableArity.PER_REQUEST,
        val default: Boolean = false
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
annotation class NotInjectableFor(
        val types: Array<KClass<*>> = []
)

data class InjectAnnotationInfo(
        val types: List<KClass<*>>,
        val environment: String,
        val tags: List<String>,
        val arity: InjectableArity,
        val default: Boolean
)

fun injectableAnnotationToInfo(annotation: Injectable): InjectAnnotationInfo = with(annotation) {
    InjectAnnotationInfo(
            types = types.asList(),
            environment = environment,
            tags = tags.asList(),
            arity = arity,
            default = default
    )
}

fun injectableTestAnnotationToInfo(annotation: TestInjectable): InjectAnnotationInfo = with(annotation) {
    InjectAnnotationInfo(
            types = types.asList(),
            environment = if (environment.isEmpty()) "test" else "test.$environment",
            tags = tags.asList(),
            arity = arity,
            default = default
    )
}