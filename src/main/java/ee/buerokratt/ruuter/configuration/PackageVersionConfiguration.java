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

    // Base64-encoded by CI (see check-version.yml) to survive being embedded as a single Properties
    // value - .env is loaded via the default PropertySourceFactory, i.e. as a plain Properties file,
    // not shell syntax, so a raw multi-line changelog would need Properties escaping instead.
    // Optional (defaults to empty) since local/non-CI .env files don't set it.
    @Value("${CHANGELOG:}")
    private String changelogBase64;

}
