package net.datenstrudel.kotlin_fixture_magic

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter

/**
 * Gives full control over instantiation of a specific type.
 * Can be used as well to override creation of already supported types.
 */
interface CustomCreator<T: Any> {

    fun supports(clazz: KClass<*>): Boolean

    /**
     * @param fixtureFactory to be used to create random child instances that might be necessary to instantiate
     * the return value of this function
     * @return an instance of clazz where [this.supports()] returns true for
     */
    fun create(fixtureFactory: FixtureFactory, typeParams: Map<KTypeParameter, KType?>?, paramName: String?): T
}