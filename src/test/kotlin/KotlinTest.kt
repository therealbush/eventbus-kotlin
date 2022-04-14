import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.bush.illnamethislater.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Test
import org.opentest4j.AssertionFailedError
import sun.misc.Unsafe
import kotlin.jvm.internal.PropertyReference0Impl
import kotlin.random.Random
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty
import kotlin.reflect.full.*
import kotlin.reflect.javaType
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

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
    fun `setup logger and initialize eventbus` () {
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
    fun `test listener priority and ability to cancel events or receive cancelled events` () {
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
    fun `test subscribing on a kotlin singleton object` () {
        eventBus.subscribe(ObjectTest)
        val event = SimpleEvent()
        eventBus.post(event)
        Assertions.assertEquals(3, event.count)
        eventBus.unsubscribe(ObjectTest)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test primitive types and listeners which don't belong to a class` () {
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
    fun `test that we can detect if an external event is cancelled` () {
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
    fun `test parallel event posting` () {
        runBlocking {
            sus()
        }
    }

    suspend fun sus() {
        println()
    }

    fun sussy() {
        println()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `call every method on multiple threads concurrently to ensure no CME is thrown` () {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `test that inheritance doesn't affect events or listeners` () {
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
    fun `test that require annotation mode works` () {
        val eventBus = EventBus(Config(annotationRequired = true))
        eventBus.subscribe(this)
        eventBus.post(Unit)
        Assertions.assertTrue(called)
    }

    var called = false

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
    fun `test subscribing on a companion object` () {
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

    @EventListener
    private val listener2 get() = listener<SimpleEvent> {
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
