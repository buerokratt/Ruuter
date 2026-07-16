package ee.buerokratt.ruuter.helper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ee.buerokratt.ruuter.domain.Dsl;
import ee.buerokratt.ruuter.domain.steps.DeclarationStep;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DeclarationStep instances are built by deserializing a YAML snippet through the same ymlMapper
 * production uses (matching JacksonConfiguration's bean) - DeclarationStep only exposes @Getter, no
 * setters, and its AllowList is a non-static inner class, so this is the realistic way to construct
 * one (also how it's actually built in production, via DslMappingHelper).
 */
class OpenApiBuilderTest {

    private static final ObjectMapper YML_MAPPER =
        new ObjectMapper(new YAMLFactory()).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Dsl dslWithDeclaration(String declarationYaml) throws Exception {
        DeclarationStep declaration = YML_MAPPER.readValue(declarationYaml, DeclarationStep.class);
        Map<String, DslStep> steps = new LinkedHashMap<>();
        steps.put("declare", declaration);
        return new Dsl(steps);
    }

    @Test
    void constructor_shouldSetTitleAndVersion() {
        OpenAPI openAPI = new OpenApiBuilder("BYK", "1.0").build();

        assertEquals("BYK", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion());
    }

    @Test
    void addService_shouldAddPostOperationWithRequestBodySchema() throws Exception {
        Dsl dsl = dslWithDeclaration("""
            method: post
            description: Creates a resource
            returns: the created resource
            namespace: myproject
            allowlist:
              body:
                - field: name
                  type: string
                  description: the resource name
            """);

        OpenAPI openAPI = new OpenApiBuilder("BYK", "1.0").addService(dsl, "create-resource").build();

        PathItem pathItem = openAPI.getPaths().get("/myproject/create-resource");
        assertEquals("Creates a resource", pathItem.getDescription());
        Schema<?> requestSchema = pathItem.getPost().getRequestBody().getContent().get("application/json").getSchema();
        assertEquals("object", requestSchema.getType());
        assertTrue(requestSchema.getProperties().containsKey("name"));
        assertEquals("the created resource", pathItem.getPost().getResponses().get("200").getDescription());
    }

    @Test
    void addService_shouldAddPutOperation_sameAsPost() throws Exception {
        Dsl dsl = dslWithDeclaration("""
            method: put
            description: Updates a resource
            returns: the updated resource
            namespace: myproject
            """);

        OpenAPI openAPI = new OpenApiBuilder("BYK", "1.0").addService(dsl, "update-resource").build();

        PathItem pathItem = openAPI.getPaths().get("/myproject/update-resource");
        assertEquals("Updates a resource", pathItem.getDescription());
        assertEquals("the updated resource", pathItem.getPost().getResponses().get("200").getDescription());
    }

    @Test
    void addService_shouldAddGetOperationWithQueryParameters() throws Exception {
        Dsl dsl = dslWithDeclaration("""
            method: get
            description: Looks up a resource
            namespace: myproject
            allowlist:
              params:
                - field: id
                  description: the resource id
            """);

        OpenAPI openAPI = new OpenApiBuilder("BYK", "1.0").addService(dsl, "get-resource").build();

        PathItem pathItem = openAPI.getPaths().get("/myproject/get-resource");
        Parameter parameter = pathItem.getGet().getParameters().get(0);
        assertEquals("id", parameter.getName());
        assertEquals("the resource id", parameter.getDescription());
    }

    @Test
    void addService_shouldAddNoOperation_whenGetHasNoAllowlistedParams() throws Exception {
        Dsl dsl = dslWithDeclaration("""
            method: get
            description: Looks up a resource
            namespace: myproject
            """);

        OpenAPI openAPI = new OpenApiBuilder("BYK", "1.0").addService(dsl, "get-resource").build();

        PathItem pathItem = openAPI.getPaths().get("/myproject/get-resource");
        assertNull(pathItem.getGet());
        assertNull(pathItem.getPost());
    }

    @Test
    void addService_shouldAccumulatePathsAcrossMultipleCalls() throws Exception {
        Dsl first = dslWithDeclaration("""
            method: get
            namespace: myproject
            allowlist:
              params:
                - field: id
                  description: the resource id
            """);
        Dsl second = dslWithDeclaration("""
            method: post
            namespace: myproject
            returns: the created resource
            """);

        OpenApiBuilder builder = new OpenApiBuilder("BYK", "1.0")
            .addService(first, "get-resource")
            .addService(second, "create-resource");

        OpenAPI openAPI = builder.build();

        assertEquals(2, openAPI.getPaths().size());
        assertTrue(openAPI.getPaths().containsKey("/myproject/get-resource"));
        assertTrue(openAPI.getPaths().containsKey("/myproject/create-resource"));
    }
}
