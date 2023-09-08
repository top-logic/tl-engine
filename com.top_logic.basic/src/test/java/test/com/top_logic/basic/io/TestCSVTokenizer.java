/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.CSVTokenizer;

/**
 * Test class to check the {@link CSVTokenizer}
 *
 * @author  <a href=mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestCSVTokenizer extends BasicTestCase {

    /**
     * Constructor to conduct a special test.
     *
     * @param name name of the test to execute.
     */
    public TestCSVTokenizer (String name) {
        super (name);
    }

    // Test methodes

    /** 
     * Main and single testcase here.
     */
    public void testMain () throws Exception
    {
        String line;
        CSVTokenizer csvT;

        line = "";
		csvT = new CSVTokenizer(line, '\"');
		doTest(csvT, line, new String[] {""});

		line = ";";
		csvT = new CSVTokenizer(line, '\"');
		doTest(csvT, line, new String[] {"", ""});

		line = "aa;";
		csvT = new CSVTokenizer(line,'\"');
		doTest(csvT, line, new String[] {"aa", ""});

		line = ";aa";
		csvT = new CSVTokenizer(line,'\"');
		doTest(csvT, line, new String[] {"", "aa"});

		line = "aa;bb";
        csvT = new CSVTokenizer(line,'\"');
		doTest(csvT, line, new String[] {"aa", "bb"});

		line = "\"\"";
        csvT = new CSVTokenizer(line,'\"');
		doTest(csvT, line, new String[] {""});

		line = "\"aa\"";
        csvT = new CSVTokenizer(line,'\"');
		doTest(csvT, line, new String[] {"aa"});

		line = "\"aa\";\"bb\"";
        csvT = new CSVTokenizer(line,'\"');
		doTest(csvT, line, new String[] {"aa", "bb"});

		line = "\"aa\";\"b\\\"b\"";
        csvT = new CSVTokenizer(line,'"');
		doTest(csvT, line, new String[] {"aa", "b\"b"});
    }

    public void testEscapeStart() throws Exception {
    	String line = "\"\\u0041AA\""; // \\ u0041 = 'A' ;-)
		CSVTokenizer csvT = new CSVTokenizer(line, /* sep */' ', /* quot */ '"');
		String[] expectedTokens = new String[] {
				"AAA"
		};
		doTest(csvT, line, expectedTokens);
    }

    public void testEscapeEnd() throws Exception {
    	String line = "\"AA\\u0041\"";
		CSVTokenizer csvT = new CSVTokenizer(line, /*sep=*/' ', /*quot=*/'"');
		String[] expectedTokens = new String[] {
				"AAA"
		};
		doTest(csvT, line, expectedTokens);
    }

    public void testEscape() throws Exception {
    	String line = "A\\u0041A \"A\\u0041A\" \"A\\b\\t\\n\\f\\r\\\"\\'\\u0041\" \"abc\\u0020cba\"";
		CSVTokenizer csvT = new CSVTokenizer(line, /*sep=*/' ', /*quot=*/'"');
		String[] expectedTokens = new String[] {
				"A\\u0041A", "AAA", "A\b\t\n\f\r\"\'A", "abc cba" 
		};
		doTest(csvT, line, expectedTokens);
    }

    public void testInvalidEscape() throws Exception {
    	String line = "\"ABC\\DCBA\"";
		CSVTokenizer csvT = new CSVTokenizer(line, /* sep */' ', /* quot */'"');
		// bhu: CSVTokenizer announces to "ignore" "invalid" escapes and output 
		// the escape character to the created token. This is not common, but
		// a matter of specification of the parsed format.
		String[] expectedTokens = new String[] {
				"ABC\\DCBA" 
		};
		doTest(csvT, line, expectedTokens);
    }

    public void testCustomQuotation() throws Exception {
    	String line = "*ab c* def *gh\\*i*";
		CSVTokenizer csvT = new CSVTokenizer(line, /* sep */ ' ', /* quot */ '*');
		String[] expectedTokens = new String[] {
				"ab c", "def", "gh\\*i"
		};
		doTest(csvT, line, expectedTokens);
    }
    
    public void testAdvancedCustomQuotation() throws Exception {
       	String line = "* \\*jkl\\* * * mn\\* \\*o *";
		CSVTokenizer csvT = new CSVTokenizer(line, /* sep */ ' ', /* quot */ '*');
		String[] expectedTokens = new String[] {
				" \\*jkl\\* ", " ", "mn\\*", "\\*o", ""
		};
		doTest(csvT, line, expectedTokens);
    }

    /** 
     * Check all tokens from the given tokenizer against the array of expected tokens. 
     * 
     * @param csvT the tokenizer to test.
     * @param line the textual contents of the tokenizer to be parsed.
     * @param expectedTokens an array of expected tokens.
     */
    private void doTest(CSVTokenizer csvT, String line, String[] expectedTokens) {
    	for (int n = 0; n < expectedTokens.length; n++) {
    		String token = csvT.nextToken();
    		assertEquals(
    				"Token No. " + n + " of '" + line + "' is wrong",
    				expectedTokens[n], token);
    	}
		String token = csvT.nextToken();
    	assertNull(
    			"No more tokens expected. Got: '" + token + "'",
    			token);
	}

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        TestSuite suite = new TestSuite (TestCSVTokenizer.class);
        // TestSuite suite = new TestCSVTokenizer("doDeleteFile")
        return BasicTestSetup.createBasicTestSetup(suite);
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
