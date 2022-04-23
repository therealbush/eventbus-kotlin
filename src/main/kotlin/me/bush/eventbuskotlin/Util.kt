package me.bush.eventbuskotlin

import org.apache.logging.log4j.LogManager
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

// by bush, unchanged since 1.0.0

internal val LOGGER = LogManager.getLogger("EventBus")

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
 * I could not find any other way to do this, not even on StackOverflow.
 *
 * Funny how this solution is 1/10th the lines and always works.
 */
internal fun <R> KCallable<R>.handleCall(receiver: Any? = null): R {
    isAccessible = true
    return runCatching { call(receiver) }.getOrElse { call() }
}

/*
private val KCallable<*>.isJvmStatic
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
private inline val KClass<*>.listeners
    get() = allMembers.filter {
        // Set nullability to false, so this will detect listeners in Java
        // with "!" nullability. Also make sure there are no parameters.
        it.returnType.withNullability(false) == typeOf<Listener>() && it.valueParameters.isEmpty()
    } as Sequence<KCallable<Listener>>

/**
 * Finds all listeners in [subscriber].
 *
 * @return A list of listeners belonging to [subscriber], or null if an exception is caught.
 */
internal fun getListeners(subscriber: Any, config: Config) = runCatching {
    subscriber::class.listeners.filter { !config.annotationRequired || it.hasAnnotation<EventListener>() }
        .map { member -> member.handleCall(subscriber).also { it.subscriber = subscriber } }.toList()
}.onFailure { config.logger.error("Unable to register listeners for subscriber $subscriber", it) }.getOrNull()

/**
 * An annotation that must be used to identify listeners if [Config.annotationRequired] is `true`.
 *
 * [Information and examples](https://github.com/therealbush/eventbus-kotlin#annotationRequired)
 */
annotation class EventListener
