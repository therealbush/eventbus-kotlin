package me.bush.illnamethislater

import kotlin.reflect.KClass

/**
 * This class is not intended to be used externally, use [listener] instead. You *could* use this,
 * and it would work fine however you would have to specify the type explicitly. (ew!)
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
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
 * Creates a listener that can be held in a variable, returned from
 * a function or getter, or directly registered to an Eventbus.
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
 *
 * @param T The type of event to listen for. Inheritance has no effect here.
 * If [T] is a base class, subclass events will not be received.
 * @param priority The priority of this listener. This can be any integer.
 * Listeners with a higher [priority] will be invoked first.
 * @param parallel If a listener should be invoked in parallel with other [parallel] listeners, or sequentially. todo finish parallel
 * @param receiveCancelled If a listener should receive events that have been cancelled by previous listeners.
 * @param listener The body of the listener that will be invoked.
 */
inline fun <reified T : Any> listener(
    priority: Int = 0,
    parallel: Boolean = false,
    receiveCancelled: Boolean = false,
    noinline listener: (T) -> Unit
) = Listener(listener, T::class, priority, parallel, receiveCancelled)
