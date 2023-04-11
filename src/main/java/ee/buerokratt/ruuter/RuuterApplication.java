package ee.buerokratt.ruuter;

import ee.buerokratt.ruuter.configuration.ApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"io.micrometer.tracing"})
@EnableConfigurationProperties({ApplicationProperties.class})
public class RuuterApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuuterApplication.class, args);
    }
}
