package me.bush.illnamethislater

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * @author bush
 * @since 1.0.0
 */
internal class ListenerGroup(
    private val type: KClass<*>,
    private val config: Config
) {
    val cancelledState = CancelledState.of(type, config)
    val sequential = CopyOnWriteArrayList<Listener>()
    val parallel = CopyOnWriteArrayList<Listener>()

    fun add(listener: Listener) {
        with(if (listener.parallel) parallel else sequential) {
            add(listener)
            sortBy { it.priority }
        }
    }

    fun remove(listener: Listener) = with(if (listener.parallel) parallel else sequential) {
        remove(listener)
    }

    fun removeFrom(subscriber: Any) {
        parallel.removeIf(Listener::subscriber::equals)
        sequential.removeIf(Listener::subscriber::equals)
    }

    fun post(event: Any): Boolean {
        return false
    }

    fun debugInfo() = config.logger.info("${type.simpleName}: ${sequential.size}, ${parallel.size}")
}
