package me.bush.illnamethislater

import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * A class for storing and handling listeners.
 *
 * @author bush
 * @since 1.0.0
 */
internal class ListenerGroup(
    private val type: KClass<*>,
    private val config: Config
) {
    private val cancelledState = CancelledState.of(type, config)
    val sequential = CopyOnWriteArrayList<Listener>()
    val parallel = CopyOnWriteArrayList<Listener>()

    /**
     * Adds [listener] to this [ListenerGroup], and sorts its list.
     */
    fun register(listener: Listener) {
        (if (listener.parallel) parallel else sequential).let {
            if (it.addIfAbsent(listener)) {
                it.sortedByDescending(Listener::priority)
            }
        }
    }

    /**
     * Removes [listener] from this [ListenerGroup].
     */
    fun unregister(listener: Listener): Boolean {
        return if (listener.parallel) parallel.remove(listener)
        else sequential.remove(listener)
    }

    /**
     * Posts an event to every listener. Returns true of the event was cancelled.
     */
    fun post(event: Any): Boolean {
        sequential.forEach {
            if (it.receiveCancelled || !cancelledState.isCancelled(event)) {
                it.listener(event)
            }
        }
        if (parallel.isNotEmpty()) {
            // We check this once, because listener order is not guaranteed.
            val cancelled = cancelledState.isCancelled(event)
            // Credit to KB for the idea
            parallel.forEach {
                if (it.receiveCancelled || !cancelled) {
                    config.parallelScope.launch {
                        it.listener(event)
                    }
                }
            }
        }
        return cancelledState.isCancelled(event)
    }

    override fun toString() = "${type.simpleName}: ${sequential.size}, ${parallel.size}"
}
