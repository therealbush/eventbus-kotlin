package me.bush.illnamethislater

import java.lang.reflect.Modifier
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.typeOf

/**
 * Using [KClass.members] only returns public members, and using [KClass.declaredMembers]
 * doesn't return inherited members. This returns all members, private and inherited.
 *
 * @author bush
 * @since 1.0.0
 */
internal val <T : Any> KClass<T>.allMembers
    get() = (declaredMembers + allSuperclasses.flatMap { it.declaredMembers }).asSequence()

/**
 * Checks if a [KCallable] is static on the jvm, and handles invocation accordingly.
 * I am not aware of a better alternative that works with `object` classes.
 *
 * @author bush
 * @since 1.0.0
 */
internal fun <R> KCallable<R>.handleCall(receiver: Any? = null): R {
    isAccessible = true
    return if (static) call() else call(receiver)
}

/**
 * Checks if the calling [KCallable] is a static java field. Because Kotlin likes to be funny, properties
 * belonging to `object` classes are static, but their getters are not. If there is a getter (the property
 * is not private), we will be accessing that, otherwise we check if the field is static with Java reflection.
 * This also lets us support static listeners in Java code.
 *
 * @author bush
 * @since 1.0.0
 */
internal val KCallable<*>.static
    get() = if (this !is KProperty<*> || javaGetter != null) false
    else javaField?.let { Modifier.isStatic(it.modifiers) } ?: false

/**
 * Finds all listeners in a class. (properties and methods)
 *
 * @author bush
 * @since 1.0.0
 */
@Suppress("UNCHECKED_CAST") // This cannot fail
internal inline val KClass<*>.listeners
    get() = allMembers.filter { it.returnType == typeOf<Listener>() } as Sequence<KCallable<Listener>>
