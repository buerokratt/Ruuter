package ee.buerokratt.ruuter.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@NoArgsConstructor
public class Step {

    @JsonAlias({"step"})
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
