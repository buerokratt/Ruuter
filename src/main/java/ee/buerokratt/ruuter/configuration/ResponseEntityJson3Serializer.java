package ee.buerokratt.ruuter.configuration;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Boot 4's default HTTP message converter serializes via Jackson 3 (a JsonMapper), not the
 * Jackson 2 ObjectMapper bean this app otherwise uses - so the wire-format fix for ResponseEntity
 * has to be registered as a Jackson 3 module too. See ResponseEntityJsonSerializer for why this is
 * needed: Spring Framework 7's HttpHeaders no longer implements Map (exploding into ~30 reflected
 * getters), getStatusCodeValue() was removed, and getStatusCode() toString()s as "200 OK" instead
 * of the enum name "OK".
 */
@SuppressWarnings("rawtypes")
public class ResponseEntityJson3Serializer extends ValueSerializer<ResponseEntity> {
    @Override
    public void serialize(ResponseEntity value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        value.getHeaders().forEach(headers::put);
        gen.writeStartObject();
        gen.writePOJOProperty("headers", headers);
        gen.writePOJOProperty("body", value.getBody());
        gen.writeNumberProperty("statusCodeValue", value.getStatusCode().value());
        String statusCodeName = value.getStatusCode() instanceof HttpStatus httpStatus
            ? httpStatus.name()
            : String.valueOf(value.getStatusCode().value());
        gen.writeStringProperty("statusCode", statusCodeName);
        gen.writeEndObject();
    }
}
