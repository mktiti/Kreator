package hu.mktiti.kreator.core

import hu.mktiti.kreator.annotation.*
import hu.mktiti.kreator.api.Injector
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Method
import java.util.function.Supplier
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.jvm.kotlinFunction

typealias ProducerMap = Map<KClass<*>, List<InjectionProducer<*>>>

private data class ProducerScanResult(
        val producerMap: ProducerMap,
        val autostartSingleton: Supplier<*>?
)

private data class ScanResult(
        val producerMap: ProducerMap,
        val autostartSingletons: List<Supplier<*>>
)

private const val ENVIRONMENT_VAR = "KREATOR_ENV"

class MainInjector : Injector {

    private val defaultEnvironment: InjectEnvironment = loadDefaultEnvironment()

    private val producerMap: ProducerMap

    init {
        val scanResult = scanForProducers()
        producerMap = scanResult.producerMap
        scanResult.autostartSingletons.forEach { it.get() }
    }

    private fun loadDefaultEnvironment(): InjectEnvironment = InjectEnvironment.safeParse(System.getenv(ENVIRONMENT_VAR)
            ?: "")

    private fun scanForProducers(): ScanResult {
        val config = ConfigurationBuilder().setScanners(MethodAnnotationsScanner(), TypeAnnotationsScanner(), SubTypesScanner())
                     .setUrls(ClasspathHelper.forJavaClassPath())

        val reflections = Reflections(config)

        val injectableTypes = reflections.getTypesAnnotatedWith(InjectableType::class.java).map { it.kotlin }

        val classes: Set<Class<*>> = reflections.getTypesAnnotatedWith(Injectable::class.java) + reflections.getTypesAnnotatedWith(TestInjectable::class.java)
        val methods: Set<Method> = reflections.getMethodsAnnotatedWith(Injectable::class.java) + reflections.getMethodsAnnotatedWith(TestInjectable::class.java)

        val producers = classes.map { loadConstructorInjectors(it.kotlin, injectableTypes) } +
                        methods.map { loadFunctionInjectors(it.kotlinFunction!!, injectableTypes) }

        val allProducers = mutableMapOf<KClass<*>, List<InjectionProducer<*>>>()
        val autoStarts = mutableListOf<Supplier<*>>()

        producers.forEach { (map, auto) ->
            map.entries.forEach { (k, v) ->
                allProducers.merge(k, v) { a, b -> a + b }
            }
            if (auto != null) {
                autoStarts.add(auto)
            }
        }

        return ScanResult(allProducers, autoStarts)
    }

    private fun loadConstructorInjectors(clazz: KClass<*>, injectableTypes: List<KClass<*>>): ProducerScanResult {
        val noArgConstructor = clazz.constructors.find { it.parameters.all(KParameter::isOptional) }
        if (noArgConstructor != null) {
            return findInjectionTargets(noArgConstructor, clazz.annotations + noArgConstructor.annotations, injectableTypes)
        } else {
            throw InjectionConfigException(clazz.jvmName)
        }
    }

    private fun loadFunctionInjectors(function: KFunction<*>, injectableTypes: List<KClass<*>>): ProducerScanResult {
        if (function.parameters.all(KParameter::isOptional)) {
            return findInjectionTargets(function, function.annotations, injectableTypes)
        } else {
            throw InjectionConfigException(function.name)
        }
    }

    private fun findInjectionTargets(producerFun: KFunction<*>, annotations: Collection<Annotation>, injectableTypes: List<KClass<*>>): ProducerScanResult {
        if (producerFun.parameters.any { !it.isOptional } || producerFun.returnType.isMarkedNullable) {
            throw InjectionConfigException(producerFun.name)
        }

        val resultType = producerFun.returnType.jvmErasure

        val suppressedTypes: Set<KClass<*>> = producerFun.annotations
                                                .filterIsInstance(NotInjectableFor::class.java)
                                                .flatMap { it.types.asList() }
                                                .toSet()

        val injectDefinitions: List<InjectAnnotationInfo>
                = annotations.filterIsInstance(Injectable::class.java).map(::injectableAnnotationToInfo) +
                  annotations.filterIsInstance(TestInjectable::class.java).map(::injectableTestAnnotationToInfo)

        val allInjectableSupers: Lazy<List<KClass<*>>> = lazy {
            val allInjectableTypes: MutableSet<KClass<*>> = injectableTypes.filter { resultType.isSubclassOf(it) }.toMutableSet()
            allInjectableTypes += resultType
            allInjectableTypes += resultType.allSuperclasses.filter(this::isKClassInjectable)

            allInjectableTypes.filterNot { it in suppressedTypes }.toList()
        }

        val provider: Supplier<*> = if (injectDefinitions.all { it.arity == InjectableArity.PER_REQUEST }) {
            // Per Request
            Supplier { producerFun.callBy(emptyMap()) }
        } else {
            // Singleton
            val lazySingleton = lazy { producerFun.callBy(emptyMap()) }
            Supplier { lazySingleton.value }
        }

        // Autostart
        val autoStarter: Supplier<*>? = if (injectDefinitions.any { it.arity == InjectableArity.SINGLETON_AUTOSTART }) provider else null

        val producerMap: ProducerMap = injectDefinitions.flatMap { info ->
            val types = if (info.types.isEmpty()) allInjectableSupers.value else info.types
            types.map { it to info }
        }.groupBy(Pair<KClass<*>, *>::first) { (_, info) ->
            InjectionProducer(
                    InjectEnvironment.safeParse(info.environment),
                    info.tags,
                    info.default,
                    provider)
        }

        return ProducerScanResult(producerMap, autoStarter)
    }

    private fun isKClassInjectable(clazz: KClass<*>) = clazz.annotations.any { it is Injectable || it is TestInjectable }

    override fun <T : Any> inject(clazz: KClass<T>, tag: String?): T?
            = injectWithMultiDefaultLogic(clazz, tag) { throw InjectionException(clazz.jvmName) }

    override fun <T : Any> injectAny(clazz: KClass<T>, tag: String?): T?
            = injectWithMultiDefaultLogic(clazz, tag) { it.first() }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> injectWithMultiDefaultLogic(
            clazz: KClass<T>,
            tag: String?,
            onMultiDefault: (List<InjectionProducer<*>>) -> InjectionProducer<*>): T? {

        val producers = fetchAvailable(clazz, tag)
        val defaults = when (producers.size) {
            0 -> return null
            1 -> return producers.first().create() as T?
            else -> producers.filter(InjectionProducer<*>::default)
        }

        return when (defaults.size) {
            0 -> onMultiDefault(producers)
            1 -> defaults.first()
            else -> onMultiDefault(defaults)
        }.create() as T?
    }

    private fun <T : Any> fetchAvailable(clazz: KClass<T>, tag: String?): List<InjectionProducer<*>> {
        val tagProducers: List<InjectionProducer<*>> =
                if (tag == null) {
                    producerMap[clazz]
                } else {
                    producerMap[clazz]?.filter { tag in it.tags }
                } ?: return listOf()

        return filterEnvironmentsSorted(tagProducers.map { it.environment to it }, defaultEnvironment)
    }
}