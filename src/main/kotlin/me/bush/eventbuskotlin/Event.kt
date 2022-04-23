package me.bush.eventbuskotlin

/**
 * A base class for events that can be cancelled.
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#creating-an-event)
 *
 * @author bush
 * @since 1.0.0
 */
abstract class Event {

    /**
     * Whether this event is cancelled or not. If it is, only future listeners with
     * [Listener.receiveCancelled] will receive it. However, it can be set back to
     * `false`, and listeners will be able to receive it again.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#posting-an-event)
     */
    var cancelled = false
        set(value) {
            if (cancellable) field = value
        }

    /**
     * Determines if this event can be [cancelled]. This does not have to return a constant value.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#creating-an-event)
     */
    protected abstract val cancellable: Boolean

    /**
     * Sets [cancelled] to true.
     *
     * [Information and examples](https://github.com/therealbush/eventbus-kotlin#posting-an-event)
     */
    fun cancel() {
        cancelled = true
    }
}
