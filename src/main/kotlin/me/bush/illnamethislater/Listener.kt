package me.bush.illnamethislater

import kotlin.reflect.KClass

/**
 * This class is not intended to be used externally, use [listener] instead.
 *
 * You *could* use this, and it would work fine, however
 * [listener] lets us use reified types for a cleaner look.
 *
 * @author bush
 * @since 1.0.0
 */
class Listener @PublishedApi internal constructor(
    listener: (Nothing) -> Unit,
    internal var priority: Int = 0,
    internal var parallel: Boolean = false,
    internal var receiveCancelled: Boolean = false,
    internal val type: KClass<*>
) {
    @Suppress("UNCHECKED_CAST")
    internal val listener = listener as (Any) -> Unit
    internal lateinit var subscriber: Any
}

/**
 *
 */
inline fun <reified T : Any> listener(
    priority: Int = 0,
    parallel: Boolean = false,
    receiveCancelled: Boolean = false,
    noinline listener: (T) -> Unit
) = Listener(listener, priority, parallel, receiveCancelled, T::class)
