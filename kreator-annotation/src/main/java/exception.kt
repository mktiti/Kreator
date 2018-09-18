package hu.mktiti.kreator.annotation

class InjectorException : RuntimeException("Cannot find injection provider, kreator-core package may be missing")

class InjectionException(componentName: String) : RuntimeException("Cannot injectable object for $componentName")

class InjectionConfigException(componentName: String) : RuntimeException("Invalid injection provider $componentName")