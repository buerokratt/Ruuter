package ee.buerokratt.ruuter.domain.steps;

import ee.buerokratt.ruuter.BaseStepTest;
import ee.buerokratt.ruuter.domain.steps.conditional.Condition;
import ee.buerokratt.ruuter.domain.steps.conditional.SwitchStep;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class SwitchStepTest extends BaseStepTest {

    @Mock
    private ScriptingHelper scriptingHelper;

    @BeforeEach
    protected void mockScriptingHelper() {
        when(ci.getScriptingHelper()).thenReturn(scriptingHelper);
    }

    @Test
    void execute_shouldJumpToCorrectStep() {
        HashMap<String, Object> testContext = new HashMap<>() {{
            put("currentTime", "Sunday");
        }};
        Condition firstCondition = new Condition() {{
            setNextStepName("second_step");
            setConditionStatement("${currentTime == \"Friday\"}");
        }};
        Condition secondCondition = new Condition() {{
            setNextStepName("third_step");
            setConditionStatement("${currentTime == \"Saturday\"}");
        }};
        Condition thirdCondition = new Condition() {{
            setNextStepName("fourth_step");
            setConditionStatement("${currentTime == \"Sunday\"}");
        }};
        List<Condition> conditions = List.of(firstCondition, secondCondition, thirdCondition);
        SwitchStep switchStep = new SwitchStep() {{
            setConditions(conditions);
            setNextStepName("fifth_step");
            setName("switch");
        }};

        when(ci.getContext()).thenReturn(testContext);
        when(scriptingHelper.evaluateScripts(eq("${currentTime == \"Sunday\"}"), eq(testContext), anyMap(), anyMap())).thenReturn(true);
        when(scriptingHelper.evaluateScripts(eq("${currentTime == \"Saturday\"}"), eq(testContext), anyMap(), anyMap())).thenReturn(false);
        when(scriptingHelper.evaluateScripts(eq("${currentTime == \"Friday\"}"), eq(testContext), anyMap(), anyMap())).thenReturn(false);
        switchStep.execute(ci);

        assertEquals("fourth_step", switchStep.getNextStepName());
    }

}
