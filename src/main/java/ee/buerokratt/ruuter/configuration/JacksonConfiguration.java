package ee.buerokratt.ruuter.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;

import java.util.Map;

@Configuration
public class JacksonConfiguration {

    @Value("${application.allowDuplicateRequestKeys:false}")
    private boolean allowDuplicateRequestKeys;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = allowDuplicateRequestKeys
            ? new ObjectMapper()
            : new ObjectMapper().configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        SimpleModule module = new SimpleModule();
        module.addSerializer(ResponseEntity.class, new ResponseEntityJsonSerializer());
        mapper.registerModule(module);

        return mapper;
    }

    @Bean
    public ObjectMapper ymlMapper() {
        return new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // Spring Boot 4's web message converter serializes via Jackson 3 (JsonMapper), not the Jackson
    // 2 ObjectMapper bean above - see ResponseEntityJson3Serializer for why this is needed.
    @Bean
    public JsonMapperBuilderCustomizer responseEntityJsonMapperCustomizer() {
        return builder -> builder.addModule(new tools.jackson.databind.module.SimpleModule()
            .addSerializer(ResponseEntity.class, new ResponseEntityJson3Serializer()));
    }
}

