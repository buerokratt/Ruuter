package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class DeclarationStep extends DslStep {
    String version;
    String description;

    String method;
    String accepts;
    String returns;

    String namespace;

    AllowList allowlist;

    List<String> allowedBody;
    List<String> allowedHeader;
    List<String> allowedParams;

    @Override
    protected void executeStepAction(DslInstance di) {
        return;
    }

    @Override
    public String getType() {
        return "declare";
    }

    public List<String> getAllowedBody() {
        if (allowedBody == null) {
            allowedBody = allowlist.body.stream().map(field -> field.getField()).toList();
        }
        return allowedBody;
    }

    public List<String> getAllowedHeader() {
        if (allowedHeader == null) {
            allowedHeader = allowlist.header.stream().map(field -> field.getField()).toList();
        }
        return allowedHeader;
    }

    public List<String> getAllowedParams() {
        if (allowedParams == null) {
            allowedParams = allowlist.params.stream().map(field -> field.getField()).toList();
        }
        return allowedParams;
    }

    @Getter
    public class AllowList {
        List<DslField> body;
        List<DslField> header;
        List<DslField> params;
    }

}
