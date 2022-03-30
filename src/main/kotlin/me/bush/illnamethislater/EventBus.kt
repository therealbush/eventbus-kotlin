package me.bush.illnamethislater

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.reflect.KClass

/**
 * [A simple event dispatcher](http://github.com/therealbush/eventbus-kotlin)
 *
 * @author bush
 * @since 1.0.0
 */
class EventBus(private val logger: Logger = LogManager.getLogger()) {
    private val listeners = hashMapOf<KClass<*>, ListenerGroup>()
    private val subscribers = mutableSetOf<Any>()

    /**
     * blah blah annotation properties override listener properties
     */
    infix fun subscribe(subscriber: Any): Boolean {
        return if (subscriber in subscribers) false
        else runCatching {
            subscriber::class.listeners.forEach {
                register(it.handleCall(subscriber), subscriber)
            }
            true
        }.getOrElse {
            logger.error("Unable to register listeners for subscriber $subscriber", it)
            false
        }
    }

    /**
     * Registers a listener (which may not belong to any subscriber) to this [EventBus]. If no object
     * is given, a key will be returned which can be used in [unsubscribe] to remove the listener.
     *
     * The
     */
    fun register(listener: Listener, subscriber: Any = Any()): Any {
        listener.subscriber = subscriber
        listeners.computeIfAbsent(listener.type, ::ListenerGroup).addListener(listener)
        subscribers += subscriber
        return subscriber
    }

    /**
     *
     */
    // doc
    infix fun unsubscribe(subscriber: Any) = subscribers.remove(subscriber).apply {
        if (this) listeners.entries.removeIf {
            it.value.removeFrom(subscriber)
            it.value.isEmpty
        }
    }

    /**
     * Posts an event.
     */
    // doc
    infix fun post(event: Any) = listeners[event::class]?.let { group ->
        group.sequential.forEach {
            if (!group.cancelState.isCancelled(event) || it.receiveCancelled) {
                it.listener(event)
            }
        }
    }

    /**
     * Logs the subscriber count, total listener count, and listener count
     * for every event type with at least one subscriber to [logger].
     * Per-event counts are sorted from greatest to least listeners.
     * ```
     * Subscribers: 5
     * Listeners: 8 sequential, 4 parallel
     * SomeInnerClass: 4, 2
     * OtherEvent: 3, 1
     * String: 1, 1
     */
    // do lol

    fun debugInfo() {
        logger.info(StringBuilder().apply {
            append("\nSubscribers: ${subscribers.size}")
            append("\nListeners: ${listeners.values.sumOf { it.sequential.size }} sequential, ${listeners.values.sumOf { it.parallel.size }} parallel")
            listeners.entries.sortedByDescending { it.value.sequential.size + it.value.parallel.size }.forEach {
                append("${it.key.simpleName}: ${it.value.sequential.size}, ${it.value.parallel.size}")
            }
        }.toString())
    }
}

