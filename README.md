# kotlin-fixture-magic

![Build and test kotlin-fixture-magic](https://github.com/wThomas84/kotlin-fixture-magic/workflows/Build%20and%20test%20kotlin-fixture-magic/badge.svg)

Allows creation of random instances of (many) arbitrary classes in Kotlin.
Works only on JVM.

## Why?
During testing we often encounter situations where instances of Value classes or domain classes
are required in order to test the underlying code. Many times it is not important what the values contain,
but that there are actual values the code under test can work with.

This lib allows to create such random instances, without the need to fill in
endless constructor arguments.

Another, more specific example could be testing of serialization/deserialization. While  oftentimes
it is possible to serialize a given instance, deserialization can be much harder, e.g. if deserialization
information (like specific types) is missing. In this case one could use this lib in order to simply create 
random instances with all values filled in order to automate testing of (de-)serialization.
## Usage

Check the releases/* branches to obtain recent version 

Using Gradle
```kotlin
testImplementation("net.datenstrudel:kotlin-fixture-magic:0.1.2")
```
Using Maven
```xml
<dependency>
  <groupId>net.datenstrudel</groupId>
  <artifactId>kotlin-fixture-magic</artifactId>
  <version>0.1.2</version>
</dependency>
```


## Example

Given the following value types:
```kotlin
data class Car<T : Engine>(val name: String, val age: Short, val engine: T)

interface Engine{
    fun supportsElectricity(): Boolean
}

data class ElectricEngine(val name: String) : Engine {
    override fun supportsElectricity() = true
}
```

Now, in order to create a random instance of `Car` you would:

```kotlin
val factory = FixtureFactory.build {}

val randomElectricCar = factory.createInstance<Car<ElectricEngine>()

println(randomElectricCar)

>>> Car(name=name_3#wYu, age=-11130, engine=ElectricEngine(name=name_LVRF)))
```

## Supported Features

 * Creation of primitive types and Strings
   `Boolean`, `Byte`, `Char`, `Short`, `Int`, `Long`, `Float`, `Double`, `String`
 * Creation of classes / class hierarchies that are instantiable by (private) constructors
 * `Enum`s - an enum's instance is chosen randomly
 * Creation of Arrays - Typed arrays and Kotlin Arrays (like `Array<Byte>` and `ByteArray`)
 * Creation of Basic collection classes: 
    * `Collection ` - creates list
    * `List`    
    * `Set`    
    * `Map`
 * Generic Types  
 * Creation of some java.time classes like:
    * `ZoneId`, `Instant`, `LocalDate`, `LocalDateTime`, `ZonedDateTime` 
 * `CustomCreator`s - allow you to manually implement instantiation strategy of types that are not supported out of the box
    or to override existing behavior for a given type
 * Bulk creation of instances by shared annotation or common super class => `FixtureFactory.bulkOperations(..)` returns a
   bulk creation factory
 * Customization of random lengths of primitive values and arrays (not complete)  (look what the `FixtureFactory.build{}` block has to offer)

### Custom creators
With `CustomCreator`s you can implement or override the instantiation strategy of a specific type.

In order to do so, implement the interface `CustomCreator` for the troubling type and register it on
creation of the `FixtureFactory`, like so:

```kotlin

val customStringCreator = object: CustomCreator<String> {
   override fun supports(clazz: KClass<*>) = clazz == String::class
   override fun create(fixtureFactory: FixtureFactory) = "I always want this string"
}

val factory = FixtureFactory.build{
    customCreators = arrayOf(customStringCreator)
}
```
Using the factory, created above, will now always put String instances as defined in `customStringCreator.create(..)`
into any parameters of type String that might be required to build the whole object hierarchy.

The `create(..)` function gets also the `FixtureFactory` which allows you to create random instances of parameters that might
be necessary to instantiate your custom type.

### Bulk creation

#### Create instances of sub types of `SomeSuperType`
```kotlin
val bulkFactory = FixtureFactory.build{}.bulkOperations("root.package.to.search")

bulkFactory.createInstancesOfSubclasses<SomeSuperType>()
```

#### Create instances of classes annotated with `@SomeAnnotation`
```kotlin
val bulkFactory = FixtureFactory.build{}.bulkOperations("root.package.to.search")

bulkFactory.createInstancesByAnnotation<SomeAnnotation>()
```

## Worth to know
 
  * The lib tries to use all constructors of a given type until it finds one that works. While searching for a working constructor,
    it starts with the one, having the shortest argument size

## Known issues / not supported
 * JVM only
 * Creation of arbitrary interface types is not supported (but can be solved by implementing a `CustomCreator` `)

## Contribution
Suggestions and pull requests are very welcome 💎

## License
Apache License Version 2.0