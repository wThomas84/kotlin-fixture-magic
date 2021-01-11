package net.datenstrudel.kotlin_fixture_magic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BulkFixtureFactoryTest {

    @Test
    fun `createInstancesOfSubclasses() should create instances for all sub classes and ignore abstract sub classes`() {

        val factory = BulkFixtureFactory(BASE_PACKAGE)

        val res = factory.createInstancesOfSubclasses<Parent<*>>()

        val resChild1 = res.filterIsInstance(Child1::class.java).first()
        assertThat(resChild1.child1A).isNotBlank()
        assertThat(resChild1.parentA).isNotBlank()

        val resChild2 = res.filterIsInstance(Child2::class.java).first()
        assertThat(resChild2.child2A).isNotBlank()
        assertThat(resChild2.parentA).isNotNull()

        assertThat(res).hasSize(2)
    }

    @Test
    fun `createInstancesOfAnnotatedTypes() should create instances for all sub classes`() {
        val factory = BulkFixtureFactory(BASE_PACKAGE)

        val res = factory.createInstancesByAnnotation<InstTarget>()

        val res1 = res.filterIsInstance(ByAnnotation1::class.java).first()
        assertThat(res1.a).isNotBlank()

        val res2 = res.filterIsInstance(ByAnnotation2::class.java).first()
        assertThat(res2.a).isNotBlank()

        assertThat(res).hasSize(2)
    }

    @Test
    fun `createInstancesOfAnnotatedTypes() should ignore non instantiable annotated type but instantiate inheriting sub typees`() {
        val factory = BulkFixtureFactory(BASE_PACKAGE)

        val res = factory.createInstancesByAnnotation<NonInstantiable>()
        assertThat(res).hasSize(2)
        assertThat(res.filterIsInstance(Child1::class.java).any() ).isTrue()
        assertThat(res.filterIsInstance(Child2::class.java).any() ).isTrue()

    }


    @NonInstantiable
    abstract class Parent<T>(val parentA: T)

    class Child1(val child1A: String, child1B: String) : Parent<String>(child1B)

    class Child2(val child2A: String, child2B: Int) : Parent<Int>(child2B)

    abstract class AbstractChild(val child2A: String, child2B: Int) : Parent<Int>(child2B)

    @InstTarget
    class ByAnnotation1(val a: String)
    @InstTarget
    class ByAnnotation2(val a: String)

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class InstTarget

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class NonInstantiable

    companion object {
        const val BASE_PACKAGE = "net.datenstrudel.kotlin_fixture_magic"

    }
}