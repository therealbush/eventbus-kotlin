package me.bush.illnamethislater

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

/**
 * @author bush
 * @since 1.0.0
 */
internal class ListenerGroup(type: KClass<*>) {
    val parallel = CopyOnWriteArrayList<Listener>()
    val sequential = CopyOnWriteArrayList<Listener>()
    val cancelState = cancelStateOf(type)

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
