package net.datenstrudel.kotlin_fixture_magic

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

/**
 * Allows to create instances of classes in bulk by different properties of classes (e.g. sub types or annotations).
 * @param basePackage - base package to scan for (sub) classes or annotated classes
 * @param fixtureFactory - the [FixtureFactory] used to create random instances
 */
class BulkFixtureFactory(val basePackage: String, private val fixtureFactory: FixtureFactory = FixtureFactory.build {}) {

    private val reflections: Reflections = Reflections(ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage(basePackage))
        .setScanners(SubTypesScanner(), TypeAnnotationsScanner()))

    inline fun <reified T: Any> createInstancesOfSubclasses(): List<T> {
        return createInstancesOfSubclasses(T::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> createInstancesOfSubclasses(clazz: KClass<T>) : List<T> {
        val subTypesOf = reflections.getSubTypesOf(clazz.java)

        return subTypesOf.filter { !it.kotlin.isAbstract }
            .map { fixtureFactory.createInstance(it.kotlin.starProjectedType) as T }
    }

    inline fun <reified A : Annotation> createInstancesByAnnotation(): List<Any> {
        return createInstancesByAnnotation(A::class.java)
    }

    fun <A: Annotation> createInstancesByAnnotation(annotation: Class<A>): List<Any> {
        val matches = reflections.getTypesAnnotatedWith(annotation)

        return matches.filter { !it.kotlin.isAbstract }
            .map { fixtureFactory.createInstance(it.kotlin.starProjectedType) }

    }
}
