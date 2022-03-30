package me.bush.illnamethislater

import sun.misc.Unsafe
import java.lang.reflect.Modifier
import kotlin.reflect.*
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

// author bush
// since 1.0.0

/**
 * Using [KClass.members] only returns public members, and
 * using [KClass.declaredMembers] doesn't return inherited
 * members. This returns all members, private and inherited.
 */
internal val <T : Any> KClass<T>.allMembers
    get() = (declaredMembers + allSuperclasses.flatMap { it.declaredMembers }).asSequence()

/**
 * Checks if a [KCallable] is static on the jvm, and handles invocation accordingly.
 *
 * I am not aware of a better alternative that works with `object` classes.
 */
internal fun <R> KCallable<R>.handleCall(receiver: Any) = if (static) call() else call(receiver)

/**
 * Checks if the calling [KCallable] is a static java field.
 *
 * Because kotlin likes to be funny, properties belonging to
 * `object` classes are static, but their getters are not.
 *
 * If there is a getter (the property is not private),
 * we will be accessing it through that, so we can stop checking.
 *
 * Otherwise, we check if the field is static with java reflection.
 */
internal val KCallable<*>.static
    get() = if (this !is KProperty<*> || javaGetter != null) false
    else javaField?.let { Modifier.isStatic(it.modifiers) } ?: false

/**
 * Finds all listeners in a class. (properties and methods)
 */
@Suppress("UNCHECKED_CAST") // This cannot fail
internal inline val KClass<*>.listeners
    get() = allMembers.filter { it.returnType == typeOf<Listener>() } as Sequence<KCallable<Listener>>

internal fun interface CancelledState {
    fun isCancelled(event: Any): Boolean
}

private val unsafe = Unsafe::class.java.getDeclaredField("theUnsafe").let {
    it.isAccessible = true
    it.get(null) as Unsafe
}

private val offsetMap = hashMapOf<KClass<*>, Long>()

private val possibleNames = arrayOf("canceled", "cancelled")

private val NOT_CANCELLABLE = CancelledState { false }

internal fun cancelStateOf(type: KClass<*>) = when {
    type.isSubclassOf(Event::class) -> CancelledState { (it as Event).cancelled }
    else -> findCancelField(type)?.let {
        offsetMap[type] = unsafe.objectFieldOffset(it.javaField)
        CancelledState { event -> unsafe.getBoolean(event, offsetMap[type]!!) }
    } ?: NOT_CANCELLABLE
}

private fun findCancelField(type: KClass<*>) = type.allMembers
    .filter { it.name in possibleNames && it.returnType == typeOf<Boolean>() }
    .filterIsInstance<KMutableProperty1<*, *>>().toList().let {
        if (it.isEmpty()) null else {
            if (it.size != 1) TODO() // warn
            it[0]
        }
    }
