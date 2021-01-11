package net.datenstrudel.kotlin_fixture_magic

import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.time.*
import kotlin.random.Random
import kotlin.reflect.*
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

/**
 * This is the main artifact to interact with as client of this lib.
 *
 * The [FixtureFactory] allows you to create random instances of arbitrary types, given there
 * is a usable constructor.
 *
 * To obtain an instance call [FixtureFactory.build{}]
 *
 * To create a random instance call `factory.createInstance<MyClass>()`
 *
 * To perform buklkOperations acquire a [BulkFixtureFactory] by calling `factory.bulkOperations()`
 */
class FixtureFactory private constructor(
    val randomIntRange: IntRange,
    val randomStringLength: Int = 10,
    val randomArrayLength: Int = 10,
    val randomCollectionSize: Int = 10,
    private val customCreators: Set<CustomCreator<*>>
) {

    private constructor(builder: Builder) : this(
        randomIntRange = builder.randomIntRange,
        randomStringLength = builder.randomStringLength,
        randomArrayLength = builder.randomArrayLength,
        randomCollectionSize = builder.randomCollectionSize,
        customCreators = builder.customCreators.toSet()
    )

    private val rand = Random

    /**
     * @return the [BulkFixtureFactory] to create instances of annotated types or by super type.
     */
    fun bulkOperations(basePackage: String): BulkFixtureFactory {
        return BulkFixtureFactory(basePackage, this)
    }

    /**
     * Create random instance of supplied type `T`
     * @return instance of T with all its members initialized to random values
     */
    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> createInstance(): T{
        return createInstance(typeOf<T>(), null, null) as T
    }

    fun createInstance(type: KType, typeParams: Map<KTypeParameter, KType?>? = null, paramName: String? = null): Any {
        val resTypeParams = determineAndMergeNewTypeParams(type, typeParams)

        return createInstance(type.classifier as KClass<*>, type, resTypeParams, paramName)
    }

    private fun determineAndMergeNewTypeParams(type: KType, parentTypeParams: Map<KTypeParameter, KType?>?, ): Map<KTypeParameter, KType?> {
        val resTypeParams = mutableMapOf<KTypeParameter, KType?>()

        // Create a type param -> actual runtime type mapping
        val newTypeParams = type.jvmErasure.typeParameters.zip(
            type.arguments.map { it.type }
        ).toMap()

        parentTypeParams?.let { resTypeParams.putAll(it) }

        newTypeParams.forEach { (k, v) ->
            val newValue = resTypeParams[v!!.classifier] ?: v  // Take actual (parent's) type param down to nested usage
            resTypeParams[k] = newValue
        }
        return resTypeParams
    }

    private fun createInstance(clazz: KClass<*>, type: KType, typeParams: Map<KTypeParameter, KType?>? = null, paramName: String? = null): Any{
        val customCreation = createCustomTypeOrNull(clazz, typeParams, paramName)
        customCreation?.let { return it }

        val enumSelection = resolveEnumOrNull(clazz)
        enumSelection?.let { return it }

        val stdInstance = createStandardInstanceOrNull(clazz, type, paramName)
        stdInstance?.let { return it }

        return constructInstance(clazz, typeParams)
    }

    private fun constructInstance(clazz: KClass<*>, typeParams: Map<KTypeParameter, KType?>?): Any {
        val constructors = clazz.constructors
            .sortedBy { it.parameters.size }

        var lastThrow: Throwable? = null
        for (constructor in constructors) {
            try {
                val arguments = constructor.parameters
                    .map { it.type to it.name }
                    .map {
                        // look up specific type for generic parameter or, if non generic, take the type as is
                        val typeToCreate = typeParams?.get(it.first.classifier) ?: it.first
                        createInstance(typeToCreate, typeParams, it.second)
                    }
                    .toTypedArray()
                constructor.isAccessible = true
                return constructor.call(*arguments)
            } catch (e: Throwable) {
                lastThrow = e
                log.debug("Couldn't create instance", e)
            }
        }
        throw lastThrow ?: NoUsableConstructorFound("Couldn't create instance of type=${clazz.jvmName}")
    }

    private fun resolveEnumOrNull(clazz: KClass<*>): Any? {
        if(!clazz.isSubclassOf(Enum::class)) return null

        val enumConsts = clazz.java.enumConstants
        val randIdx = rand.nextInt(0, enumConsts.size - 1)

        return enumConsts[randIdx]
    }

    private fun createCustomTypeOrNull(clazz: KClass<*>, typeParams: Map<KTypeParameter, KType?>?, paramName: String? ): Any? {
        val creator = customCreators.find { it.supports(clazz) }
        return creator?.let {
            creator.create(this, typeParams, paramName)
        }
    }

    private fun createStandardInstanceOrNull(clazz: KClass<*>, type: KType, paramName: String?): Any? {
        var res: Any? = when (clazz) {
            // Primitives
            Boolean::class -> rand.nextBoolean()
            Byte::class -> rand.nextBytes(1)[0]
            Char::class -> rand.nextInt(32, 126).toChar()
            Short::class -> rand.nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            Int::class -> rand.nextInt(randomIntRange.first, randomIntRange.last)
            Long::class -> rand.nextLong()
            Float::class -> rand.nextFloat()
            Double::class -> rand.nextDouble()
            String::class -> createRandomString(paramName)

            // Arrays
            BooleanArray:: class -> createBooleanArray(type)
            ByteArray::class -> createByteArray(type)
            CharArray::class -> createCharArray(type)
            ShortArray::class -> createShortArray(type)
            IntArray::class -> createIntArray(type)
            LongArray::class -> createLongArray(type)
            FloatArray::class -> createFloatArray(type)
            DoubleArray::class -> createDoubleArray(type)

            // Collections
            Map::class -> createRandomMap(type, paramName)
            Set::class -> createRandomList(type, paramName).toSet()
            List::class, Collection::class -> createRandomList(type, paramName)

            // Java Time
            ZoneId::class -> ZoneId.systemDefault()
            Instant::class -> Instant.ofEpochMilli(rand.nextLong())
            LocalDate::class -> LocalDate.now().minusDays(rand.nextInt().toShort().toLong())
            LocalDateTime::class -> LocalDateTime.now().minusDays(rand.nextInt().toShort().toLong())
            ZonedDateTime::class -> ZonedDateTime.now().minusDays(rand.nextInt().toShort().toLong())

            else -> null
        }

        // not all types can be matched by KClass representation
        res = res ?: when (clazz.qualifiedName ?: "") {
            "kotlin.Array" -> createGenericArray(type, paramName)
            else -> null
        }

        return res
    }

    private fun createRandomString(paramName: String?): String {
        val paramLength = paramName?.length ?: 0
        val randLength = this.randomStringLength - paramLength
        if (randLength < 1) throw InvalidFixtureConfigException("randomStringLength too small for random string generation")

        val res = (1 until randLength)
            .map { rand.nextInt(32, 126).toChar() }
            .joinToString(separator = "") { "$it" }

        return paramName?.let { it + "_" + res } ?: res
    }

    private fun createRandomMap(type: KType, paramName: String?): Map<Any?, Any?> {
        val keyType = type.arguments[0].type!!
        val valType = type.arguments[1].type!!

        val keys = (1..randomCollectionSize).map { createInstance(keyType, null, paramName) }
        val values = (1..randomCollectionSize).map { createInstance(valType, null, paramName) }

        return mutableMapOf(* keys.zip(values).toTypedArray())
    }

    private fun createRandomList(type: KType, paramName: String?): List<Any?> {
        val elemType = type.arguments[0].type!!
        return (1..randomCollectionSize)
            .map { createInstance(elemType, null, paramName) }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createBooleanArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Boolean>()) as Boolean }

        return if (type.arguments.isEmpty()) {
            elements.toBooleanArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createByteArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Byte>()) as Byte }

        return if (type.arguments.isEmpty()) {
            elements.toByteArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createCharArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Char>()) as Char }

        return if (type.arguments.isEmpty()) {
            elements.toCharArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createShortArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Short>()) as Short }

        return if (type.arguments.isEmpty()) {
            elements.toShortArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createIntArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Int>()) as Int }

        return if (type.arguments.isEmpty()) {
            elements.toIntArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createLongArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Long>()) as Long }

        return if (type.arguments.isEmpty()) {
            elements.toLongArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createFloatArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Float>()) as Float }

        return if (type.arguments.isEmpty()) {
            elements.toFloatArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun createDoubleArray(type: KType): Any {
        val elements = (1..randomArrayLength).map { createInstance(typeOf<Double>()) as Double }

        return if (type.arguments.isEmpty()) {
            elements.toDoubleArray()
        } else {  // an Array<Int> has an argument
            elements.toTypedArray()
        }
    }

    private fun createGenericArray(type: KType, paramName: String?): Any {
        val elemType = type.arguments[0].type!!
        val elements = (1..randomArrayLength).map { createInstance(elemType, null, paramName) }

        return toTypedArray(elements, elemType)
    }

    /**
     * The `list` argument supplied usually lacks the type of the generic argument(s), which makes
     * `kotlin.Collection.toTypedArray()` return an `Array<Object>`. This function allows to create the correctly typed
     * array by using the supplied `elemType`
     */
    @Suppress("UNCHECKED_CAST")
    private fun toTypedArray(list: List<Any>, elemType: KType): Any {
        val resArray: Array<Any> =
            java.lang.reflect.Array.newInstance((elemType.classifier as KClass<*>).java, randomArrayLength) as Array<Any>
        list.forEachIndexed{ i, it -> resArray[i] = it }
        return resArray
    }

    class Builder(
        var randomIntRange: IntRange = IntRange(0, Int.MAX_VALUE),
        var randomStringLength: Int = 10,
        var randomArrayLength: Int = 10,
        var randomCollectionSize: Int = 10,
        var customCreators: Array<CustomCreator<*>> = arrayOf()
    ) {
        fun build() = FixtureFactory(this)
    }

    companion object {
        val log = LoggerFactory.getLogger(this::class.java)

        /**
         * Obtain an instance of [FixtureFactory]. Customize by applying parameters in expected `block`
         */
        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }
}

class NoUsableConstructorFound(msg: String) : RuntimeException(msg)
class InvalidFixtureConfigException(msg: String) : RuntimeException(msg)