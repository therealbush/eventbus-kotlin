import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.bush.eventbuskotlin.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

/**
 * I don't know how to do these....
 *
 * @author bush
 * @since 1.0.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinTest {
    private lateinit var eventBus: EventBus
    private val logger = LogManager.getLogger("Kotlin Test")

    @BeforeAll
    fun `setup logger and initialize eventbus`() {
        // Log level defaults to only error
        Configurator.setRootLevel(Level.ALL)
        eventBus = EventBus(
            // Defaults
            Config(
                logger = LogManager.getLogger("Eventbus"),
                parallelScope = CoroutineScope(Dispatchers.Default),
                thirdPartyCompatibility = true,
                annotationRequired = false
            )
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test priority and ability to cancel events or receive cancelled events`() {
        eventBus.subscribe(this)
        val event = SimpleEvent()
        eventBus.post(event)
        Assertions.assertEquals(3, event.count)
        eventBus.unsubscribe(this)
    }

    // Last to be called; does not have receiveCancelled, but the last listener un-cancelled the event.
    fun listener4() = listener<SimpleEvent>(priority = Int.MIN_VALUE) {
        Assertions.assertEquals(2, it.count)
        it.count++
    }

    // Will not be called; second-highest priority, no receiveCancelled.
    val listener2
        get() = listener<SimpleEvent>(priority = 0) {
            Assertions.fail("This should not be called")
        }

    // First to be called; highest priority.
    private val listener1 = listener<SimpleEvent>(priority = 10) {
        Assertions.assertEquals(0, it.count)
        it.count++
        // Cancel, so next listener shouldn't receive it.
        it.cancel()
    }

    // Second to be called; has receiveCancelled and can un-cancel the event.
    fun listener3() = listener<SimpleEvent>(priority = Int.MIN_VALUE + 100, receiveCancelled = true) {
        Assertions.assertEquals(1, it.count)
        it.count++
        it.cancelled = false
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test subscribing on a kotlin singleton object`() {
        eventBus.subscribe(ObjectTest)
        val event = SimpleEvent()
        eventBus.post(event)
        Assertions.assertEquals(5, event.count)
        eventBus.unsubscribe(ObjectTest)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test primitive types and listeners which don't belong to a class`() {
        val random = Random.nextInt()
        var changed = 0
        val listener = listener<Int> {
            changed = it
        }
        eventBus.register(listener)
        eventBus.post(random)
        Assertions.assertEquals(random, changed)
        eventBus.unregister(listener)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test that we can detect if an external event is cancelled`() {
        eventBus.subscribe(this)
        val event = ExternalEvent()
        eventBus.post(event)
        eventBus.unsubscribe(this)
    }

    @EventListener
    fun externalListener1() = listener<ExternalEvent>(priority = 1) {
        it.canceled = true
    }

    // Should not be called
    fun externalListener2() = listener<ExternalEvent>(priority = -1) {
        Assertions.fail("This should not be called")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test parallel event posting`() {
        val listeners = mutableListOf<Listener>()
        repeat(10) {
            // Not sure what to test
            listeners += listener<Unit>(parallel = true) {
                logger.info("Thread:" + Thread.currentThread().name)
            }
        }
        listeners.forEach { eventBus.register(it) }
        eventBus.post(Unit)
        listeners.forEach { eventBus.unregister(it) }

        /* I'm not sure what else to test for this, but I'm also not really happy with parallel event posting yet. TODO

           [DefaultDispatcher-worker-5 @coroutine#5] INFO  Kotlin Test - DefaultDispatcher-worker-5 @coroutine#5
           [DefaultDispatcher-worker-2 @coroutine#2] INFO  Kotlin Test - DefaultDispatcher-worker-2 @coroutine#2
           [DefaultDispatcher-worker-1 @coroutine#1] INFO  Kotlin Test - DefaultDispatcher-worker-1 @coroutine#1
           [DefaultDispatcher-worker-4 @coroutine#4] INFO  Kotlin Test - DefaultDispatcher-worker-4 @coroutine#4
           [DefaultDispatcher-worker-7 @coroutine#7] INFO  Kotlin Test - DefaultDispatcher-worker-7 @coroutine#7
           [DefaultDispatcher-worker-6 @coroutine#6] INFO  Kotlin Test - DefaultDispatcher-worker-6 @coroutine#6
           [DefaultDispatcher-worker-8 @coroutine#8] INFO  Kotlin Test - DefaultDispatcher-worker-8 @coroutine#8
           [DefaultDispatcher-worker-9 @coroutine#9] INFO  Kotlin Test - DefaultDispatcher-worker-9 @coroutine#9
           [DefaultDispatcher-worker-3 @coroutine#3] INFO  Kotlin Test - DefaultDispatcher-worker-3 @coroutine#3
         [DefaultDispatcher-worker-10 @coroutine#10] INFO  Kotlin Test - DefaultDispatcher-worker-10 @coroutine#10 */

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test thread safety`() {
        val listeners = mutableListOf<Listener>()
        repeat(10) {
            listeners += listener<Any>(parallel = false) {
                doStuff()
            }
            listeners += listener<Any>(parallel = true) {
                doStuff()
            }
        }
        listeners.forEach { eventBus.register(it) }
        eventBus.debug()
        CoroutineScope(Dispatchers.Default).launch {
            repeat(100) {
                launch {
                    doStuff()
                    eventBus.post(Any())
                }
            }
        }
        Thread.sleep(2000)
        Assertions.assertEquals(2100, counter.get())
        listeners.forEach { eventBus.unregister(it) }
        eventBus.unregister(dummy)
        eventBus.unsubscribe(this)
        // Should be empty
        eventBus.debug()
    }

    private val dummy = listener<Unit> {}

    private var counter = AtomicInteger()

    private fun doStuff() {
        eventBus.unsubscribe(this)
        eventBus.subscribe(this)
        eventBus.unregister(dummy)
        eventBus.register(dummy)
        counter.getAndIncrement()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test that inheritance doesn't affect events or listeners`() {
        val superTest = listener<SimpleEvent> {
            Assertions.fail("This should not be called")
        }
        eventBus.register(superTest)
        // No listeners should be called when a superclass is posted.
        eventBus.post(Any())
        eventBus.unregister(superTest)
        val subTest = listener<Any> {
            Assertions.fail("This should not be called")
        }
        eventBus.register(subTest)
        // No listeners should be called when a subclass is posted.
        eventBus.post(SimpleEvent())
        eventBus.unregister(subTest)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test that require annotation mode works`() {
        val eventBus = EventBus(Config(annotationRequired = true))
        eventBus.subscribe(this)
        eventBus.post(Unit)
        Assertions.assertTrue(called)
        eventBus.unsubscribe(this)
    }

    private var called = false

    @EventListener
    val annotation = listener<Unit> {
        called = true
    }

    val noAnnotation = listener<Unit> {
        Assertions.fail("This should not be called")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test subscribing on a companion object`() {
        eventBus.subscribe(KotlinTest)
        val string = "i love bush's eventbus <3"
        eventBus.post(string)
        Assertions.assertEquals(string, value)
        eventBus.unsubscribe(KotlinTest)
    }

    companion object {
        var value = ""

        @EventListener
        private val listener = listener<String> {
            value = it
        }
    }
}

object ObjectTest {
    private val listener1 = listener<SimpleEvent> {
        it.count++
    }

    @EventHandler
    fun listenerAnnotated(event: SimpleEvent) {
        event.count++
    }

    @EventHandler
    private fun listenerAnnotatedPrivate(event: SimpleEvent) {
        event.count++
    }

    @EventListener
    private val listener2
        get() = listener<SimpleEvent> {
            it.count++
        }

    private fun listener3() = listener<SimpleEvent> {
        it.count++
    }
}

class SimpleEvent : Event() {
    override val cancellable = true

    var count = 0
}

class ExternalEvent {
    var canceled = false
}
