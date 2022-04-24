package me.bush.eventbuskotlin

import java.util.function.Consumer
import kotlin.reflect.KClass

/**
 * This class is not intended to be used externally, use [listener] instead. You *could* use this,
 * and it would work fine however you would have to specify the type explicitly. (ew!)
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#creating-a-listener)
 *
 * @author bush
 * @since 1.0.0
 */
class Listener @PublishedApi internal constructor(
    listener: (Nothing) -> Unit,
    internal val type: KClass<*>,
    internal val priority: Int = 0,
    internal val parallel: Boolean = false,
    internal val receiveCancelled: Boolean = false
) {
    @Suppress("UNCHECKED_CAST")
    // Generics have no benefit here.
    internal val listener = listener as (Any) -> Unit
    internal var subscriber: Any? = null
}

/**
 * Creates a listener that can be held in a variable or returned from a function
 * or getter belonging to an object to be subscribed with [EventBus.subscribe],
 * or directly registered to an [EventBus] with [EventBus.register].
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#creating-a-listener)
 *
 * @param T                The **exact** (no inheritance) type of event to listen for.
 * @param priority         The priority of this listener, high to low.
 * @param parallel         If a listener should be invoked in parallel with other parallel listeners, or sequentially.
 * @param receiveCancelled If a listener should receive events that have been cancelled by previous listeners.
 * @param listener         The body of the listener that will be invoked.
 */
inline fun <reified T : Any> listener(
    priority: Int = 0,
    parallel: Boolean = false,
    receiveCancelled: Boolean = false,
    noinline listener: (T) -> Unit
) = Listener(listener, T::class, priority, parallel, receiveCancelled)

/**
 * **This function is intended for use in Java only.**
 *
 * Creates a listener that can be held in a variable or returned from a function
 * or getter belonging to an object to be subscribed with [EventBus.subscribe],
 * or directly registered to an [EventBus] with [EventBus.register].
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#creating-a-listener)
 *
 * @param type             The **exact** (no inheritance) type of event to listen for.
 * @param priority         The priority of this listener, high to low.
 * @param parallel         If a listener should be invoked in parallel with other parallel listeners, or sequentially.
 * @param receiveCancelled If a listener should receive events that have been cancelled by previous listeners.
 * @param listener         The body of the listener that will be invoked.
 */
@JvmOverloads
fun <T : Any> listener(
    type: Class<T>,
    priority: Int = 0,
    parallel: Boolean = false,
    receiveCancelled: Boolean = false,
    // This might introduce some overhead, but its worth
    // not manually having to return "Unit.INSTANCE" from every Java listener.
    listener: Consumer<T>
) = Listener(listener::accept, type.kotlin, priority, parallel, receiveCancelled)
