package me.bush.illnamethislater

import kotlin.reflect.KClass

/**
 * [A simple event dispatcher.](https://github.com/therealbush/eventbus-kotlin#tododothething)
 *
 * @author bush
 * @since 1.0.0
 */
class EventBus(private val config: Config = Config()) {
    private val listeners = hashMapOf<KClass<*>, ListenerGroup>()
    private val subscribers = hashMapOf<Any, List<Listener>>()

    /**
     * Searches [subscriber] for members that return [Listener] and registers them.
     *
     * This will not find top level listeners, use [register] instead.
     *
     * Returns `false` if [subscriber] was already subscribed, `true` otherwise.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun subscribe(subscriber: Any): Boolean {
        return if (subscriber in subscribers) false
        else runCatching {
            // Register every listener into a group, but also
            // keep a separate list just for this subscriber.
            subscribers[subscriber] = subscriber::class.listeners.map { member ->
                register(member.handleCall(subscriber).also { it.subscriber = subscriber })
            }.toList()
            true
        }.getOrElse {
            config.logger.error("Unable to register listeners for subscriber $subscriber", it)
            false
        }
    }

    /**
     * Unregisters all listeners belonging to [subscriber].
     *
     * This will not remove top level listeners, use [unregister] instead.
     *
     * Returns `true` if [subscriber] was subscribed, `false` otherwise.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun unsubscribe(subscriber: Any): Boolean {
        val contained = subscriber in subscribers
        // Unregister every listener for this subscriber,
        // and return null so the map entry is removed.
        subscribers.computeIfPresent(subscriber) { _, listeners ->
            listeners.forEach { unregister(it) }
            null
        }
        return contained
    }

    /**
     * Registers a [Listener] to this [EventBus].
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun register(listener: Listener): Listener {
        listeners.computeIfAbsent(listener.type) {
            ListenerGroup(it, config)
        }.register(listener)
        return listener
    }

    /**
     * Unregisters a [Listener] from this [EventBus].
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun unregister(listener: Listener): Boolean {
        return listeners[listener.type]?.let {
            val contained = it.unregister(listener)
            if (it.parallel.isEmpty() && it.sequential.isEmpty()) {
                listeners.remove(listener.type)
            }
            contained
        } ?: false
    }

    /**
     * Posts an [event] to every listener that accepts its type.
     *
     * Events are **not** queued: only listeners subscribed currently will be called.
     *
     * If [event] is a subclass of [Event], or has a field-backed mutable boolean property
     * named "cancelled" or "canceled" and [Config.thirdPartyCompatibility] is `true`,
     * it can be cancelled by a listener, and only future listeners with [Listener.receiveCancelled]
     * will receive it.
     *
     * Sequential listeners are called in the order of [Listener.priority], and parallel
     * listeners are called before or after, depending on the value of [Config.parallelFirst].
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun post(event: Any) = listeners[event::class]?.post(event) ?: false

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
    fun debugInfo() {
        config.logger.info("Subscribers: ${subscribers.size}")
        val sequential = listeners.values.sumOf { it.sequential.size }
        val parallel = listeners.values.sumOf { it.parallel.size }
        config.logger.info("Listeners: $sequential sequential, $parallel parallel")
        listeners.values.sortedByDescending { it.sequential.size + it.parallel.size }.forEach { it.debugInfo() }
    }
}

