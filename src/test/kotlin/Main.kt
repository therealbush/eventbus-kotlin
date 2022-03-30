import me.bush.illnamethislater.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.config.Configurator

/**
 * @author bush
 * @since 1.0.0
 */
fun main() {
    Configurator.setRootLevel(Level.INFO)

    EventBus().run {

        subscribe(Subscriber())

        post(External())

//        val key = register(listener<Int> {
//            println(it)
//        })
//
//        val topLevelListenerKey = register(topLevelListener())
//
//        unsubscribe(key)
//
//        unsubscribe(topLevelListenerKey)
//
//        debugInfo()
    }

}

fun topLevelListener() = listener<Int> {
    println("topLevelListener(): $it")
}

class Subscriber {

    val listener0 = listener<External>(500) {
        println("listener 0")
        println(it.canceled)
        it.canceled = true
    }

    val listener1 = listener<External>(250, receiveCancelled = true) {
        println("listener 1")
        println(it.canceled)
        it.canceled = false
    }

    val listener2 get() = listener<External> {
        println("listener 2")
        println(it.canceled)
        it.canceled = true
    }

    fun listener3() = listener<External>(-250) {
        println("listener 3")
        println(it.canceled)
    }
}

class External {
    var canceled = false
}
