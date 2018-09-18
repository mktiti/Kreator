package hu.mktiti.kreator.api

import hu.mktiti.kreator.annotation.InjectionException
import kotlin.reflect.KClass

interface Injector {
    fun <T : Any> inject(clazz: KClass<T>, tag: String? = null): T?

    fun <T : Any> injectAny(clazz: KClass<T>, tag: String? = null): T?
}

inline fun <reified T : Any> inject(tag: String? = null): T = inject(T::class, tag)
        ?: throw InjectionException(T::class.java.canonicalName)

inline fun <reified T : Any> injectOpt(tag: String? = null): T? = inject(T::class, tag)

inline fun <reified T : Any> injectAny(tag: String? = null): T = injectAny(T::class, tag)
        ?: throw InjectionException(T::class.java.canonicalName)

fun <T : Any> inject(clazz: KClass<T>, tag: String? = null) = InjectorFactory.injector.inject(clazz, tag)

fun <T : Any> injectAny(clazz: KClass<T>, tag: String? = null) = InjectorFactory.injector.injectAny(clazz, tag)