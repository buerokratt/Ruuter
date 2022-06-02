package ee.buerokratt.ruuter.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
@Service
@NoArgsConstructor
public class ConfigurationModel {

    private HashMap<String, List<Step>> configurations = new HashMap<>();
}
