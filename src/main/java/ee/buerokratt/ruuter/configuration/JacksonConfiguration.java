package ee.buerokratt.ruuter.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.Map;

@Configuration
public class JacksonConfiguration {

    @Value("${application.allowDuplicateRequestKeys:false}")
    private boolean allowDuplicateRequestKeys;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        if (allowDuplicateRequestKeys)
            return new ObjectMapper();
        else
            return new ObjectMapper().configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
    }

    @Bean
    public ObjectMapper ymlMapper() {
        return new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}

