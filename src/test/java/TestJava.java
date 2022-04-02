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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import static me.bush.illnamethislater.ListenerKt.listener;

/**
 * I was getting noclassdeffound when trying to load this Java
 * class in the other test and I don't care enough to fix it.
 *
 * @author bush
 * @since 1.0.0
 */
@TestInstance(Lifecycle.PER_CLASS)
public class TestJava {
    private static boolean thisShouldChange;
    private boolean thisShouldChangeToo;
    private EventBus eventBus;
    private final Logger logger = LogManager.getLogger();

    @BeforeAll
    public void setup() {
        Configurator.setRootLevel(Level.ALL);

        // Test that init works
        logger.info("Initializing");
        eventBus = new EventBus();

        // Ensure no npe
        logger.info("Logging empty debug info");
        eventBus.debugInfo();

        // Test that basic subscribing reflection works
        logger.info("Subscribing");
        eventBus.subscribe(this);

        logger.info("Testing Events");
    }

    @AfterAll
    public void unsubscribe() {
        logger.info("Unsubscribing");
        eventBus.unsubscribe(this);
        eventBus.debugInfo();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void javaSubscriberTest() {
        eventBus.subscribe(this);
        eventBus.post(new MyEvent());
        Assertions.assertTrue(TestJava.thisShouldChange);
        Assertions.assertTrue(this.thisShouldChangeToo);
        // TODO: 4/2/2022 fix calling from java
    }

    public Listener listener = listener(MyEvent.class, 200, event -> {
        Assertions.assertEquals(event.getSomeString(), "donda");
        event.setSomeString("donda 2");
        this.thisShouldChangeToo = true;
    });

    public static Listener someStaticListener() {
        return listener(MyEvent.class, 100, event -> {
            Assertions.assertEquals(event.getSomeString(), "donda 2");
            thisShouldChange = true;
        });
    }
}
