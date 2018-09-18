# Kreator
Minimalistic dependency injection framework for Kotlin with annotation based, declarative resource description and programmatic injection.

## Injection logic
Injection resolution is based on the program environment, the tags of the resources and default flags.

## Environment
The running program has its environment set using the `kreator-env` environmental variable.
It is a structured string value separated by dots (e.g. `test.unit`, `prod.ec2`, `dev`).
The default setting is empty ("").
Resource declarations define envionment in which the resource can be injected, with empty ("") default.
When searching for appropiate resources the framework first looks at resources with environemnt equals the current one.
If no valid resource is found that way, resources with sub-environements of the current one are looked at (e.g. `kreator-env = "test"`, environment of resource: `test.unit`)
If that also fails, resources with environements that are parent of the current one are looked at (e.g. `kreator-env = "dev.local"`, environment of resource: `dev`)
Note that environments on different 'branches' are never injected (e.g. `kreator-env = "test.unit"`, environment of resource: `test.integ`)

Note that resources marked for empty environment are always applicable ("" is the root of the env tree),
similarly if the environment is set to be empty (`kreator-env = ""`) then all resources are injectable.

### Example
`kreator-env = "test.unit"`

| env group   | env example       | order of lookup |
| ----------- | ----------------- | --------------- |
| exact match | `test.unit`       | first           |
| sub-env     | `test.unit.junit` | second          |
| sup-env     | `test`            | third           |
| neither     | `prod`            | never           |

## Tags
Resources can have string tags (e.g. `async`, `sync`, `file`, `db`, `in-mem`).
These are completely custom and the framework only compares them to the required tag if one is supplied as an injection parameter.
If such a tag is passed only a resource marked with that tag can be injected.

## Defaults
Resources can be marked default, so when multiple candidates are listed (all from the same env group) the framework selects them.

## Arity
Arity defines how a resource should be instantized.
Possible values are `PER_REQUEST` for new creation on every inject request,
`SINGLETON` for singleton resources ans `SINGLETON_AUTOSTART` for singletons that are created at framework initialization. 

## Resource declaration
Marked resources can be classes, constructors or generator functions.
Classes must have a no-arg contructor (default arguments are fine) and behave as if that constructor was marked.
Constructors and functions must have no arguments (default arguments are fine here, too).
In case of functions, their "type" is the type of their return value.

You can declare resources for injection using annotations. The main annotations are `InjectableType`, `Injectable`, `TestInjectable` and `NotInjectableFor`.
All resources must be marked for injection, there is no automatic injetion of children of marked resources.

`InjectableType` defines types (mostly interfaces) so their subclasses (implementations) become automatically injectable for them if they themselves are marked injectable.

`Injectable` marks a type for injection. It defines the environment, the tags, defaultness and arity of the reource.
It also defines the types it can be injected for, if left empty it is automatically injectable for its own type (class) and
all of its supertypes that are marked with InjectableType.

`TestInjectable` behaves the same as Injectable˙, only its environment gets an implicit `test` prefix for type safety.
(`test` is the most common non default environment)

resources marked with `NotInjectableFor` are not injectable for any types marked in the annotation parameter.
It is useful if a class has a lot of superclasses marked with `InjectableType` and you want it not be injectable for only a few of them.

Resources (classes, generator functions) can have multiple injectable annotations all with their own rules.

## Injection
Injection happens by calling one of the injection functions: `inject`, `injectAny` or `injectOpt`.

`inject` injects a given resource if it can be found deterministically.
If more than one possible resources are found (same env group, correct tag, same default value) than an `InjectionException` is thrown.
Similarly if no resource is found than an `InjectionException` is thrown.

`injectOpt` works the same way only if no resource is found it returns `null`.

`injectAny` works as `inject`, but if multiple resources are available it picks one of them instead of throwing an exception.

Sample usage can be found in module kreator-sample.

## Modules
The project consists of three production modules, `kreator-annotation` defines the annotations,
`kreator-core` defines the injection functions and `kreator-core` contains the injection logic implementation.
