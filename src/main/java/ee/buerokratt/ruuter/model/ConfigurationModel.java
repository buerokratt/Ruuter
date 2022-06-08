package ee.buerokratt.ruuter.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Service
@NoArgsConstructor
public class ConfigurationModel {

    private HashMap<String, Map<String, Step>> configurations = new HashMap<>();
}
