package ee.buerokratt.ruuter.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
                boolean allowCredentials = properties.getCors().isAllowCredentials();
                registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods(allowedMethods)
                    .allowCredentials(allowCredentials);
            }
        };
    }
}
