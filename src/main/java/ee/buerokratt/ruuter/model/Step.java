package ee.buerokratt.ruuter.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ee.buerokratt.ruuter.model.step.types.AssignStep;
import ee.buerokratt.ruuter.model.step.types.HttpStep;
import ee.buerokratt.ruuter.model.step.types.ReturnStep;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@NoArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "call")
@JsonSubTypes({
    @JsonSubTypes.Type(value = HttpStep.class, name = "http.get"),
    @JsonSubTypes.Type(value = HttpStep.class, name = "http.post"),
    @JsonSubTypes.Type(value = ReturnStep.class, name = "return"),
    @JsonSubTypes.Type(value = AssignStep.class, name = "assign"),
})
public class Step {

    @JsonAlias({"step"})
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
