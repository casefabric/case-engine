package org.cafienne.model.cmmn.test;

import org.cafienne.model.cmmn.test.basic.TestRepeatRule;
import org.cafienne.model.cmmn.test.casefile.TestCaseFileTransition;
import org.cafienne.model.cmmn.test.casefile.TestRepetitiveFileItems;
import org.cafienne.model.cmmn.test.task.TestGetListGetDetails;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    TestRepeatRule.class,
    TestRepetitiveFileItems.class,
    TestCaseFileTransition.class,
    TestGetListGetDetails.class
    })
public class RepetitionTests {

}
