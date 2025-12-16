package org.cafienne.dmn;

import org.cafienne.model.cmmn.definition.DefinitionsDocument;
import org.cafienne.model.cmmn.test.TestScript;
import org.junit.Test;

public class DecisionModelDefinitionTests {

    @Test
    public void instantiateDecisionModelDefinitionFromDefinitions() {
        DefinitionsDocument definitions = TestScript.getDefinitions("testdefinition/dmn/dmn.xml");

    }


}
