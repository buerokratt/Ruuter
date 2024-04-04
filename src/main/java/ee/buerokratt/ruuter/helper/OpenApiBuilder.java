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
import org.springframework.web.bind.annotation.RequestParam;

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

    public OpenApiBuilder addService(Dsl dsl) {

        DeclarationStep declaration = dsl.getDeclaration();




        PathItem pathItem = new PathItem();
        if (declaration.getMethod().toUpperCase().equals("POST")) {
            Schema requestBodySchema = new Schema();
            requestBodySchema.setType("object");
            declaration.getUsedFields().forEach(
                field -> requestBodySchema.addProperties(field.getField(),
                    new Schema().type(field.getType()).description(field.getDescription()))
            );
            RequestBody requestBody = new RequestBody();
            requestBody.setContent(
                new Content()
                    .addMediaType("application/json",
                        new MediaType().schema(requestBodySchema))
            );
            pathItem.post( new Operation().requestBody(requestBody));
        }  if (declaration.getMethod().toUpperCase().equals("GET")) {

            declaration.getUsedFields().forEach(
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
        paths.addPathItem("/" + declaration.getNamespace() + "/" + declaration.getDeclare(), pathItem);


        this.openAPI.setPaths(paths);

        return this;
    }

    public OpenAPI build() {
        return this.openAPI;
    }

}
