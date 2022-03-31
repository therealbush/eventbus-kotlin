package me.bush.illnamethislater

import sun.misc.Unsafe
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.javaField
import kotlin.reflect.typeOf

/**
 * @author bush
 * @since 1.0.0
 */
internal fun interface CancelledState {

    /**
     * Will either return false, cast [event] to [Event] and return [Event.cancelled], or use
     * [Unsafe.getBoolean] to get the value of the "cancelled" field of an external event.
     *
     * @author bush
     * @since 1.0.0
     */
    fun isCancelled(event: Any): Boolean

    companion object {
        private val UNSAFE = Unsafe::class.declaredMembers.single { it.name == "theUnsafe" }.handleCall() as Unsafe

        // This is really just for interoperability with forge events, but ig it would work with anything
        private val CANCELLED_NAMES = arrayOf("canceled", "cancelled")
        private val NOT_CANCELLABLE by lazy { CancelledState { false } }
        private val OFFSETS = hashMapOf<KClass<*>, Long>()

        fun cancelStateOf(type: KClass<*>, bus: EventBus): CancelledState {
            if (type.isSubclassOf(Event::class)) return CancelledState { (it as Event).cancelled }
            if (!bus.externalSupport) return NOT_CANCELLABLE
            type.allMembers.filter { it.name in CANCELLED_NAMES && it.returnType == typeOf<Boolean>() }
                .filterIsInstance<KMutableProperty1<*, *>>().toList().let {
                    if (it.isEmpty()) return NOT_CANCELLABLE
                    if (it.size != 1) bus.logger.warn("Multiple possible cancel fields found for event type $type")
                    OFFSETS[type] = UNSAFE.objectFieldOffset(it[0].javaField)
                    return CancelledState { UNSAFE.getBoolean(it, OFFSETS[type]!!) }
                }
        }
    }
}
