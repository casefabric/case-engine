package org.cafienne.model.cmmn.test.plan;

import org.cafienne.model.cmmn.actorapi.command.StartCase;
import org.cafienne.model.cmmn.definition.CaseDefinition;
import org.cafienne.model.cmmn.test.TestScript;
import org.cafienne.model.cmmn.test.assertions.CaseAssertion;
import org.cafienne.util.json.ValueMap;
import org.junit.Test;

import static org.cafienne.model.cmmn.test.TestScript.*;

public class TestImmediateMilestone {
    private final CaseDefinition definitions = loadCaseDefinition("testdefinition/milestonedependency.xml");

    @Test
    public void testImmediateMilestone() {
        String caseInstanceId = "MilestoneDependencyTest";
        TestScript testCase = new TestScript("MilestoneDependencyTest");
        ValueMap greeting = new ValueMap();

        StartCase startCase = createCaseCommand(testUser, caseInstanceId, definitions, greeting);
        testCase.addStep(startCase, CaseAssertion::print);

        testCase.runTest();
    }
}
