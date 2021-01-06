package net.datenstrudel.fixtmagic

import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.typeOf

class FixtFactory {

    var randomIntRange = IntRange(100, Int.MAX_VALUE)
        private set
    var randomStringLength = 50
        private set
    var randomArrayLength = 10
        private set


    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> createInstance(): T{
        return createInstance(T::class, typeOf<T>()) as T
    }

    private fun createInstance(type: KType, paramName: String? = null): Any {
        return createInstance(type.classifier as KClass<*>, type, paramName)
    }

    fun createInstance(clazz: KClass<*>, type: KType, paramName: String? = null): Any{
        val primitive = createStandardInstanceOrNull(clazz, type, paramName)
        primitive?.let { return it }

        val constructors = clazz.constructors
            .sortedBy { it.parameters.size }

        var lastThrow: Throwable? = null
        for (constructor in constructors) {
            try {
                val arguments = constructor.parameters
                    .map { it.type to it.name }
                    .map {
                       createInstance(it.first, it.second)
                    }
                    .toTypedArray()
                    return constructor.call(*arguments)
            } catch (e: Throwable) {
                lastThrow = e
                log.debug("Couldn't create instance", e)
            }
        }
        throw lastThrow?.let { it } ?: NoUsableConstructorFound("Couldn't create instance of type=${clazz.jvmName}")
    }


    private val rand = Random

    private fun createStandardInstanceOrNull(clazz: KClass<*>, type: KType, paramName: String?): Any? {
        var res: Any? = when (clazz) {
            Boolean::class -> rand.nextBoolean()
            Byte::class -> rand.nextBytes(1)[0]
            Char::class -> rand.nextInt(32, 126).toChar()
            Short::class -> rand.nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            Int::class -> rand.nextInt(randomIntRange.first, randomIntRange.last)
            Long::class -> rand.nextLong()
            Float::class -> rand.nextFloat()
            Double::class -> rand.nextDouble()
            String::class -> createRandomString(paramName)

            BooleanArray:: class -> createBooleanArray(clazz, type, paramName)
            ByteArray::class -> createByteArray(clazz, type, paramName)
            CharArray::class -> createCharArray(clazz, type, paramName)
            ShortArray::class -> createShortArray(clazz, type, paramName)
            IntArray::class -> createIntArray(clazz, type, paramName)
            LongArray::class -> createLongArray(clazz, type, paramName)
            FloatArray::class -> createFloatArray(clazz, type, paramName)
            DoubleArray::class -> createDoubleArray(clazz, type, paramName)
            else -> null
        }

        // not all types can be matched by KClass representation
        res = res ?: when (clazz.qualifiedName ?: "") {
            "kotlin.Array" -> createGenericArray(clazz, type, paramName)
            else -> null
        }

        return res
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createBooleanArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Boolean>()) as Boolean }

        return if (type.arguments.isEmpty()) {
            elements.toBooleanArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createByteArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Byte>()) as Byte }

        return if (type.arguments.isEmpty()) {
            elements.toByteArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createCharArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Char>()) as Char }

        return if (type.arguments.isEmpty()) {
            elements.toCharArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createShortArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Short>()) as Short }

        return if (type.arguments.isEmpty()) {
            elements.toShortArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createIntArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Int>()) as Int }

        return if (type.arguments.isEmpty()) {
            elements.toIntArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createLongArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Long>()) as Long }

        return if (type.arguments.isEmpty()) {
            elements.toLongArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createFloatArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Float>()) as Float }

        return if (type.arguments.isEmpty()) {
            elements.toFloatArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createDoubleArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Double>()) as Double }

        return if (type.arguments.isEmpty()) {
            elements.toDoubleArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    private fun createGenericArray(clazz: KClass<*>, type: KType, paramName: String?): Any {
        val elemType = type.arguments[0].type!!
        val elements = (1..randomArrayLength).map { createInstance(elemType) }

        return toTypedArray(elements, elemType)
    }

    /**
     * The `list` argument supplied usually lacks the type of the generic argument(s), which makes
     * `kotlin.Collection.toTypedArray()` return an `Array<Object>`. This function allows to create the correctly typed
     * array by ussing the supplied `elemType`
     */
    private fun toTypedArray(list: List<Any>, elemType: KType): Any {
        val resArray: Array<Any> =
            java.lang.reflect.Array.newInstance((elemType.classifier as KClass<*>).java, randomArrayLength) as Array<Any>
        list.forEachIndexed{ i, it -> resArray[i] = it }
        return resArray
    }

    private fun createRandomString(paramName: String?): String {
        val paramLength = paramName?.let { it.length } ?: 0
        val randLength = this.randomStringLength - paramLength
        if (randLength < 1) throw InvalidFixtConfigException("randomStringLength too small for random string generation")

        val res = (1 until randLength)
            .map { rand.nextInt(32, 126).toChar() }
            .joinToString(separator = "") { "$it" }

        return paramName?.let { it + "_" + res } ?: res
    }


    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        fun <T : Any> cast(any: Any, clazz: KClass<out T>): T = clazz.javaObjectType.cast(any)
    }
}

class NoUsableConstructorFound(msg: String) : RuntimeException(msg)
class InvalidFixtConfigException(msg: String) : RuntimeException(msg)