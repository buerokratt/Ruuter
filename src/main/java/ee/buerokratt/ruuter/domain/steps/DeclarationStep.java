package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.DslInstance;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (allowedBody == null && allowlist != null && allowlist.body != null) {
            allowedBody = allowlist.body.stream().map(field -> field.getField()).toList();
            warnOnCasingCollisions("body", allowedBody);
        }
        return allowedBody;
    }

    public List<String> getAllowedHeader() {
        if (allowedHeader == null && allowlist != null && allowlist.header != null) {
            allowedHeader = allowlist.header.stream().map(field -> field.getField()).toList();
            warnOnCasingCollisions("header", allowedHeader);
        }
        return allowedHeader;
    }

    public List<String> getAllowedParams() {
        if (allowedParams == null && allowlist != null && allowlist.params != null) {
            allowedParams = allowlist.params.stream().map(field -> field.getField()).toList();
            warnOnCasingCollisions("params", allowedParams);
        }
        return allowedParams;
    }

    // Allowlist matching against request fields is case-insensitive (see DslService.filterFields),
    // so two declared fields differing only by case (e.g. "Email" and "email") collapse into a
    // single effective entry. Warn once here, at first (and only, due to memoization above)
    // resolution, rather than on every request that hits filterFields/checkFields.
    private void warnOnCasingCollisions(String fieldSetName, List<String> fields) {
        if (fields.stream().anyMatch(Objects::isNull)) {
            log.warn("Allowlist '{}' declares a field with no name - check for a malformed entry " +
                "(e.g. missing the 'field:' key)", fieldSetName);
        }

        fields.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(String::toLowerCase))
            .values().stream()
            .filter(group -> group.size() > 1)
            .forEach(group -> log.warn(
                "Allowlist '{}' declares fields that differ only by case and will be treated as the same field: {}",
                fieldSetName, group));
    }

    @Getter
    public class AllowList {
        List<DslField> body;
        @JsonAlias("headers")
        List<DslField> header;
        List<DslField> params;
    }
}
