/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;

/**
 * Testcase for {@link com.top_logic.mig.html.HTMLUtil}.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestHTMLUtil extends BasicTestCase {

	/**
	 * Constructor 
	 * 
	 * @param name name of the fucntion to exexute as Test
	 */
	public TestHTMLUtil(String name) {
		super(name);
	}

	/** Test for {@link com.top_logic.mig.html.HTMLUtil#extractTitle} */
	public void testExtractTitle() {
		
		String 		 str;
		StringReader reader;
		
		str 	= "This no title";
		reader  = new StringReader(str);
		assertNull(HTMLUtil.extractTitle(reader));

        // Since reader was closed this must fail ..
        try {
            assertTrue(reader.ready());
            fail("Reader should be close()d");
        } catch (IOException expected) { /* expected */ }

		str 	= "This is still no <title>";
		reader  = new StringReader(str);
		assertNull(HTMLUtil.extractTitle(reader));

		str 	= "<title>a title</title>";
		reader  = new StringReader(str);
		assertEquals("a title", HTMLUtil.extractTitle(reader));

		str 	= "<TITLE>a TITLE</TITLE>";
		reader  = new StringReader(str);
		assertEquals("a TITLE",HTMLUtil.extractTitle(reader));

		str 	= "<title>\n		a title		\n	</title>";
		reader  = new StringReader(str);
		assertEquals("a title", HTMLUtil.extractTitle(reader));
	}

	/** Recursivly extract all Titles in a given Directory.
	 * 
	 * @return number of files found.
	 */
	public int extractTitles(File aFile) throws IOException {

		int result = 0;
        if (aFile.isDirectory()) {
            File subs[] = aFile.listFiles();
            int size = subs.length;
            for (int i=0; i < size; i++)
                result += extractTitles(subs[i]);
        }
        else if (aFile.isFile()) {
        	HTMLUtil.extractTitle(new FileReader(aFile));
	        result = 1; // one file processes
        }
        return result;	
	}

	/** Measure time needed for
	 *  {@link com.top_logic.mig.html.HTMLUtil#extractTitle} 
	 * 
	 * This requires a directory full og HMTL Files an therefore
	 * is <em>not</em> part of the regular TestCases.
	 */
	public void timeExtractTitle() throws IOException {

		File aDir = new File("E:\\ct99");		
		startTime();
		int result = extractTitles(aDir);
		logTime("Time for extracting titles from " + result + " files");
	}
    
    
    /** Help Testing <code>writeBRForNewline</code>. */
    protected static String helpBRForNewline(String in) throws IOException {
        StringWriter out=new StringWriter(128);
        HTMLUtil.writeBRforNewline(in, out);
        return out.toString();
    }
    
    /** Testcase for <code>writeBRForNewline</code>. */
    public void testBRForNewline() throws IOException {
        assertEquals(""         , helpBRForNewline(""));
        assertEquals("abcdefg"  , helpBRForNewline("abcdefg"));
        assertEquals("<br />"             , helpBRForNewline("\n" ));
        assertEquals(""             , helpBRForNewline("\r" ));
        assertEquals("<br />"             , helpBRForNewline("\n\r" ));
        assertEquals("<br />"             , helpBRForNewline("\r\n"));
        assertEquals("<br /><br />"     , helpBRForNewline("\n\n" ));
        //assertEquals(""     , helpBRForNewline("\r\r" ));
        assertEquals("a<br />b"   , helpBRForNewline("a\nb\r" ));
    }

    /** Testcase for <code>encodeJS</code>. */
    public void testEncodeJS() {
        assertEquals("", HTMLUtil.encodeJS(null));
        assertEquals("", HTMLUtil.encodeJS(""));
        assertEquals("Da lag da cadava aba", HTMLUtil.encodeJS("Da lag da cadava aba"));
        //Text: "That's the way it is", said Adam \ Eve   -->   \"That\'s the way it is\", said Adam \\ Eve
        assertEquals("\\\"That\\'s the way it is\\\", said Adam \\\\ Eve",
                    HTMLUtil.encodeJS("\"That's the way it is\", said Adam \\ Eve"));
    }

    /** 
     * Test the method parseURLparams.
     * 
     * TODO KHA/MGA/DKO this is quite incomplete ...
     */
     public void testparseURLParams() {
         String str       = "COMPANY.COMPANYCODEID=S300";
         String outerAttr = "COMPANY";
         String name      = "COMPANYCODEID";
         String p3Value   = "S300";
         Map testTable  = HTMLUtil.parseURLParams(str);
         Map innerTable = (Map) testTable.get(outerAttr);
         assertEquals(p3Value, innerTable.get(name));

     }

	/**
	 * Test creation of some variants of select/option tags.
	 */
	public void testSelect() throws Exception {

		TagWriter htw = new TagWriter();
		HTMLUtil.beginForm(htw);
		HTMLUtil.beginSelect(htw, "name");
		HTMLUtil.beginOption(htw, "value");
		HTMLUtil.endOption(htw);
		HTMLUtil.endSelect(htw);
		HTMLUtil.endForm(htw);

		String expected = "<form method=\"post\">"
			+ "<select name=\"name\">"
			+ "<option value=\"value\">"
			+ "</option>"
			+ "</select>"
			+ "</form>";
		/*System.out.println("--- expected ---"); System.out.println(expected);
		 * System.out.println("--- seen -------"); System.out.println(htw.toString());
		 * System.out.println("----------------"); */
		assertEquals(expected, htw.toString());

		htw = new TagWriter();
		HTMLUtil.beginForm(htw);
		HTMLUtil.beginSelect(htw, "name", "lila");
		HTMLUtil.writeOption(htw, "value", "value");
		HTMLUtil.endSelect(htw);
		HTMLUtil.endForm(htw);
		assertEquals("<form method=\"post\">"
			+ "<select name=\"name\" class=\"lila\">"
			+ "<option value=\"value\">value</option>"
			+ "</select>"
			+ "</form>",
			htw.toString());

		htw = new TagWriter();
		HTMLUtil.beginForm(htw);
		HTMLUtil.beginSelect(htw, "name", 5);
		HTMLUtil.writeOption(htw, "value", "value");
		HTMLUtil.writeOption(htw, 17, "SiebZehn", 17);
		HTMLUtil.writeOption(htw, "value", "value", HTMLUtil.SELECTED);
		HTMLUtil.endSelect(htw);
		HTMLUtil.endForm(htw);
		assertEquals("<form method=\"post\">"
			+ "<select name=\"name\" size=\"5\">"
			+ "<option value=\"value\">value</option>"
			+ "<option value=\"17\" selected=\"selected\">SiebZehn</option>"
			+ "<option value=\"value\" selected=\"selected\">value</option>"
			+ "</select>"
			+ "</form>",
			htw.toString());

		htw = new TagWriter();
		HTMLUtil.beginForm(htw);
		HTMLUtil.beginSelect(htw, "name", "class", 7, HTMLUtil.MULTI_SELECT);
		HTMLUtil.endSelect(htw);
		HTMLUtil.endForm(htw);
		assertEquals("<form method=\"post\">"
			+ "<select name=\"name\" class=\"class\" size=\"7\" multiple=\"multiple\">"
			+ "</select>"
			+ "</form>",
			htw.toString());
	}

	/**
	 * Test writing of some complex Table.
	 */
	public void testTable() throws Exception {

		TagWriter htw = new TagWriter();
		HTMLUtil.beginTable(htw, "A Summary");
		HTMLUtil.beginTr(htw);
		HTMLUtil.beginTd(htw);
		HTMLUtil.beginTable(htw, "A Summary");
		HTMLUtil.beginTr(htw);
		HTMLUtil.beginTh(htw);
		HTMLUtil.beginTable(htw, "A Summary");
		HTMLUtil.beginTr(htw);
		HTMLUtil.beginBeginTh(htw);
		htw.writeAttribute(HTMLConstants.CLASS_ATTR, "inner");
		HTMLUtil.endBeginTh(htw);
		HTMLUtil.beginTable(htw, "A Summary");
		HTMLUtil.beginTr(htw);
		HTMLUtil.writeEmptyTd(htw);
		// Middle of 4 times nested table :-)
		HTMLUtil.endTr(htw);
		HTMLUtil.endTable(htw);
		HTMLUtil.endTh(htw);
		HTMLUtil.endTr(htw);
		HTMLUtil.endTable(htw);
		HTMLUtil.endTh(htw);
		HTMLUtil.endTr(htw);
		HTMLUtil.endTable(htw);
		HTMLUtil.endTd(htw);
		HTMLUtil.endTr(htw);
		HTMLUtil.endTable(htw);

		HTMLUtil.beginTable(htw, "With Class and Border", "borderClass", 77);
		HTMLUtil.endTable(htw);

		HTMLUtil.beginTable(htw, "All Zero", null, 0, 0, 0);
		HTMLUtil.endTable(htw);

		HTMLUtil.beginTable(htw, "All Zero", null, 0, 0, 0);
		HTMLUtil.endTable(htw);

		htw.close();
		// System.out.println(sout.toString());
	}

	/**
	 * Test writing of some simple tags.
	 */
	public void testSimple() throws Exception {

		TagWriter htw = new TagWriter();
		HTMLUtil.writeJavascriptRef(htw, "", "script/something.js");
		HTMLUtil.writeStylesheetRef(htw, "", "styles/someStyle.css");
		htw.writeComment("Written by " + TestHTMLUtil.class.getName());
		htw.close();

		// System.out.println(htw.toString());
	}

	/**
	 * Test writing of Anchors.
	 */
	public void testA() throws Exception {

		TagWriter htw = new TagWriter();
		HTMLUtil.beginA(htw, "http://www.blah.blurg");
		htw.getPrinter().write("Look at Blah");
		HTMLUtil.endA(htw);

		HTMLUtil.beginA(htw, "http://www.bliek.dot", "anchor");
		htw.getPrinter().write("Anchor at Bliek");
		HTMLUtil.endA(htw);

		// System.out.println(htw.toString());
	}

	/**
	 * Test writing of form related objects.
	 */
	public void testForm() throws Exception {

		TagWriter htw = new TagWriter();
		HTMLUtil.beginForm(htw);
		HTMLUtil.writeHiddenInput(htw, "hidden", "not so secret");
		HTMLUtil.writeSubmit(htw, "ok", "[OK]");
		HTMLUtil.endForm(htw);

		HTMLUtil.beginForm(htw, "FormName");
		HTMLUtil.writeInput(htw, "text", "style", "default", 32, 255);
		HTMLUtil.writeInput(htw, "short", "default", 16, 16);
		HTMLUtil.endForm(htw);

		HTMLUtil.beginForm(htw, "anotherName", "/servlet/ActionSerlvet");
		HTMLUtil.endForm(htw);

		HTMLUtil.beginForm(htw, "hicks", "/servlet/ActionSerlvet", "PROST");
		HTMLUtil.endForm(htw);

		// System.out.println(htw.toString());
	}

	public void testUseOnloadHandlerAtPageReloadForAfterRenderingFunctions() throws Exception {
		StringWriter outputBuffer = new StringWriter();
		TagWriter tagWriter = new TagWriter(outputBuffer);
		tagWriter.beginTag("html");
		tagWriter.beginTag("body");
		HTMLUtil.writeScriptAfterRendering(tagWriter, "var test = 'foo';");
		tagWriter.endTag("body");
		tagWriter.endTag("html");

		tagWriter.flush();

		String callOnloadFunctionPattern =
			"BAL\\s*\\.\\s*addEventListener\\s*\\(\\s*window\\s*,\\s*\\'load\\'\\s*\\,\\s*function\\s*\\(\\)\\s*\\{";
		assertJSFunctionCall(outputBuffer, callOnloadFunctionPattern);
	}

	private void assertJSFunctionCall(StringWriter outputBuffer, String jsFunctionPattern) {
		Pattern onloadHandlerPattern = Pattern.compile(jsFunctionPattern);
		String output = outputBuffer.getBuffer().toString();
		assertTrue(onloadHandlerPattern.matcher(output).find());
	}

	public void testUseUpdateQueueAtAjaxUpdateForAfterRenderingFunctions() throws Exception {
		StringWriter outputBuffer = new StringWriter();
		TagWriter tagWriter = new TagWriter(outputBuffer);
		tagWriter.beginTag("div");
		HTMLUtil.writeScriptAfterRendering(tagWriter, "var test = 'foo';");
		tagWriter.endTag("div");

		tagWriter.flush();

		String callAfterRenderingFunctionPattern =
			"services\\s*\\.\\s*ajax\\s*\\.\\s*executeAfterRendering\\s*\\(\\s*window\\s*\\,\\s*function\\s*\\(\\)\\s*\\{";
		assertJSFunctionCall(outputBuffer, callAfterRenderingFunctionPattern);
	}

     /**
	 * the suite of Tests to execute 
	 */
	static public Test suite () {
	    return TLTestSetup.createTLTestSetup(TestHTMLUtil.class);
	    // return new TestHTMLUtil("testBRForNewline");
	}


	/**
	 * main function for direct testing.
	 */
    static public void main (String[] argv) {

    	SHOW_TIME = true;	// show elapsed time 
    	
        junit.textui.TestRunner.run (suite ());
    }


}
