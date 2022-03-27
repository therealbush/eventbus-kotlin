import me.bush.illnamethislater.*

/**
 * @author bush
 * @since 3/13/2022
 */
fun main() {
    EventBus().run {

        subscribe(Subscriber())

        post("Object()")

        register(listener<String> {
            println("it")
        })
    }
}

class Subscriber {
// clean up and writr tests
    @EventListener
    fun onDeez() = listener(listener =  { e: String ->
        println(e.uppercase())
    }, receiveCancelled = false, )
}
