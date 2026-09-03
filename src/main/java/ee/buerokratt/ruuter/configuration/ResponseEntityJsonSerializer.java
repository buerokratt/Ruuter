package ee.buerokratt.ruuter.configuration;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring Framework 7 (Spring Boot 4) changed how {@link ResponseEntity} serializes to JSON:
 * {@code HttpHeaders} no longer implements {@code Map}, so its actual header values are replaced
 * by ~30 reflected convenience-getter fields, {@code getStatusCodeValue()} was removed, and
 * {@code getStatusCode()} now toString()s as "200 OK" instead of the enum name "OK". This
 * serializer reproduces the pre-upgrade wire format so DSL responses that embed a raw
 * ResponseEntity (e.g. HttpStepResult) don't change shape for existing consumers.
 */
@SuppressWarnings("rawtypes")
public class ResponseEntityJsonSerializer extends JsonSerializer<ResponseEntity> {
    @Override
    public void serialize(ResponseEntity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        value.getHeaders().forEach(headers::put);
        gen.writeStartObject();
        gen.writeObjectField("headers", headers);
        gen.writeObjectField("body", value.getBody());
        gen.writeNumberField("statusCodeValue", value.getStatusCode().value());
        String statusCodeName = value.getStatusCode() instanceof HttpStatus httpStatus
            ? httpStatus.name()
            : String.valueOf(value.getStatusCode().value());
        gen.writeStringField("statusCode", statusCodeName);
        gen.writeEndObject();
    }
}
