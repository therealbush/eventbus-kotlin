package me.bush.illnamethislater

import kotlinx.coroutines.Dispatchers
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.coroutines.CoroutineContext


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
     * The logger this [EventBus] will use to log errors, or log [EventBus.debugInfo]
     */
    val logger: Logger = LogManager.getLogger("Eventbus"),

    /**
     * The [CoroutineContext] to use when posting events to parallel listeners. The default
     * value will work just fine, but you can specify a custom context if desired.
     *
     * [What is a Coroutine Context?](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html)
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val parallelContext: CoroutineContext = Dispatchers.Default,

    /**
     * Whether this [EventBus] should try to find a "cancelled" field in events being listened for that
     * are not a subclass of [Event]. This is experimental, and should be set to `false` if problems arise.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val thirdPartyCompatibility: Boolean = true,

    /**
     * Whether parallel listeners should be called before or after sequential listeners. Parallel listeners
     * will always finish before sequential listeners are called, or before [EventBus.post] returns.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val parallelFirst: Boolean = true
)
