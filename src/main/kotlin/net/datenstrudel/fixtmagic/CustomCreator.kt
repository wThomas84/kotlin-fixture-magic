package net.datenstrudel.fixtmagic

import kotlin.reflect.KClass

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
    fun create(fixtureFactory: FixtureFactory): T
}