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

// author bush
// since 1.0.0

/*
 * Using [KClass.members] only returns public members, and
 * using [KClass.declaredMembers] doesn't return inherited
 * members. This returns all members, private and inherited.
 */
internal val <T : Any> KClass<T>.allMembers
    get() = declaredMembers + allSuperclasses.flatMap { it.declaredMembers }

/*
 * Checks if a [KCallable] is static on the jvm, and handles invocation accordingly.
 *
 * I am not aware of a better alternative that works with `object` classes.
 */
internal fun <R> KCallable<R>.handleCall(receiver: Any) = if (static) call() else call(receiver)

/*
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

/*
 * Finds all listeners in a class. (properties and methods)
 */
@Suppress("UNCHECKED_CAST") // This cannot fail
internal inline val KClass<*>.listeners
    get() = allMembers.asSequence().filter {
        it.returnType == Listener::class.starProjectedType
    } as Sequence<KCallable<Listener>>
