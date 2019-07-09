package hu.mktiti.kreator.property

import hu.mktiti.kreator.annotation.InjectionException
import hu.mktiti.kreator.api.inject
import hu.mktiti.kreator.api.injectAny
import hu.mktiti.kreator.api.injectOpt
import java.time.LocalDateTime

object PropertiesStore {

    // Possible values: env-var, sys-props, file-props, toml, structured-file [+ self defined PropertiesSource tags]
    private const val PROPERTIES_SOURCE_TAG_ENV_KEY = "KREATOR_PROPS_SOURCE"

    private val source: PropertiesSource

    init {
        val propertySource = System.getenv(PROPERTIES_SOURCE_TAG_ENV_KEY) ?: ""
        source = when (propertySource.toLowerCase()) {
            "" -> EmptySource
            "env-var" -> EnvVarPropertiesSource()
            "sys-props" -> SystemPropertiesSource()
            "file-props" -> PropertiesFileSource()
            "toml" -> TomlSource()
            "structured-file" -> StructuredFileSource()
            else -> injectOpt(tag = propertySource) ?: EmptySource
        }
    }

    operator fun get(name: String): String? = source.safeProperty(name)

    fun getInt(name: String): Int? = source.safeIntProperty(name)

    fun getBool(name: String): Boolean? = source.safeBoolProperty(name)

    fun getDate(name: String): LocalDateTime? = source.safeDateProperty(name)

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
        PropertiesStore.getInt(name) ?: default

fun intProperty(name: String, default: Int? = null): Int =
        intPropertyOpt(name, default) ?: throw InjectionException(name)

fun boolPropertyOpt(name: String, default: Boolean? = null): Boolean? =
        PropertiesStore.getBool(name) ?: default

fun boolProperty(name: String, default: Boolean? = null): Boolean =
        boolPropertyOpt(name, default) ?: throw InjectionException(name)

fun datePropertyOpt(name: String, default: LocalDateTime? = null): LocalDateTime? =
        PropertiesStore.getDate(name) ?: default

fun dateProperty(name: String, default: LocalDateTime? = null): LocalDateTime =
        datePropertyOpt(name, default) ?: throw InjectionException(name)