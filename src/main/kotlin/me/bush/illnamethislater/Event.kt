package me.bush.illnamethislater

/**
 * A base class for events that can be cancelled.
 *
 * @author bush
 * @since 3/13/2022
 */
abstract class Event {
    var cancelled = false
        set(value) {
            if (cancellable) field = value
        }

    abstract val cancellable: Boolean

    fun cancel() { cancelled = false }
}