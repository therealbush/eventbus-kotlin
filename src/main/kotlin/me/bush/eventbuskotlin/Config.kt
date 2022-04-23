package me.bush.eventbuskotlin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.apache.logging.log4j.Logger


/**
 * A class containing configuration options for an [EventBus].
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
 *
 * @author bush
 * @since 1.0.0
 */
data class Config(

    /**
     * The logger this [EventBus] will use to log errors, or [EventBus.debug]
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val logger: Logger = LOGGER,

    /**
     * The [CoroutineScope] to use when posting events to parallel listeners. The default
     * value will work just fine, but you can specify a custom scope if desired.
     *
     * [What is a Coroutine?](https://kotlinlang.org/docs/coroutines-overview.html)
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val parallelScope: CoroutineScope = CoroutineScope(Dispatchers.Default),

    /**
     * Whether this [EventBus] should try to find a "cancelled" field in events being listened for that
     * are not a subclass of [Event]. This is experimental, and should be set to `false` if problems arise.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val thirdPartyCompatibility: Boolean = true,

    /**
     * Whether listeners need to be annotated with [EventListener] to be subscribed to this [EventBus].
     * This has no effect on anything else, and is just to improve code readability.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val annotationRequired: Boolean = false
)
