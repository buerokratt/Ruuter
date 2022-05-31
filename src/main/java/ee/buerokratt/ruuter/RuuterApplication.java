package ee.buerokratt.ruuter;

import ee.buerokratt.ruuter.configuration.routing.Configuration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class RuuterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuuterApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void executeAfterSpringAppStarted() {
        Configuration.load();
    }

}
