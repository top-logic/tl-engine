/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import java.io.StringReader;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.CSVReader;

/**
 * The TestCSVReader tests the CSVReader class.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class TestCSVReader extends BasicTestCase {

    /**
     * Creates a new instance of this class.
     */
    public TestCSVReader (String name) {
        super (name);
    }

    /**
     * Return the suite of tests to execute.
     */
    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestCSVReader.class));
    }

    /**
     * Main function for direct testing.
     */
    public static void main (String[] args) {
        TestRunner.run(suite());
    }


    public void testMain() throws Exception {
//        List line = null;
        String content = "";
        CSVReader csv = new CSVReader(new StringReader(content));
        /* line = */ csv.readLine();
//        assertNull("Empty line should be null.", line);
        assert true;
    }

    /*
     * String to test:
     * ""                            --> 0 lines ??? 1 line: 1 token ("")
     * "\n"                          --> 1 line: 1 token ("")
     * ","                           --> 1 line: 2 tokens ("", "")
     * "a,b"                         --> 1 line: 2 tokens ("a", "b")
     * "a,b,"                        --> 1 line: 3 tokens ("a", "b", "")
     * "a,b\n"                       --> 1 line: 2 tokens ("a", "b")
     * "a,b,\n"                      --> 1 line: 3 tokens ("a", "b", "")
     *
     */

//  if (c == -1)
//  "aaa","bbb","ccc" CRLF
//  zzz,yyy,xxx
//
//
//  "aaa","b CRLF
//  bb","ccc" CRLF
//  zzz,yyy,xxx




}
