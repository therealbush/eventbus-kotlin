package me.bush.illnamethislater

/**
 * A base class for events that can be cancelled.
 *
 * If [cancellable] is true, your event can be [cancelled].
 *
 * If [cancelled] is true, listeners with lower priority will not receive it unless:
 * * They have [Listener.receiveCancelled] set to `true`.
 * * A future listener with [Listener.receiveCancelled] sets [cancelled] to `false`
 *
 * @author bush
 * @since 1.0.0
 */
// TODO: 3/27/2022 ducks or a way to cancel anything (can't put a custom annotation on forge events)
// store cancellable info at subscribe time, do not calculate it in post
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
