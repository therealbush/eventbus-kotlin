package me.bush.illnamethislater

/**
 * A base class for events that can be cancelled. More information can be found
 * [here](https://github.com/therealbush/eventbus-kotlin)
 *
 * If [cancellable] is `true`, your event can be [cancelled], where future listeners will not receive it unless:
 * * They have [Listener.receiveCancelled] set to `true`.
 * * A previous listener with [Listener.receiveCancelled] sets [cancelled] back to `false`
 *
 * @author bush
 * @since 1.0.0
 */
abstract class Event {
    var cancelled = false
        set(value) {
            if (cancellable) field = value
        }

    abstract val cancellable: Boolean

    fun cancel() {
        cancelled = false
    }
}
