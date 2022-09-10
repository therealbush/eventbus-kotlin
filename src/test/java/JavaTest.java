import me.bush.eventbuskotlin.EventBus;
import me.bush.eventbuskotlin.EventHandler;
import me.bush.eventbuskotlin.EventListener;
import me.bush.eventbuskotlin.Listener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static me.bush.eventbuskotlin.ListenerKt.listener;

/**
 * @author bush
 * @since 1.0.0
 */
@TestInstance(Lifecycle.PER_CLASS)
public class JavaTest {
    private final Logger logger = LogManager.getLogger();
    private EventBus eventBus;

    @BeforeAll
    public void setup() {
        Configurator.setRootLevel(Level.ALL);
        logger.info("Running Java tests");
        eventBus = new EventBus();
        eventBus.subscribe(this);
    }

    @Test
    public void javaSubscriberTest() {
        eventBus.subscribe(this);
        SimpleEvent event = new SimpleEvent();
        eventBus.post(event);
        Assertions.assertEquals(8, event.getCount());
    }

    public Listener someInstanceListenerField = listener(SimpleEvent.class, event -> {
        event.setCount(event.getCount() + 1);
    });

    public static Listener someStaticListenerField = listener(SimpleEvent.class, event -> {
        event.setCount(event.getCount() + 1);
    });

    public Listener someInstanceListenerMethod() {
        return listener(SimpleEvent.class, event -> {
            event.setCount(event.getCount() + 1);
        });
    }

    public static Listener someStaticListenerMethod() {
        return listener(SimpleEvent.class, event -> {
            event.setCount(event.getCount() + 1);
        });
    }

    @EventHandler
    public void someAnnotatedListenerMethod(SimpleEvent event) {
        event.setCount(event.getCount() + 1);
    }

    @EventHandler
    private void somePrivateAnnotatedListenerMethod(SimpleEvent event) {
        event.setCount(event.getCount() + 1);
    }

    @EventHandler
    public static void someStaticAnnotatedListenerMethod(SimpleEvent event) {
        event.setCount(event.getCount() + 1);
    }

    @EventHandler
    private static void someStaticPrivateAnnotatedListenerMethod(SimpleEvent event) {
        event.setCount(event.getCount() + 1);
    }
}
