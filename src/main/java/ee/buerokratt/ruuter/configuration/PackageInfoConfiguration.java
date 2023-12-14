package ee.buerokratt.ruuter.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Properties;

@Getter
@Configuration
@PropertySource("classpath:heartbeat.properties")
public class PackageInfoConfiguration {

    @Value("${app.name:}")
    private String appName;

    @Value("${app.version}")
    private String version;

    @Value("${app.packaging.time:}")
    private long packagingTime;

}
