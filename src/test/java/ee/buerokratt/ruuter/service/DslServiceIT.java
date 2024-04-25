package ee.buerokratt.ruuter.service;

import ee.buerokratt.ruuter.BaseIntegrationTest;
import ee.buerokratt.ruuter.domain.steps.AssignStep;
import ee.buerokratt.ruuter.domain.steps.DslStep;
import ee.buerokratt.ruuter.domain.steps.ReturnStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpGetStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpPostStep;
import ee.buerokratt.ruuter.domain.steps.http.HttpQueryArgs;
import ee.buerokratt.ruuter.domain.steps.http.HttpStep;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestPropertySource(properties = { "application.config-path=${user.dir}/src/test/resources/service" })
class DslServiceIT extends BaseIntegrationTest {

    /*
    @Autowired
    private DslService dslService;

    @Value("${application.config-path}")
    private String configPath;

    @Test
    void getDsls_shouldReturnDslsMap() {
        HttpQueryArgs expectedGetArgs = new HttpQueryArgs() {{
            setQuery(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        HttpQueryArgs expectedPostArgs = new HttpQueryArgs() {{
            setBody(new HashMap<>() {{
                put("some_val", "Hello World");
                put("another_val", 123);
            }});
            setUrl("https://example.com/endpoint");
        }};
        HttpStep expectedGetStep = new HttpGetStep() {{
            setName("get_message");
            setArgs(expectedGetArgs);
            setResultName("the_response");
        }};
        HttpStep expectedPostStep = new HttpPostStep() {{
            setName("post_message");
            setArgs(expectedPostArgs);
            setResultName("the_message");
        }};
        AssignStep<Object> expectedAssignStep = new AssignStep<>() {{
            setName("assign_value");
            setAssign(new HashMap<>() {{
                put("stringValue", "Bürokratt");
                put("integerValue", 2021);
            }});
        }};
        ReturnStep expectedReturnStep = new ReturnStep() {{
            setName("return_value");
            setReturnValue("return_value");
        }};

        Map<String, Map<String, Map<String, DslStep>>> dsls = dslService.getDsls(configPath);
        List<String> stepNames = new ArrayList<>(dsls.get("POST").get("test-conf").keySet());

        assertEquals("get_message", stepNames.get(0));
        assertEquals("post_message", stepNames.get(1));
        assertEquals("assign_value", stepNames.get(2));
        assertEquals("return_value", stepNames.get(3));
        assertEquals(expectedGetStep, dsls.get("POST").get("test-conf").get("get_message"));
        assertEquals(expectedPostStep, dsls.get("POST").get("test-conf").get("post_message"));
        assertEquals(expectedAssignStep, dsls.get("POST").get("test-conf").get("assign_value"));
        assertEquals(expectedReturnStep, dsls.get("POST").get("test-conf").get("return_value"));
    }

    @Test
    void getDsls_shouldAddDefinedHeadersToHttpSteps() {
        Map<String, Map<String, Map<String, DslStep>>> dsls = dslService.getDsls(configPath);
        Map<String, DslStep> steps = new HashMap<>(dsls.get("POST").get("pass-headers-with-request"));

        HttpGetStep httpGetStep = (HttpGetStep) steps.get("get_message");
        HttpPostStep httpPostStep = (HttpPostStep) steps.get("post_message");

        HashMap<String, String> httpGetStepHeaders = new HashMap<>() {{
            put("Connection", "keep-alive");
        }};
        HashMap<String, String> httpPostStepHeaders = new HashMap<>() {{
            put("Cache-Control", "no-cache");
        }};

        assertEquals(httpGetStepHeaders, httpGetStep.getArgs().getHeaders());
        assertEquals(httpPostStepHeaders, httpPostStep.getArgs().getHeaders());
    }

    @Test
    void getDsls_shouldPutAllDslsToPostAndGetKeys() {
        Map<String, Map<String, Map<String, DslStep>>> dsls = dslService.getDsls(configPath);

        assertEquals(2, dsls.keySet().size());
        assertTrue(dsls.containsKey("POST"));
        assertTrue(dsls.containsKey("GET"));
    }

     */
}
