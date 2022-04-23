package me.bush.eventbuskotlin

import sun.misc.Unsafe
import java.lang.reflect.Modifier
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.javaField
import kotlin.reflect.typeOf

/**
 * A simple SAM interface for determining if an event (or any class) is cancellable.
 *
 * @author bush
 * @since 1.0.0
 */
internal fun interface CancelledState {

    /**
     * [event] should only ever be of the type that was passed
     * to [CancelledState.invoke], **or this will throw.**
     *
     * @return `true` if [event] is cancelled, `false` otherwise.
     */
    fun isCancelled(event: Any): Boolean

    companion object {
        private val UNSAFE = runCatching {
            Unsafe::class.declaredMembers.single { it.name == "theUnsafe" }.handleCall() as Unsafe
        }.onFailure {
            LOGGER.error("Could not obtain Unsafe instance. Will not be able to determine external cancel state.")
        }.getOrNull() // soy jvm
        private val NAMES = arrayOf("canceled", "cancelled")
        private val NOT_CANCELLABLE = CancelledState { false }
        private val CACHE = ConcurrentHashMap<KClass<*>, CancelledState>()

        /**
         * Creates a [CancelledState] object for events of class [type].
         */
        operator fun invoke(type: KClass<*>, config: Config): CancelledState = CACHE.getOrPut(type) {
            // Default implementation for our event class.
            if (type.isSubclassOf(Event::class)) CancelledState { (it as Event).cancelled }
            // If compatibility is disabled.
            else if (!config.thirdPartyCompatibility) NOT_CANCELLABLE
            // Find a field named "cancelled" or "canceled" that is a boolean, and has a backing field.
            else type.allMembers.filter { it.name in NAMES && it.returnType.withNullability(false) == typeOf<Boolean>() }
                .filterIsInstance<KMutableProperty<*>>().filter { it.javaField != null }.toList().let {
                    if (it.isEmpty() || UNSAFE == null) NOT_CANCELLABLE else {
                        if (it.size != 1) config.logger.warn("Multiple possible cancel fields found for event $type")
                        val offset = it[0].javaField!!.let { field ->
                            if (Modifier.isStatic(field.modifiers))
                                UNSAFE.staticFieldOffset(field)
                            else UNSAFE.objectFieldOffset(field)
                        }
                        // This is the same speed as direct access, plus one JNI call.
                        CancelledState { event -> UNSAFE.getBoolean(event, offset) }
                    }
                }
        }
    }
}
