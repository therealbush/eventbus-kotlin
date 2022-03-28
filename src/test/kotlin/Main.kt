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

//    EventBus().run {
//
//        subscribe(Subscriber())
//
//        post("String")
//
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
//    }

    val not = NotDuck()
    not.wtf()
    doDuck(not)
    //doDuck(Any())
}

fun topLevelListener() = listener<Int> { println("topLevelListener(): $it") }

class Subscriber {

    val listener0 get() = listener<String>(500, true, false) {
        println(it.uppercase())
    }
}

fun doDuck(any: Any) {
}

class NotDuck {
    fun wtf() {
        println("wtf")
    }
}

interface Duck {
    fun wtf()
}
