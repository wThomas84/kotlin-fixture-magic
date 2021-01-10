package net.datenstrudel.fixtmagic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import kotlin.reflect.KClass


class FixtureFactoryTest {

    private val fixtureFactory: FixtureFactory = FixtureFactory.build{}

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `it should create instance of simple class`() {
        val res: SimpleClass = fixtureFactory.createInstance()
        assertThat(res).isInstanceOf(SimpleClass::class.java)
    }

    @Test
    fun `it should create instance of class with complex param`() {
        val res: SimpleClassWithComplexParam = fixtureFactory.createInstance()
        assertThat(res).isInstanceOf(SimpleClassWithComplexParam::class.java)
        assertThat(res.paramA).isInstanceOf(SimpleClass::class.java)
    }

    @Test
    fun `it should create instance of simple class with primitive params`() {
        data class WithPrimitives(
            val bo: Boolean,
            val b: Byte,
            val s: Short,
            val i: Int,
            val l: Long,
            val d: Double,
            val f: Float,
            val c: Char,
            val str: String
        )

        val res: WithPrimitives = fixtureFactory.createInstance()
        assertThat(res).isInstanceOf(WithPrimitives::class.java)

        log.info("$res")
        assertThat(res.i).isBetween(fixtureFactory.randomIntRange.first, fixtureFactory.randomIntRange.last)
        assertThat(res.str).hasSize(fixtureFactory.randomStringLength)
    }


    enum class TestEnum{
        ONE, TWO
    }

    @Test
    fun `it should support enums by randomly returning an instance`(){
        val res = fixtureFactory.createInstance<TestEnum>()

        assertThat(res).isInstanceOf(TestEnum::class.java)
    }

    data class TypeWithEnumParam(val a: TestEnum)
    @Test
    fun `it should support enums as parameter`(){

        val res = fixtureFactory.createInstance<TypeWithEnumParam>()

        assertThat(res.a).isInstanceOf(TestEnum::class.java)
    }

    @Test
    fun `it should be creatable by Builder with default params`(){
        val res = FixtureFactory.build{}

        assertThat(res.randomArrayLength).isEqualTo(10)
        assertThat(res.randomCollectionSize).isEqualTo(10)
        assertThat(res.randomStringLength).isEqualTo(10)
        assertThat(res.randomIntRange).isEqualTo(IntRange(0, Int.MAX_VALUE))
    }

    @Test
    fun `it should be creatable by Builder with user defined params`(){
        val res = FixtureFactory.build{
            randomArrayLength = 11
            randomCollectionSize = 11
            randomStringLength = 11
            randomIntRange = IntRange(-10, 10)
        }

        assertThat(res.randomArrayLength).isEqualTo(11)
        assertThat(res.randomCollectionSize).isEqualTo(11)
        assertThat(res.randomStringLength).isEqualTo(11)
        assertThat(res.randomIntRange).isEqualTo(IntRange(-10, 10))

    }

    @Test
    fun `bulkOperations() - should return correctly configureed BulkFixtureFactory`() {
        val res = fixtureFactory.bulkOperations("org.my.package")

        assertThat(res.basePackage).isEqualTo("org.my.package")
    }

    data class GenericType<T>(val b: T)

    data class ParentGenericType<T>(val a: ChildGenericType<T>)
    data class ChildGenericType<T>(val a: T)

    data class ParentMixedGenericType<A : Serializable, B>(val a: A, val b: ChildGenericType<B>)

