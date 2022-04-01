package me.bush.illnamethislater

import org.apache.logging.log4j.LogManager
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
     * The logger this [EventBus] will use to log errors, or [EventBus.debugInfo]
     */
    val logger: Logger = LogManager.getLogger("Eventbus"),

    /**
     * Whether this [EventBus] should try to find a "cancelled" field in events being listened for that
     * are not a subclass of [Event]. This is experimental, and should be set to `false` if problems arise.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#tododothething)
     */
    val thirdPartyCompatibility: Boolean = true
)
