package me.bush.illnamethislater

import java.lang.reflect.Modifier
import kotlin.reflect.*
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMembers
import kotlin.reflect.full.valueParameters
import kotlin.reflect.full.withNullability
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

// by bush, unchanged since 1.0.0

/**
 * Using [KClass.members] only returns public members, and using [KClass.declaredMembers]
 * doesn't return inherited members. This returns all members, private and inherited.
 */
internal val <T : Any> KClass<T>.allMembers
    get() = (declaredMembers + allSuperclasses.flatMap { it.declaredMembers }).asSequence()

/**
 * Checks if a [KCallable] is static on the jvm, and handles invocation accordingly.
 *
 * I have tried to check if the callable needs a receiver, and have left my code
 * below, but for some reason a property (private, no getter) of a companion object
 * (which is static in bytecode) requires a receiver, while an identical property in a
 * non companion object does not, and will throw if one is passed.
 *
 * I am not aware of a way to check if a property belongs to a companion object, is static,
 * or requires certain arguments. (instanceParameter exists, but it will throw if it is an argument)
 * I thought maybe I was using the wrong methods, but apart from KProperty#get, (which is only for
 * properties, and only accepts arguments of type `Nothing` when `T` is star projected or covariant)
 * I could not find any other way to do this, not even on StackOverFlow.
 *
 * Funny how this solution is 1/10th the lines and always works.
 */
internal fun <R> KCallable<R>.handleCall(receiver: Any? = null): R {
    isAccessible = true
    return runCatching { call(receiver) }.getOrElse { call() }
}

/*
internal val KCallable<*>.isJvmStatic
    get() = when (this) {
        is KFunction -> Modifier.isStatic(javaMethod?.modifiers ?: 0)
        is KProperty -> this.javaGetter == null && Modifier.isStatic(javaField?.modifiers ?: 0)
        else -> false
    }
 */

/**
 * Finds all members of return type [Listener]. (properties and methods)
 */
@Suppress("UNCHECKED_CAST") // This cannot fail
internal inline val KClass<*>.listeners
    // Force nullability to false, so this will detect listeners in Java
    // with "!" nullability. Also make sure there are no parameters.
    get() = allMembers.filter {
        it.returnType.withNullability(false) == typeOf<Listener>() && it.valueParameters.isEmpty()
    } as Sequence<KCallable<Listener>>