    @Nested
    inner class Generics {

        @Test
        fun `it should create instance of generic class`() {
            val res: GenericType<Short> = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(GenericType::class.java)
            log.info("$res")
        }

        @Test
        fun `it should create instance of generic class with many type params`() {
            data class ManyGenericParamType<T, O, M>(val a: T, val b: O, val c: M)

            val res: ManyGenericParamType<Long, String, Short> = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(ManyGenericParamType::class.java)
            assertThat(res.b).isNotBlank()
            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with generic param`() {
            data class WithGenericParam(val a: GenericType<String>)

            val res: WithGenericParam = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithGenericParam::class.java)
            assertThat(res.a.b).isNotBlank()

            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with generic complex param`() {
            data class WithGenericParam(val a: GenericType<SimpleClass>)

            val res: WithGenericParam = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithGenericParam::class.java)
            assertThat(res.a.b).isInstanceOf(SimpleClass::class.java)
            log.info("$res")
        }

        @Test
        fun `it should create instance of generic class with nested type param`() {
            val res: ParentGenericType<String> = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(ParentGenericType::class.java)
            assertThat(res.a.a).isNotBlank()
            log.info("$res")
        }

        @Test
        fun `it should create instance of generic class with nested and mixed type params`() {
            val res: ParentMixedGenericType<String, String> = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(ParentMixedGenericType::class.java)
            assertThat(res.a).isNotBlank()
            assertThat(res.b.a).isNotBlank()
            log.info("$res")
        }

        @Test
        fun `it should create instance of generic class with triple nested and mixed type params`() {
            val res: ParentMixedGenericType<String, ChildGenericType<ChildGenericType<String>>> = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(ParentMixedGenericType::class.java)
            assertThat(res.a).isNotBlank()
            assertThat(res.b.a.a.a).isNotBlank()
            log.info("$res")
        }

    }

