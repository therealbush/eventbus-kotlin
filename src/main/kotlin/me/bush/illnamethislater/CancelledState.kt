package me.bush.illnamethislater

import sun.misc.Unsafe
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubclassOf
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
     * Returns whether [event] is cancelled or not. [event] should only ever be of the type
     * that was passed to [CancelledState.of], or this will cause an error.
     */
    fun isCancelled(event: Any): Boolean

    companion object {
        private val UNSAFE = Unsafe::class.declaredMembers.single { it.name == "theUnsafe" }.handleCall() as Unsafe
        private val CANCELLED_NAMES = arrayOf("canceled", "cancelled")
        private val NOT_CANCELLABLE = CancelledState { false }
        private val OFFSETS = hashMapOf<KClass<*>, Long>()

        /**
         * Creates a [CancelledState] object for events of class [type].
         */
        fun of(type: KClass<*>, config: Config): CancelledState {
            // Default impl for our event class.
            if (type.isSubclassOf(Event::class)) return CancelledState { (it as Event).cancelled }
            // If compat is disabled.
            if (!config.thirdPartyCompatibility) return NOT_CANCELLABLE
            // Find a field named "cancelled" or "canceled" that is a boolean, and has a backing field.
            type.allMembers.filter { it.name in CANCELLED_NAMES && it.returnType == typeOf<Boolean>() }
                .filterIsInstance<KMutableProperty<*>>().filter { it.javaField != null }.toList().let {
                    if (it.isEmpty()) return NOT_CANCELLABLE
                    if (it.size != 1) config.logger.warn("Multiple possible cancel fields found for event type $type")
                    it[0].javaField!!.let { field ->
                        if (Modifier.isStatic(field.modifiers)) OFFSETS[type] = UNSAFE.staticFieldOffset(field)
                        else OFFSETS[type] = UNSAFE.objectFieldOffset(field)
                    }
                    // This is the same speed as direct access, plus one JNI call and hashmap access.
                    // If you are familiar with C, this is essentially the same idea as pointers.
                    return CancelledState { event -> UNSAFE.getBoolean(event, OFFSETS[type]!!) }
                }
        }
    }
}
