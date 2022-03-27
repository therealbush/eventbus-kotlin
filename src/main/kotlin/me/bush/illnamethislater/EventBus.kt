package me.bush.illnamethislater

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * @author bush
 * @since 3/13/2022
 */
class EventBus(private val logger: Logger = LogManager.getLogger(), private val strict: Boolean = true) {
    private val listeners = hashMapOf<KClass<*>, MutableList<Listener>>()
    private val subscribers = hashSetOf<Any>()

    // TODO: 3/26/2022 coroutine shit idk

    /**
     * blah blah annotation properties override listener properties
     */
    fun subscribe(subscriber: Any) {
        if (subscribers.add(subscriber)) {
            subscriber::class.allMembers.filterReturnType<Listener>().forEach { property ->
                register(property.handleCall(subscriber).apply {
                    // If the annotation is present, update listener settings from it
                    // If it is not, and we are in strict mode, continue to the next property
                    property.findAnnotation<EventListener>()?.let {
                        priority = it.priority
                        receiveCancelled = it.receiveCancelled
                    } ?: if (strict) return@forEach
                }, subscriber)
            }
        }
    }

    /**
     *
     */
    fun register(listener: Listener, subscriber: Any = ListenerKey()): Any {
        putListener(listener.also { it.subscriber = subscriber })
        return subscriber
    }

    /**
     * Puts a listener into its respective list and sorts the list.
     */
    private fun putListener(listener: Listener) {
        listeners.getOrPut(listener.type) { mutableListOf() }.run {
            add(listener)
            sortBy(Listener::priority)
        }
    }

    /**
     *
     */
    fun unsubscribe(subscriber: Any) {
        if (subscribers.remove(subscriber)) {
            listeners.values.forEach { it.removeIf(subscriber::equals) }
        }
    }

    /**
     * ur mom
     */
    fun post(event: Any) = listeners[event::class].let { list ->
        if (list == null || list.isEmpty()) false
        else if (event is Event) {
            list.forEach {
                if (!event.cancelled || it.receiveCancelled) {
                    it.listener(event)
                }
            }
            event.cancelled
        } else {
            list.forEach { it.listener(event) }
            false
        }
    }
}
