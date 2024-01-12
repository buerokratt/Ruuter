package ee.buerokratt.ruuter.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("file:/app/.env")
public class PackageVersionConfiguration {

    @Value("${RELEASE}")
    private String release;

    @Value("${VERSION}")
    private String version;

    @Value("${BUILD}")
    private String build;

    @Value("${FIX}")
    private String fix;

    @Value("${BUILDTIME}")
    private Long buildTime;

}
