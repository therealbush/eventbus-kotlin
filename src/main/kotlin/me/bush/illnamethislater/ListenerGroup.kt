package me.bush.illnamethislater

import kotlinx.coroutines.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext
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
        with(if (listener.parallel) parallel else sequential) {
            add(listener)
            sortedByDescending { it.priority }
        }
    }

    /**
     * Removes [listener] from this [ListenerGroup].
     */
    fun unregister(listener: Listener) {
        if (listener.parallel) parallel.remove(listener)
        else sequential.remove(listener)
    }

    /**
     * Removes every listener whose subscriber is [subscriber].
     */
    fun unsubscribe(subscriber: Any) {
        parallel.removeIf { it.subscriber == subscriber }
        sequential.removeIf { it.subscriber == subscriber }
    }

    /**
     * Posts an event to every listener. Returns true of the event was cancelled.
     */
    fun post(event: Any): Boolean {
        if (config.parallelFirst) postParallel(event)
        sequential.forEach {
            if (it.receiveCancelled || !cancelledState.isCancelled(event)) {
                it.listener(event)
            }
        }
        if (!config.parallelFirst) postParallel(event)
        return cancelledState.isCancelled(event)
    }

    /**
     * Posts an event to all parallel listeners. Cancel state of the event is checked once before
     * posting the event as opposed to before calling each listener, to avoid inconsistencies.
     */
    private fun postParallel(event: Any) {
        if (parallel.isEmpty()) return
        // We check this once, because listener order is not consistent
        val cancelled = cancelledState.isCancelled(event)
        // Credit to KB for the idea
        runBlocking(config.parallelContext) {
            parallel.forEach {
                if (it.receiveCancelled || !cancelled) launch {
                    it.listener(event)
                }
            }
        }
    }

    /**
     * Logs information about this [ListenerGroup].
     */
    fun debugInfo() = config.logger.info("${type.simpleName}: ${sequential.size}, ${parallel.size}")
}
