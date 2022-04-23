package me.bush.eventbuskotlin

import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * A class for storing and handling listeners of the same [type].
 *
 * @author bush
 * @since 1.0.0
 */
internal class ListenerGroup(
    private val type: KClass<*>,
    private val config: Config
) {
    private val cancelledState = CancelledState(type, config)
    val sequential = CopyOnWriteArrayList<Listener>()
    val parallel = CopyOnWriteArrayList<Listener>()

    /**
     * Registers a listener.
     *
     * @return `false` if [listener] was already registered, `true` otherwise.
     */
    fun register(listener: Listener): Boolean = listWith(listener) {
        var position = it.size
        // Sorted insertion and duplicates check
        it.forEachIndexed { index, other ->
            if (listener == other) return false
            if (listener.priority > other.priority && position == it.size) {
                position = index
            }
        }
        it.add(position, listener)
        true
    }

    /**
     * Unregisters a listener.
     *
     * @return `false` if [listener] was not already registered, `true` otherwise.
     */
    fun unregister(listener: Listener): Boolean = listWith(listener) {
        it.remove(listener)
    }

    /**
     * Posts an event to every listener in this group. [event] must be the same type as [type].
     *
     * @return `true` if [event] was cancelled by sequential listeners, `false` otherwise.
     */
    fun post(event: Any): Boolean {
        sequential.forEach {
            if (it.receiveCancelled || !cancelledState.isCancelled(event)) {
                it.listener(event)
            }
        }
        // We check this once, because parallel listener order is not guaranteed.
        val cancelled = cancelledState.isCancelled(event)
        if (parallel.isNotEmpty()) {
            parallel.forEach {
                if (it.receiveCancelled || !cancelled) {
                    config.parallelScope.launch {
                        it.listener(event)
                    }
                }
            }
        }
        return cancelled
    }

    /**
     * Convenience method to perform an action on the list [listener] belongs to within a synchronized block.
     *
     * We are synchronizing a COWArrayList because multiple concurrent mutations can cause IndexOutOfBoundsException.
     * If we were to use a normal ArrayList we would have to synchronize [post], however posting performance
     * is much more valuable than [register]/[unregister] performance.
     */
    private inline fun <R> listWith(listener: Listener, block: (MutableList<Listener>) -> R): R {
        return (if (listener.parallel) parallel else sequential).let {
            synchronized(it) {
                block(it)
            }
        }
    }

    override fun toString() = "${type.simpleName}: ${sequential.size}, ${parallel.size}"
}
