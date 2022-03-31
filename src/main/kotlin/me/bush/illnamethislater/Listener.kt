package me.bush.illnamethislater

import kotlin.reflect.KClass

/**
 * This class is not intended to be used externally, use [listener] instead. You *could* use this,
 * and it would work fine however you would have to specify the type explicitly. (ew!)
 *
 * @author bush
 * @since 1.0.0
 */
class Listener @PublishedApi internal constructor(
    listener: (Nothing) -> Unit,
    internal val type: KClass<*>,
    internal var priority: Int = 0,
    internal var parallel: Boolean = false,
    internal var receiveCancelled: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    // Generics have no benefit here,
    // it is easier just to force cast.
    internal val listener = listener as (Any) -> Unit
    internal lateinit var subscriber: Any
}

/**
 * doc later
 *
 * @author bush
 * @since 1.0.0
 */
inline fun <reified T : Any> listener(
    priority: Int = 0,
    parallel: Boolean = false,
    receiveCancelled: Boolean = false,
    noinline listener: (T) -> Unit
) = Listener(listener, T::class, priority, parallel, receiveCancelled)
