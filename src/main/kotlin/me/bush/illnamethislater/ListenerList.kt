package me.bush.illnamethislater

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author bush
 * @since 1.0.0
 */
internal class ListenerList {
    val sequential = CopyOnWriteArrayList<Listener>()
    val parallel = CopyOnWriteArrayList<Listener>()

    val isEmpty get() = sequential.isEmpty() && parallel.isEmpty()

    fun add(listener: Listener) {
        if (listener.parallel) parallel += listener
        else sequential += listener
    }

    fun removeFrom(subscriber: Any) {
        sequential.removeIf(subscriber::equals)
        parallel.removeIf(subscriber::equals)
    }

    fun post(event: Any): Boolean {
        // todo thsi
        parallel.run {
            if (isNotEmpty()) runBlocking {
                forEach { // credit kami blue for the idea
                    launch { it.listener(event) }
                }
            }
        }
        sequential.run {
            if (isNotEmpty()) forEach {
                it.listener(event)
            }
        }
        return false
    }
}
