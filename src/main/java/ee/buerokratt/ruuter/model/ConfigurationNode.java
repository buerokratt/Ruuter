package ee.buerokratt.ruuter.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Data
@Component
public class ConfigurationNode {

    private HashMap<String, String> nodes = new HashMap<>();
}
