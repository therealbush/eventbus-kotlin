package me.bush.eventbuskotlin

import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * [A simple, thread safe, and fast event dispatcher for Kotlin/JVM and Java.](https://github.com/therealbush/eventbus-kotlin)
 *
 * @author bush
 * @since 1.0.0
 */
class EventBus(private val config: Config = Config()) {
    private val listeners = ConcurrentHashMap<KClass<*>, ListenerGroup>()
    private val subscribers = ConcurrentHashMap.newKeySet<Any>()
    private val cache = ConcurrentHashMap<Any, List<Listener>?>()

    /**
     * Searches [subscriber] for members that return [Listener] and registers them, if
     * [Config.annotationRequired] is false, or they are annotated with [EventListener].
     *
     * This will not find top level members, use [register] instead.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     *
     * @return `true` if [subscriber] was successfully subscribed,
     * `false` if it was already subscribed, or could not be.
     */
    fun subscribe(subscriber: Any): Boolean = subscribers.add(subscriber).also {
        if (it) cache.computeIfAbsent(subscriber) {
            getListeners(subscriber, config)
        }?.forEach(::register) ?: return false
    }

    /**
     * Unregisters all listeners belonging to [subscriber].
     *
     * This will not remove top level listeners, use [unregister] instead.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     *
     * @return `true` if [subscriber] was successfully unsubscribed, `false` if it was not subscribed.
     */
    fun unsubscribe(subscriber: Any): Boolean = subscribers.remove(subscriber).also {
        if (it) cache[subscriber]?.forEach(::unregister)
    }

    /**
     * Registers a [Listener] to this [EventBus].
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun register(listener: Listener): Boolean = listeners.computeIfAbsent(listener.type) {
        ListenerGroup(it, config)
    }.register(listener)

    /**
     * Unregisters a [Listener] from this [EventBus]. Returns `true` if [Listener] was registered.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun unregister(listener: Listener): Boolean = listeners[listener.type]?.let {
        val contained = it.unregister(listener)
        if (it.parallel.isEmpty() && it.sequential.isEmpty()) {
            listeners.remove(listener.type)
        }
        contained
    } ?: false

    /**
     * Posts an [event] to every listener that accepts its type.
     *
     * Events are **not** queued: only listeners currently subscribed will be called.
     *
     * If [event] is a subclass of [Event], or has a field-backed mutable boolean property
     * named "cancelled" or "canceled" and [Config.thirdPartyCompatibility] is `true`,
     * only future listeners with [Listener.receiveCancelled] will receive [event]
     * while that property is `true`.
     *
     * Sequential listeners are called in the order of [Listener.priority], and parallel listeners
     * are called after using [launch]. This method will not wait for parallel listeners to complete.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun post(event: Any): Boolean = listeners[event::class]?.post(event) ?: false

    /**
     * Logs the subscriber count, total listener count, and listener count for every event type with at
     * least one subscriber to [Config.logger]. Per-event counts are sorted from greatest to least listeners.
     *
     * **This may cause a [ConcurrentModificationException] if [register] or [subscribe] is called in parallel.**
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     * ```
     * Subscribers: 5
     * Listeners: 8 sequential, 21 parallel
     * BushIsSoCool: 4, 9
     * OtherEvent: 1, 10
     * String: 3, 0
     */
    fun debug() {
        config.logger.info("Subscribers: ${subscribers.size}")
        val sequential = listeners.values.sumOf { it.sequential.size }
        val parallel = listeners.values.sumOf { it.parallel.size }
        config.logger.info("Listeners: $sequential sequential, $parallel parallel")
        listeners.values.sortedByDescending { it.sequential.size + it.parallel.size }.forEach {
            config.logger.info(it.toString())
        }
    }
}

