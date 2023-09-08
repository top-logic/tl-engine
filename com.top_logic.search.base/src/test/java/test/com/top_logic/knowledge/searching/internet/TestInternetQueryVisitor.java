/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.searching.internet;


import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.searching.SearchTestSetup;

import com.top_logic.base.search.Query;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.searching.internet.InternetQueryVisitor;


/** 
 * Looks like a testcase for {@link com.top_logic.knowledge.searching.internet.InternetQueryVisitor}
 *
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public class TestInternetQueryVisitor extends BasicTestCase {

    /**
     * Constructor for TestInternetQueryVisitor.
     */
    public TestInternetQueryVisitor(String name) {
        super(name);
    }

    public void testVisit() throws Exception {
        Query  theQuery  = Query.parse("|(#(\"Document\"),&($(\"Wiesbaden\"),$(\"Mainz\")))");
        String theResult = new InternetQueryVisitor().visit(theQuery);
        assertEquals("( OR (Wiesbaden AND Mainz))", theResult);
        theResult = new InternetQueryVisitor(null, null, false).visit(theQuery);
        assertEquals("Wiesbaden Mainz", theResult);
    }

    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
        return SearchTestSetup.createSearchTestSetup(new TestSuite(TestInternetQueryVisitor.class));
    }

    /** Main function for direct testing.
     */
    public static void main(String[] args) {
        Logger.configureStdout();
        
        junit.textui.TestRunner.run (suite ());
    } 
}
