/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.SimpleCSVTokenizer;

/**
 * Test class to check the {@link SimpleCSVTokenizer}
 *
 * @author  <a href=mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestSimpleCSVTokenizer extends BasicTestCase {

    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestSimpleCSVTokenizer (String name) {
        super (name);
    }

    // Test methodes

    /** 
     * Main and single testcase here.
     */
    public void testMain () throws Exception
    {
        SimpleCSVTokenizer simpleT = new SimpleCSVTokenizer("");
        assert simpleT.nextToken().length() == 0 : 
                            "First token of empty line wrong";
        assert simpleT.nextToken() == null : 
                            "Second token of empty line wrong";

        simpleT = new SimpleCSVTokenizer(";");
        assert simpleT.nextToken().length() == 0 : 
                            "First token of ; line wrong";
        assert simpleT.nextToken().length() == 0 : 
                            "First token of ; line wrong";
        assert simpleT.nextToken() == null : 
                            "Third token of ; line wrong";

        simpleT = new SimpleCSVTokenizer("aa;");
        assert simpleT.nextToken().equals("aa"): 
                            "First token of aa; line wrong";
        assert simpleT.nextToken().length() == 0 : 
                            "First token of aa; line wrong";
        assert simpleT.nextToken() == null : 
                            "Third token of aa; line wrong";

        simpleT = new SimpleCSVTokenizer(";aa");
        assert simpleT.nextToken().length() == 0: 
                            "First token of ;aa line wrong";
        assert simpleT.nextToken().equals("aa"): 
                            "First token of ;aa line wrong";
        assert simpleT.nextToken() == null : 
                            "Third token of ;aa line wrong";
                            
        simpleT = new SimpleCSVTokenizer("aa;bb");
        assert simpleT.nextToken().equals("aa"): 
                            "First token of aa;bb line wrong";
        assert simpleT.nextToken().equals("bb"): 
                            "First token of aa;abb  line wrong";
        assert simpleT.nextToken() == null : 
                            "Third token of aa;abb  line wrong";

        simpleT = new SimpleCSVTokenizer("a;b;c;;e;");
        List theTokens = simpleT.tokenize();
        assertEquals(6, theTokens.size());
        assertEquals("a", theTokens.get(0));
        assertEquals("b", theTokens.get(1));
        assertEquals("c", theTokens.get(2));
        assertEquals("",  theTokens.get(3));
        assertEquals("e", theTokens.get(4));
        assertEquals("",  theTokens.get(5));
   }


    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestSimpleCSVTokenizer.class);
        // TestSuite suite = new TestCSVTokenizer("doDeleteFile")
        return BasicTestSetup.createBasicTestSetup(suite);
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
