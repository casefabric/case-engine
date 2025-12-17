package org.cafienne.model.cmmn.test.basic;

import org.cafienne.model.cmmn.actorapi.command.StartCase;
import org.cafienne.model.cmmn.definition.CaseDefinition;
import org.cafienne.model.cmmn.instance.State;
import org.cafienne.model.cmmn.instance.Transition;
import org.cafienne.model.cmmn.test.TestScript;
import org.cafienne.model.cmmn.actorapi.command.plan.task.humantask.CompleteHumanTask;
import org.cafienne.model.cmmn.actorapi.event.plan.task.humantask.HumanTaskAssigned;
import org.cafienne.model.cmmn.actorapi.event.plan.task.humantask.HumanTaskDueDateFilled;
import org.cafienne.util.json.ValueMap;
import org.junit.Test;

import static org.cafienne.model.cmmn.test.TestScript.*;

public class TestHelloWorld {
    private final CaseDefinition definitions = loadCaseDefinition("testdefinition/helloworld.xml");

    @Test
    public void testHelloWorld() {
        String caseInstanceId = "HelloWorldTest";
        TestScript testCase = new TestScript("hello-world");
        ValueMap greeting = new ValueMap("Greeting", new ValueMap("Message", "hello", "To", testUser.id(), "From", testUser.id()));

        StartCase startCase = createCaseCommand(testUser, caseInstanceId, definitions, greeting);
        testCase.addStep(startCase, casePlan -> {
            casePlan.print();
            String taskId = casePlan.assertHumanTask("Receive Greeting and Send response").getId();
            casePlan.getEvents().filter(HumanTaskDueDateFilled.class).assertSize(1);
            casePlan.getEvents().filter(HumanTaskAssigned.class).assertSize(1);

            CompleteHumanTask completeTask1 = new CompleteHumanTask(testUser, caseInstanceId, taskId, new ValueMap());
            testCase.insertStep(completeTask1, casePlan2 -> {
                casePlan2.print();
                casePlan2.assertLastTransition(Transition.Create, State.Active, State.Null);
                casePlan2.assertPlanItem("Receive Greeting and Send response").assertState(State.Completed);
                casePlan2.assertPlanItem("Read response").assertState(State.Active);

            });
        });

        testCase.runTest();
    }

    @Test
    public void testHelloWorldWithoutAssignee() {
        String caseInstanceId = "HelloWorldTest";
        TestScript testCase = new TestScript("hello-world");
        ValueMap greeting = new ValueMap("Greeting", new ValueMap("Message", "hello", "To", "", "From", testUser.id()));

        StartCase startCase = createCaseCommand(testUser, caseInstanceId, definitions, greeting);
        testCase.addStep(startCase, action -> {
            TestScript.debugMessage("Events: " + action.getTestCommand());
            action.getEvents().filter(HumanTaskDueDateFilled.class).assertSize(1);
            action.getEvents().filter(HumanTaskAssigned.class).assertSize(0);
        });

        testCase.runTest();
    }
}