    @Nested
    inner class ArrayTests {

        @Test
        fun `it should create instance of simple class with generic Array param`() {
            class WithGenericArray(val a: Array<SimpleClass>)

            val res: WithGenericArray = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithGenericArray::class.java)
            assertThat(res.a).isInstanceOf(Array<SimpleClass>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with BooleanArray param`() {
            class WithBooleanArray(val a: BooleanArray)

            val res: WithBooleanArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithBooleanArray::class.java)
            assertThat(res.a).isInstanceOf(BooleanArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Boolean param`() {
            class WithArrayOfBoolean(val a: Array<Boolean>)

            val res: WithArrayOfBoolean = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfBoolean::class.java)
            assertThat(res.a).isInstanceOf(Array<Boolean>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with ByteArray param`() {
            class WithByteArray(val a: ByteArray)

            val res: WithByteArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithByteArray::class.java)
            assertThat(res.a).isInstanceOf(ByteArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Byte param`() {
            class WithArrayOfByte(val a: Array<Byte>)

            val res: WithArrayOfByte = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfByte::class.java)
            assertThat(res.a).isInstanceOf(Array<Byte>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with CharArray param`() {
            class WithCharArray(val a: CharArray)

            val res: WithCharArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithCharArray::class.java)
            assertThat(res.a).isInstanceOf(CharArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Char param`() {
            class WithArrayOfChar(val a: Array<Char>)

            val res: WithArrayOfChar = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfChar::class.java)
            assertThat(res.a).isInstanceOf(Array<Char>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with ShortArray param`() {
            class WithShortArray(val a: ShortArray)

            val res: WithShortArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithShortArray::class.java)
            assertThat(res.a).isInstanceOf(ShortArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Short param`() {
            class WithArrayOfShort(val a: Array<Short>)

            val res: WithArrayOfShort = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfShort::class.java)
            assertThat(res.a).isInstanceOf(Array<Short>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with Array of Int param`() {
            class WithArrayOfInt(val a: Array<Int>)

            val res: WithArrayOfInt = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithArrayOfInt::class.java)
            assertThat(res.a).isInstanceOf(Array<Int>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with IntArray param`() {
            class WithIntArray(val a: IntArray)

            val res: WithIntArray = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithIntArray::class.java)
            assertThat(res.a).isInstanceOf(IntArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with LongArray param`() {
            class WithLongArray(val a: LongArray)

            val res: WithLongArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithLongArray::class.java)
            assertThat(res.a).isInstanceOf(LongArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Long param`() {
            class WithArrayOfLong(val a: Array<Long>)

            val res: WithArrayOfLong = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfLong::class.java)
            assertThat(res.a).isInstanceOf(Array<Long>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with FloatArray param`() {
            class WithFloatArray(val a: FloatArray)

            val res: WithFloatArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithFloatArray::class.java)
            assertThat(res.a).isInstanceOf(FloatArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Float param`() {
            class WithArrayOfFloat(val a: Array<Float>)

            val res: WithArrayOfFloat = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfFloat::class.java)
            assertThat(res.a).isInstanceOf(Array<Float>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with DoubleArray param`() {
            class WithDoubleArray(val a: DoubleArray)

            val res: WithDoubleArray = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithDoubleArray::class.java)
            assertThat(res.a).isInstanceOf(DoubleArray::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }


        @Test
        fun `it should create instance of simple class with Array of Double param`() {
            class WithArrayOfDouble(val a: Array<Double>)

            val res: WithArrayOfDouble = fixtureFactory.createInstance()
            assertThat(res).isInstanceOf(WithArrayOfDouble::class.java)
            assertThat(res.a).isInstanceOf(Array<Double>::class.java)
                .hasSize(fixtureFactory.randomArrayLength)

            log.info("$res")
        }

    }

    @Nested
    inner class CollectionTests {
        @Test
        fun `it should create instance of simple class with List param`() {
            data class WithList(val l: List<String>)

            val res: WithList = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithList::class.java)
            assertThat(res.l).isInstanceOf(List::class.java)
                .hasSize(fixtureFactory.randomCollectionSize)
            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with Set param`() {
            data class WithSet(val l: Set<String>)

            val res: WithSet = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithSet::class.java)
            assertThat(res.l).isInstanceOf(Set::class.java)
                .hasSize(fixtureFactory.randomCollectionSize)

            log.info("$res")
        }

        @Test
        fun `it should create instance of simple class with Map param`() {
            data class WithMap(val l: Map<String, Double>)

            val res: WithMap = fixtureFactory.createInstance()

            assertThat(res).isInstanceOf(WithMap::class.java)
            assertThat(res.l).isInstanceOf(Map::class.java)
                .hasSize(fixtureFactory.randomCollectionSize)

            log.info("$res")
        }

    }

    @Nested
    inner class JavaTimeTests {

        @Test
        fun `it should create instance of class with Instant param`() {
            data class WithInstant(val a: Instant)

            val res: WithInstant = fixtureFactory.createInstance()
            assertThat(res.a).isInstanceOf(Instant::class.java)
            log.info("$res")
        }

        @Test
        fun `it should create instance of class with LocalDate param`() {
            data class WithLocalDate(val a: LocalDate)

            val res: WithLocalDate = fixtureFactory.createInstance()
            assertThat(res.a).isInstanceOf(LocalDate::class.java)
            log.info("$res")
        }

        @Test
        fun `it should create instance of class with LocalDateTime param`() {
            data class WithLocalDateTime(val a: LocalDateTime)

            val res: WithLocalDateTime = fixtureFactory.createInstance()
            assertThat(res.a).isInstanceOf(LocalDateTime::class.java)
            log.info("$res")
        }

        @Test
        fun `it should create instance of class with ZonedDateTime param`() {
            data class WithZonedDateTime(val a: ZonedDateTime)

            val res: WithZonedDateTime = fixtureFactory.createInstance()
            assertThat(res.a).isInstanceOf(ZonedDateTime::class.java)
            log.info("$res")
        }

    }

    class PrivateCreationType private constructor(){
        lateinit var a: String

        companion object {
            fun createInstance(a: String): PrivateCreationType {
                val res = PrivateCreationType()
                res.a = a
                return res
            }
        }
        override fun toString(): String {
            return "PrivateCreationType(a='$a')"
        }
    }

    @Nested
    inner class CustomCreatorTests {

        @RepeatedTest(5)
        fun `it should allow to override creation of already supported type` (){
            val customIntCreator = object: CustomCreator<Int> {
                override fun supports(clazz: KClass<*>) = clazz == Int::class
                override fun create(fixtureFactory: FixtureFactory) = 42
            }

            val factory = FixtureFactory.build { customCreators = arrayOf(customIntCreator) }

            assertThat(factory.createInstance<Int>()).isEqualTo(42)
        }

        @Test
        fun `it should allow to create types, having no accessible constructor` (){
            val customCreator = object: CustomCreator<PrivateCreationType> {
                override fun supports(clazz: KClass<*>) = clazz == PrivateCreationType::class
                override fun create(fixtureFactory: FixtureFactory) = PrivateCreationType.createInstance("testString")
            }
            val factory = FixtureFactory.build { customCreators = arrayOf(customCreator) }

            val res = factory.createInstance<PrivateCreationType>()

            assertThat(res.a).isEqualTo("testString")
            log.info("$res")
        }
    }

}