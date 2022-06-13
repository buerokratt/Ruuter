package ee.buerokratt.ruuter.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Configuration
public class ScriptEngineConfiguration {

    public ScriptEngineConfiguration() {
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
    }

    @Bean
    public ScriptEngine scriptEngine() {
        return new ScriptEngineManager().getEngineByName("graal.js");
    }
}
