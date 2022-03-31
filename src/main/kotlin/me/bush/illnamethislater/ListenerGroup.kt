package me.bush.illnamethislater

import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author bush
 * @since 1.0.0
 */
internal class ListenerGroup(val cancelState: CancelledState) {
    val parallel = CopyOnWriteArrayList<Listener>()
    val sequential = CopyOnWriteArrayList<Listener>()

    val isEmpty get() = parallel.isEmpty() && sequential.isEmpty()

    fun addListener(listener: Listener) {
        if (listener.parallel) parallel += listener
        else sequential += listener
    }

    fun removeFrom(subscriber: Any) {
        parallel.removeIf(Listener::subscriber::equals)
        sequential.removeIf(Listener::subscriber::equals)
    }
}
