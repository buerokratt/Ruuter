package ee.buerokratt.ruuter.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class CORSConfiguration {
    private final ApplicationProperties properties;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] allowedOrigins = properties.getCors().getAllowedOrigins().toArray(new String[0]);
                String[] allowedMethods = properties.getIncomingRequests().getAllowedMethodTypes().toArray(new String[0]);
                boolean allowCredentials = Optional.ofNullable(properties.getCors().getAllowCredentials()).orElse(false);
                registry.addMapping("/**")
                    .allowedOriginPatterns(allowedOrigins)
                    .allowCredentials(allowCredentials)
                    .allowedMethods(allowedMethods);
            }
        };
    }
}
