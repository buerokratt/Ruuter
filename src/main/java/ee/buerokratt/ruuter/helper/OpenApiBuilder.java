package ee.buerokratt.ruuter.helper;

import ee.buerokratt.ruuter.domain.Dsl;
import ee.buerokratt.ruuter.domain.steps.DeclarationStep;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

public class OpenApiBuilder {

    private OpenAPI openAPI;

    public OpenApiBuilder(String name, String version) {
        openAPI = new OpenAPI();
        openAPI.info(
            new Info()
                .title(name)
                .version(version)
        );
    }

    public OpenApiBuilder addService(Dsl dsl, String path) {

        DeclarationStep declaration = dsl.getDeclaration();

        PathItem pathItem = new PathItem();
        if (declaration.getMethod().toUpperCase().equals("POST")) {
            Schema requestBodySchema = new Schema();
            requestBodySchema.setType("object");
            declaration.getAllowlist().getBody().forEach(
                field -> requestBodySchema.addProperties(field.getField(),
                    new Schema().type(field.getType()).description(field.getDescription()))
            );
            RequestBody requestBody = new RequestBody();
            requestBody.setContent(
                new Content()
                    .addMediaType("application/json",
                        new MediaType().schema(requestBodySchema))
            );
            ApiResponse success = new ApiResponse();
            success.setDescription(declaration.getReturns());

            pathItem.post( new Operation().requestBody(requestBody)
                .responses(new ApiResponses().addApiResponse("200", success)));
        } else if (declaration.getMethod().toUpperCase().equals("GET")) {

            declaration.getAllowlist().getParams().forEach(
                field -> {
                    Parameter requestParam = new Parameter();
                    requestParam.setName(field.getField());
                    requestParam.setDescription(field.getDescription());
                    pathItem.get(new Operation().addParametersItem(requestParam));
                }
            );
        }

        pathItem.setDescription(declaration.getDescription());

        Paths paths = new Paths();
        paths.addPathItem("/" + declaration.getNamespace() + "/" + path, pathItem);

        this.openAPI.setPaths(paths);

        return this;
    }

    public OpenAPI build() {
        return this.openAPI;
    }

}
