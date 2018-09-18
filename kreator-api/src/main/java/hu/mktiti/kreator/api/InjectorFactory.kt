package hu.mktiti.kreator.api

import java.util.*
import kotlin.reflect.KClass

object InjectorFactory {

    private val injectorOpt: Injector? by lazy(this::findInjector)

    val injector: Injector
        get() = injectorOpt ?: FailingInjector

    private fun findInjector(): Injector? = ServiceLoader.load(Injector::class.java).findFirst().orElse(null)

    fun forceInit() { injector }

    fun isInjectorPresent(): Boolean = injectorOpt != null

}

private object FailingInjector : Injector {

    override fun <T : Any> inject(clazz: KClass<T>, tag: String?): T? = null

    override fun <T : Any> injectAny(clazz: KClass<T>, tag: String?): T? = null

}