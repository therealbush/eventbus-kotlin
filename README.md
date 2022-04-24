# EVENTBUS

<img src="https://tokei.rs/b1/github/therealbush/eventbus-kotlin" alt="lines of code"/> <img src="https://img.shields.io/github/languages/code-size/therealbush/eventbus-kotlin" alt="code size"/> [![](https://jitpack.io/v/therealbush/eventbus-kotlin.svg)](https://jitpack.io/#therealbush/eventbus-kotlin) [![](https://jitpack.io/v/therealbush/eventbus-kotlin/month.svg)](https://jitpack.io/#therealbush/eventbus-kotlin)<br> 

*A simple, thread safe, and fast event dispatcher for Kotlin/JVM and Java.*

## Features

#### Simple to Use

Simple setup, and easy to learn, logical API

#### Thread Safe

Non locking event posting, and minimally locking registering/subscribing make this event dispatcher fit for use in
highly concurrent applications.

#### Fast

Because there is no reflection during event posting, and subscribers are cached after the first use of reflection, this
event dispatcher is much faster than other reflection based alternatives.

#### Flexible

This event dispatcher supports third party events, such as those used in MinecraftForge, and uses the Unsafe API to get
the value of a "cancelled" property at the same speed as direct access.

#### Parallel Event Posting

Listeners can be parallel, and they will be called on a Coroutine in the background. This is useful for heavy operations
that may slow down the posting thread.

## Usage

### Adding to your project:

If you have not already, add Jitpack as a repository:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```

Add the release of your choice in the dependencies block:

```groovy
dependencies {
    implementation 'com.github.therealbush:eventbus-kotlin:1.0.0'
}
```

### Creating an EventBus:

`EventBus` can take a `Config` as an argument, however it is not required.

`Config` has four parameters. The following are the default arguments:

```kotlin
Config(
    logger = LogManager.getLogger("Eventbus"),
    parallelScope = CoroutineScope(Dispatchers.Default),
    thirdPartyCompatibility = true,
    annotationRequired = false
)
```

#### logger

The `Logger` this `EventBus` will use to log errors, or `EventBus#debug`

#### parallelScope

The `CoroutineScope` to use when posting events to parallel listeners. The default value will work just fine, but you
can specify a custom scope if desired.

[What is a Coroutine?](https://kotlinlang.org/docs/coroutines-overview.html)

#### thirdpartyCompatibility

Whether this `EventBus` should try to find a "cancelled" field in events being listened for that are not a subclass
of `Event`. This is experimental, and should be set to `false` if problems arise.

#### annotationRequired

Whether listeners need to be annotated with `@EventListener` to be subscribed to this
`EventBus`. This has no effect on anything else, and is just to improve code readability.

### Creating an Event:

Any class can be posted to an `EventBus`, but if you wish to use the base event class, extend `Event`, and implement the
property `cancellable`.

```kotlin
class SimpleEvent : Event() {
    override val cancellable = true
}
```

```java
public class SimpleEvent extends Event {
    @Override
    protected boolean getCancellable() {
        return false;
    }
}
```

### Creating a Listener:

Listeners are created by using the `listener` function:

```kotlin
listener<EventType>(priority = 0, parallel = false, receiveCancelled = false) {
    ...listener body ...
}
```

```java
listener(EventType.class,0,false,false,event->{
    ...listener body...
});
```

Listeners can be registered either directly with `EventBus#register`, or subscribed by returning them from a function or
property and subscribing the object they belong to with `EventBus#subscribe`.

The following are all valid. Listeners should be public, but they don't need to be.

```kotlin
val listener0 = listener<EventType> {
    ...listener body ...
}

val listener1
    get() = listener<EventType> {
        ...listener body ...
    }

fun listener2() = listener<EventType> {
    ...listener body ...
}
```

```java
public Listener listener0=listener(EventType.class,event->{
    ...listener body...
});

public Listener listener1(){
    return listener(EventType.class,event->{
        ...listener body...
    });
}
```

#### priority

The priority of this `Listener`. Listeners with a higher priority will receive events before listeners with a lower
priority.

#### parallel

Whether this `Listener` should be called on the thread that called `EventBus#post`, or on the
`CoroutineScope` in `Config#parallelScope`. `EventBus#post` will not wait for parallel listeners to complete.

*Currently, there is no way to get a reference to the `Job` created by `launch` when posting to parallel listeners, and
listeners are not `suspend` lambdas. This may change in the future.*

#### receiveCancelled

Whether this `Listener` should receive events that have been cancelled. This will work on third party events
if `Config#thirdPartyCompatibility` is enabled.

### Subscribing an Object:

Calling `EventBus#subscribe` and `EventBus#unsubscribe` with an object will add and remove listeners belonging to that
object from the EventBus. Only listeners in subscribed objects will receive events.

Companion objects and singleton object classes can be subscribed, but subscribing a KClass will not work.

### Posting an Event:

Calling `EventBus#post` will post an event to every listener with an **exactly** matching event type. For example, if
event B extends event A, and event A is posted, B listeners will not receive it.

Events are **not** queued: only listeners currently subscribed will be called.

`EventBus#post` will return true if the posted event is cancelled after posting it to sequential listeners. Event cancel
state is checked once before posting to parallel listeners, because order is not guaranteed.

### Still Confused?

Read [this](https://github.com/therealbush/eventbus-kotlin/tree/master/src/test) for usage examples.
