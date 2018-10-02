package hu.mktiti.kreator.property

import hu.mktiti.kreator.annotation.InjectionException
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.api.injectAny
import hu.mktiti.kreator.api.injectOpt

object PropertiesStore {

    // Possible values: env-var, sys-props, file-props [+ self defined PropertiesSource tags]
    private const val PROPERTIES_SOURCE_TAG_ENV_KEY = "KREATOR_PROPS_SOURCE"

    private val source: PropertiesSource

    init {
        source = injectOpt(tag = System.getenv(PROPERTIES_SOURCE_TAG_ENV_KEY)) ?: EmptySource
    }

    operator fun get(name: String): String? = source.safeProperty(name)

}

inline fun <reified T : Any> injectPropTag(propertyName: String, default: String? = null): T =
        inject(T::class, tag = property(propertyName, default)) ?: throw InjectionException(T::class.java.canonicalName)

inline fun <reified T : Any> injectPropTagAny(propertyName: String, default: String? = null): T =
        injectAny(T::class, tag = property(propertyName, default)) ?: throw InjectionException(T::class.java.canonicalName)

inline fun <reified T : Any> injectPropTagOpt(propertyName: String, default: String? = null): T? =
        inject(T::class, tag = property(propertyName, default))

fun propertyOpt(name: String, default: String? = null): String? =
        PropertiesStore[name] ?: default

fun property(name: String, default: String? = null): String =
        propertyOpt(name, default) ?: throw InjectionException(name)

fun intPropertyOpt(name: String, default: Int? = null): Int? =
        propertyOpt(name, null)?.toInt() ?: default

fun intProperty(name: String, default: Int? = null): Int =
        intPropertyOpt(name, default) ?: throw InjectionException(name)

fun boolPropertyOpt(name: String, default: Boolean? = null): Boolean? =
        propertyOpt(name, null)?.toBoolean() ?: default

fun boolProperty(name: String, default: Boolean? = null): Boolean =
        boolPropertyOpt(name, default) ?: throw InjectionException(name)