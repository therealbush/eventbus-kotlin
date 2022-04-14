import me.bush.illnamethislater.Listener;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import me.bush.illnamethislater.EventBus;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Test;

import static me.bush.illnamethislater.ListenerKt.listener;

/**
 * I was getting NCDFE when trying to load this class
 * from the other test and I don't care enough to fix it.
 *
 * @author bush
 * @since 1.0.0
 */
@TestInstance(Lifecycle.PER_CLASS)
public class JavaTest {
    private EventBus eventBus;
    private final Logger logger = LogManager.getLogger();

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
        Assertions.assertEquals(event.getCount(), 4);
    }

    public Listener someInstanceListenerField = listener(SimpleEvent.class, event -> {
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

    public static Listener someStaticListenerField = listener(SimpleEvent.class, event -> {
        event.setCount(event.getCount() + 1);
    });
}
