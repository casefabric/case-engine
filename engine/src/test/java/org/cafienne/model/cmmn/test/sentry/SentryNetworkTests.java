/*
 * Copyright 2014 - 2019 Cafienne B.V.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.cafienne.model.cmmn.test.sentry;

import org.cafienne.model.cmmn.test.casefile.TestRepetitiveFileItems;
import org.cafienne.model.cmmn.test.expression.TestVariousSpelExpressions;
import org.cafienne.model.cmmn.test.task.TestGetListGetDetails;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        TestMilestone.class,
        TestRepetitiveFileItems.class,
        TestGetListGetDetails.class,
        TestVariousSpelExpressions.class
    })
public class SentryNetworkTests {

}
