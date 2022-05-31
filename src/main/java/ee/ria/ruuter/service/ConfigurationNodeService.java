package ee.ria.ruuter.service;

import ee.ria.ruuter.model.ConfigurationNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ConfigurationNodeService {

    @Autowired
    private final ConfigurationNode configurationNode;

    public ConfigurationNodeService(ConfigurationNode confNode) {
        this.configurationNode = confNode;
    }

    public void addNode(String confName, String conf) {
        HashMap<String, String> currentNodes = configurationNode.getNodes();
        currentNodes.put(confName, conf);
        configurationNode.setNodes(currentNodes);
    }

    public Map<String, String> getNodes() {
        return configurationNode.getNodes();
    }
}
