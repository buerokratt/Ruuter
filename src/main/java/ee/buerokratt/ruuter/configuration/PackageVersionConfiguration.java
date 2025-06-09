package ee.buerokratt.ruuter.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("file:/app/.env")
public class PackageVersionConfiguration {

    @Value("${BUILDTIME}")
    private Long buildTime;

    @Value("${MAJOR}")
    private String major;

    @Value("${MINOR}")
    private String minor;

    @Value("${PATCH}")
    private String patch;

}
