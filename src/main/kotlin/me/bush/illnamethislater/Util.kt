package me.bush.illnamethislater

import java.lang.reflect.Modifier
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter

// @author bush
// @since 3/13/2022

/**
 * Using [KClass.members] only returns public members, and
 * using [KClass.declaredMembers] doesn't return inherited
 * members. This returns all members, private and inherited.
 */
internal val <T : Any> KClass<T>.allMembers
    get() = declaredMembers + allSuperclasses.flatMap { it.declaredMembers }

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

@Suppress("UNCHECKED_CAST") // This cannot fail
internal inline fun <reified T : Any> List<KCallable<*>>.filterReturnType() =
    filter { it.returnType == T::class.starProjectedType } as List<KCallable<T>>

/**
 * A simple class returned as a "key" for listeners that are not
 * members of a class, just to make its intentions clearer. This
 * can be used in [EventBus.unsubscribe] to remove the listener.
 */
internal class ListenerKey