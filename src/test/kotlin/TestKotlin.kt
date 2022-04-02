import me.bush.illnamethislater.Event
import me.bush.illnamethislater.EventBus
import me.bush.illnamethislater.listener
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.junit.jupiter.api.AfterAll
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
    private lateinit var eventBus: EventBus
    private val logger = LogManager.getLogger()

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @BeforeAll
    fun setup() {
        Configurator.setRootLevel(Level.ALL)

        // Test that init works
        logger.info("Initializing")
        eventBus = EventBus()

        // Ensure no npe
        logger.info("Logging empty debug info")
        eventBus.debugInfo()

        // Test that basic subscribing reflection works
        logger.info("Subscribing")
        eventBus.subscribe(this)

        logger.info("Testing Events")
    }

    @AfterAll
    fun unsubscribe() {
        logger.info("Unsubscribing")
        eventBus.unsubscribe(this)
        eventBus.debugInfo()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests debug info format
    @Test
    fun debugInfoTest() {
        eventBus.debugInfo()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests autoboxing and internal data structure against primitives.
    @Test
    fun primitiveListenerTest() {
        val random = Random.nextInt()
        eventBus.post(random)
        Assertions.assertEquals(random, primitiveTestValue)
    }

    private var primitiveTestValue = 0

    val primitiveListener = listener<Int> {
        primitiveTestValue = it
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests unsubscribing of listeners which don't belong to a subscriber.
    @Test
    fun freeListenerTest() {
        // Register listener and keep the value
        val listener = eventBus.register(listener<String> {
            freeListenerTestValue = it
        })
        val valueOne = "i love bush's eventbus <3"
        val valueTwo = "sdklasdjsakdsadlksadlksdl"
        // Will change the value
        eventBus.post(valueOne)
        Assertions.assertEquals(valueOne, freeListenerTestValue)
        // Remove the listener
        eventBus.unregister(listener)
        // No effect
        eventBus.post(valueTwo)
        // Value will not change
        Assertions.assertEquals(valueOne, freeListenerTestValue)
    }

    private var freeListenerTestValue: String? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests external event cancel state functionality.
    @Test
    fun externalEventListenerTest() {
        var unchanged = "this will not change"
        // Cancels the event
        eventBus.register(listener<ExternalEvent>(200) {
            it.canceled = true
        })
        // This shouldn't be called
        eventBus.register(listener<ExternalEvent>(100) {
            unchanged = "changed"
        })
        eventBus.post(ExternalEvent())
        Assertions.assertEquals(unchanged, "this will not change")
        // Tests that duplicates are detected, and that both
        // "canceled" and "cancelled" are detected as valid fields
        eventBus.register(listener<ExternalDuplicates> {})
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests parallel invocation functionality.
    @Test
    fun parallelListenerTest() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Tests reflection against singleton object classes.
    @Test
    fun objectSubscriberTest() {
        eventBus.subscribe(ObjectSubscriber)
        eventBus.post(Unit)
        Assertions.assertTrue(ObjectSubscriber.willChange)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // todo test thread safety
    // todo ensure boolean functions return proper value (subscribe, unsubscribe, etc)
}

object ObjectSubscriber {
    var willChange = false

    fun listener() = listener<Unit> {
        willChange = true
    }
}

class MyEvent : Event() {
    override val cancellable = true

    var lastListener = ""
    var someString = "donda"
}

class ExternalEvent {
    var canceled = false
}

// Should give us a warning about duplicates
class ExternalDuplicates {
    var canceled = false
    var cancelled = false
}
