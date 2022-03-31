import me.bush.illnamethislater.Event
import me.bush.illnamethislater.EventBus
import me.bush.illnamethislater.listener
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import kotlin.random.Random

/**
 * I don't know how to do these....
 *
 * @author bush
 * @since 1.0.0
 */
@TestInstance(Lifecycle.PER_CLASS)
class Test {
    lateinit var eventBus: EventBus
    private val logger = LogManager.getLogger()

    @BeforeAll
    fun setup() {
        Configurator.setRootLevel(Level.ALL)

        logger.info("Initializing")
        eventBus = EventBus()

        logger.info("Logging empty debug info")
        eventBus.debugInfo()

        logger.info("Subscribing")
        eventBus.subscribe(this)

        logger.info("Testing Events")
    }

    // Tests autoboxing and internal data structure against primitives.
    @Test
    fun primitiveListenerTest() {
        val random = Random.nextInt()
        eventBus.post(random)
        Assertions.assertEquals(random, primitiveTestValue)
    }

    var primitiveTestValue = 0

    val primitiveListener = listener<Int> {
        primitiveTestValue = it
    }

    // Tests unsubscribing of "free" listeners which don't belong to a subscriber. todo allow keys to be resubscribed and test top level listeners
    @Test
    fun freeListenerTest() {
        // Register "free" listener, and keep the returned key
        val key = eventBus.register(listener<String> {
            freeListenerTestValue = it
        })
        val valueOne = "i love bush's eventbus <3"
        val valueTwo = "sdklasdjsakdsadlksadlksdl"
        // Will change the value
        eventBus.post(valueOne)
        Assertions.assertEquals(valueOne, freeListenerTestValue)
        // Remove the listener
        eventBus.unsubscribe(key)
        // No effect
        eventBus.post(valueTwo)
        // Value will not change
        Assertions.assertEquals(valueOne, freeListenerTestValue)
    }

    var freeListenerTestValue: String? = null

    // Tests priority and receiveCancelled functionality.
    @Test
    fun myEventListenerTest() {
        val event = MyEvent()
        eventBus.post(event)
        Assertions.assertEquals(event.lastListener, "myEventListener3")
    }

    // First to be called; highest priority.
    val myEventListener0 = listener<MyEvent>(priority = 10) {
        Assertions.assertEquals(it.lastListener, "")
        it.lastListener = "myEventListener0"
        it.cancel()
    }

    // Will not be called; second-highest priority, no receiveCancelled.
    val myEventListener1
        get() = listener<MyEvent>(priority = 0) {
            Assertions.assertTrue(false)
        }

    // Second to be called; has receiveCancelled and can un-cancel the event.
    fun myEventListener2() = listener<MyEvent>(priority = Int.MIN_VALUE + 100, receiveCancelled = true) {
        Assertions.assertEquals(it.lastListener, "myEventListener0")
        it.lastListener = "myEventListener2"
        it.cancelled = false
    }

    // Last to be called; does not have receiveCancelled, but the last listener un-cancelled the event.
    fun myEventListener3() = listener<MyEvent>(priority = Int.MIN_VALUE) {
        Assertions.assertEquals(it.lastListener, "myEventListener2")
        it.lastListener = "myEventListener3"
    }

    // Tests external event cancel state functionality.
    @Test
    fun externalEventListenerTest() {

    }

    // Tests parallel invocation functionality. todo how will this work with cancellability
    @Test
    fun parallelListenerTest() {

    }

    // Tests reflection against singleton object classes.
    @Test
    fun objectSubscriberTest() {

    }

    // todo what else?
}

// todo test changing cancellability
class MyEvent : Event() {
    override val cancellable = true

    var lastListener = ""
}

class ExternalEvent0 {
    var canceled = false
}

class ExternalEvent1 {
    var cancelled = false
}

// Should give us a warning about duplicates
class ExternalEvent2 {
    var canceled = false
    var cancelled = false
}
