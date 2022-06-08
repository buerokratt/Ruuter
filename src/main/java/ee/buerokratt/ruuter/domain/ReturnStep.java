package ee.buerokratt.ruuter.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class ReturnStep extends ConfigurationStep {
    @JsonAlias({"return"})
    private String returnValue;

    @Override
    public void execute(ConfigurationInstance configurationInstance) {
        configurationInstance.setReturnValue(returnValue);
        super.execute(configurationInstance);
    }
}
