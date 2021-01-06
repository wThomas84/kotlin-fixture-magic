package net.datenstrudel.fixtmagic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FixtFactoryTest {

    private val fixtFactory: FixtFactory = FixtFactory()

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `it should create instance of simple class`() {
        val res: SimpleClass = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(SimpleClass::class.java)
    }

    @Test
    fun `it should create instance of class with complex param`() {
        val res: SimpleClassWithComplexParam = fixtFactory.createInstance()
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

        val res: WithPrimitives = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithPrimitives::class.java)

        log.info("$res")
        assertThat(res.i).isBetween(fixtFactory.randomIntRange.first, fixtFactory.randomIntRange.last)
        assertThat(res.str).hasSize(fixtFactory.randomStringLength)
    }

    class WithGenericArray(val a: Array<SimpleClass>)

    @Test
    fun `it should create instance of simple class with generic Array param`() {

        val res: WithGenericArray = fixtFactory.createInstance()

        assertThat(res).isInstanceOf(WithGenericArray::class.java)
        assertThat(res.a).isInstanceOf(Array<SimpleClass>::class.java)
            .hasSize(fixtFactory.randomArrayLength)


        log.info("$res")
    }


    class WithBooleanArray(val a: BooleanArray)

    @Test
    fun `it should create instance of simple class with BooleanArray param`() {

        val res: WithBooleanArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithBooleanArray::class.java)
        assertThat(res.a).isInstanceOf(BooleanArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfBoolean(val a: Array<Boolean>)

    @Test
    fun `it should create instance of simple class with Array of Boolean param`() {

        val res: WithArrayOfBoolean = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfBoolean::class.java)
        assertThat(res.a).isInstanceOf(Array<Boolean>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }


    class WithByteArray(val a: ByteArray)

    @Test
    fun `it should create instance of simple class with ByteArray param`() {

        val res: WithByteArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithByteArray::class.java)
        assertThat(res.a).isInstanceOf(ByteArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfByte(val a: Array<Byte>)

    @Test
    fun `it should create instance of simple class with Array of Byte param`() {

        val res: WithArrayOfByte = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfByte::class.java)
        assertThat(res.a).isInstanceOf(Array<Byte>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithCharArray(val a: CharArray)

    @Test
    fun `it should create instance of simple class with CharArray param`() {

        val res: WithCharArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithCharArray::class.java)
        assertThat(res.a).isInstanceOf(CharArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfChar(val a: Array<Char>)

    @Test
    fun `it should create instance of simple class with Array of Char param`() {

        val res: WithArrayOfChar = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfChar::class.java)
        assertThat(res.a).isInstanceOf(Array<Char>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }


    class WithShortArray(val a: ShortArray)

    @Test
    fun `it should create instance of simple class with ShortArray param`() {

        val res: WithShortArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithShortArray::class.java)
        assertThat(res.a).isInstanceOf(ShortArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfShort(val a: Array<Short>)

    @Test
    fun `it should create instance of simple class with Array of Short param`() {

        val res: WithArrayOfShort = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfShort::class.java)
        assertThat(res.a).isInstanceOf(Array<Short>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }


    class WithArrayOfInt(val a: Array<Int>)

    @Test
    fun `it should create instance of simple class with Array of Int param`() {
        val res: WithArrayOfInt = fixtFactory.createInstance()

        assertThat(res).isInstanceOf(WithArrayOfInt::class.java)
        assertThat(res.a).isInstanceOf(Array<Int>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithIntArray(val a: IntArray)

    @Test
    fun `it should create instance of simple class with IntArray param`() {
        val res: WithIntArray = fixtFactory.createInstance()

        assertThat(res).isInstanceOf(WithIntArray::class.java)
        assertThat(res.a).isInstanceOf(IntArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithLongArray(val a: LongArray)

    @Test
    fun `it should create instance of simple class with LongArray param`() {

        val res: WithLongArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithLongArray::class.java)
        assertThat(res.a).isInstanceOf(LongArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfLong(val a: Array<Long>)

    @Test
    fun `it should create instance of simple class with Array of Long param`() {

        val res: WithArrayOfLong = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfLong::class.java)
        assertThat(res.a).isInstanceOf(Array<Long>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithFloatArray(val a: FloatArray)

    @Test
    fun `it should create instance of simple class with FloatArray param`() {

        val res: WithFloatArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithFloatArray::class.java)
        assertThat(res.a).isInstanceOf(FloatArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfFloat(val a: Array<Float>)

    @Test
    fun `it should create instance of simple class with Array of Float param`() {

        val res: WithArrayOfFloat = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfFloat::class.java)
        assertThat(res.a).isInstanceOf(Array<Float>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithDoubleArray(val a: DoubleArray)

    @Test
    fun `it should create instance of simple class with DoubleArray param`() {

        val res: WithDoubleArray = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithDoubleArray::class.java)
        assertThat(res.a).isInstanceOf(DoubleArray::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }

    class WithArrayOfDouble(val a: Array<Double>)

    @Test
    fun `it should create instance of simple class with Array of Double param`() {

        val res: WithArrayOfDouble = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithArrayOfDouble::class.java)
        assertThat(res.a).isInstanceOf(Array<Double>::class.java)
            .hasSize(fixtFactory.randomArrayLength)

        log.info("$res")
    }


    data class WithList(val l: List<String>)
    //    @Test
    fun `it should create instance of simple class with List param`() {

        val res: WithList = fixtFactory.createInstance()
        assertThat(res).isInstanceOf(WithList::class.java)
        assertThat(res.l).isInstanceOf(List::class.java)

        log.info("$res")
    }
}