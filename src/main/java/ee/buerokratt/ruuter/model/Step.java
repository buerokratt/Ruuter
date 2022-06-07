package ee.buerokratt.ruuter.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Step {

    @JsonAlias({"step"})
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
