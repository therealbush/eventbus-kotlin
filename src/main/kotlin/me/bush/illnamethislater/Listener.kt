package me.bush.illnamethislater

import kotlin.reflect.KClass

// document
annotation class EventListener(val priority: Int = 0, val receiveCancelled: Boolean = false)

/**
 * This class is not intended to be used externally, it is just
 * a wrapper for a listener function and some related properties.
 *
 * Use [listener] instead.
 *
 * @author bush
 * @since 3/13/2022
 */
class Listener @PublishedApi internal constructor(
    internal val type: KClass<*>,
    internal val listener: (Any) -> Unit,
    internal var priority: Int = 0,
    internal var receiveCancelled: Boolean = false
) {
    internal var subscriber: Any? = null
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> listener(
    noinline listener: (T) -> Unit
) = Listener(T::class, listener as (Any) -> Unit)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> listener(
    priority: Int,
    noinline listener: (T) -> Unit
) = Listener(T::class, listener as (Any) -> Unit, priority)

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> listener(
    receiveCancelled: Boolean,
    noinline listener: (T) -> Unit
) = Listener(T::class, listener as (Any) -> Unit, receiveCancelled = receiveCancelled)

// The above aren't needed as the next is functionally identical, but they
// let us avoid (imo ugly) named parameters or a lambda inside parentheses.

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> listener(
    priority: Int = 0,
    receiveCancelled: Boolean = false,
    noinline listener: (T) -> Unit
) = Listener(T::class, listener as (Any) -> Unit, priority, receiveCancelled)
