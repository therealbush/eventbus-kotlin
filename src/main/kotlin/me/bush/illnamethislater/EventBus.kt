package me.bush.illnamethislater

import kotlin.reflect.KClass

// TODO: 3/30/2022 Refactor some stuff

/**
 * [A simple event dispatcher](http://github.com/therealbush/eventbus-kotlin)
 *
 * @author bush
 * @since 1.0.0
 */
class EventBus(
    private val config: Config = Config()
) {
    private val listeners = hashMapOf<KClass<*>, ListenerGroup>()
    private val subscribers = mutableSetOf<Any>()

    /**
     * doc
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun subscribe(subscriber: Any): Boolean {
        return if (subscriber in subscribers) false
        else runCatching {
            subscriber::class.listeners.forEach { member ->
                register(member.handleCall(subscriber).also {
                    it.subscriber = subscriber
                })
            }
            subscribers += subscriber
            true
        }.getOrElse {
            config.logger.error("Unable to register listeners for subscriber $subscriber", it)
            false
        }
    }

    /**
     * Registers a listener (which may not belong to any subscriber) to this [EventBus]. If no object
     * is given, a key will be returned which can be used in [unsubscribe] to remove the listener.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun register(listener: Listener): Listener {
        listeners.computeIfAbsent(listener.type) {
            ListenerGroup(it, config)
        }.add(listener)
        return listener
    }

    /**
     * doc
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun unsubscribe(subscriber: Any): Boolean {
        return subscribers.remove(subscriber).also { contains ->
            if (contains) listeners.entries.removeIf {
                it.value.removeFrom(subscriber)
                it.value.sequential.isEmpty() && it.value.parallel.isEmpty()
            }
        }
    }

    /**
     * doc
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun unregister(listener: Listener) = listeners[listener.type]?.remove(listener) ?: false

    /**
     * Posts an event. doc
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    fun post(event: Any) = listeners[event::class]?.post(event) ?: false

    /**
     * Logs the subscriber count, total listener count, and listener count for every event type with at
     * least one subscriber to [Config.logger]. Per-event counts are sorted from greatest to least listeners.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     * ```
     * Subscribers: 5
     * Listeners: 8 sequential, 4 parallel
     * BushIsSoCool: 4, 2
     * OtherEvent: 3, 1
     * String: 1, 1
     */
    fun debugInfo() {
        config.logger.info("Subscribers: ${subscribers.size}")
        val sequential = listeners.values.sumOf { it.sequential.size }
        val parallel = listeners.values.sumOf { it.parallel.size }
        config.logger.info("Listeners: $sequential sequential, $parallel parallel")
        listeners.values.sortedByDescending { it.sequential.size + it.parallel.size }.forEach { it.debugInfo() }
    }
}

