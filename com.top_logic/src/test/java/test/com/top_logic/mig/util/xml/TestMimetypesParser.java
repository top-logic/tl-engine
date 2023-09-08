/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.util.xml;

import java.io.File;

import javax.activation.FileTypeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ConfigLoaderTestUtil;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.mime.MimetypesParser;


/**
 * Testcase for {@link com.top_logic.basic.mime.MimetypesParser}.
 * 
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestMimetypesParser extends TestCase {

	/**
	 * Constructor for TestMimetypesParser.
	 * 
	 * @param function name of fucntion to execute as test.
	 */
	public TestMimetypesParser(String function) {
		super(function);
	}
	
	/** Slurp in our Web.xml and check for some known Mimetypes. */
	public void testRead() throws Exception {
		Maybe<File> testFolder = ConfigLoaderTestUtil.getTestWebapp();
		assertTrue("No test configuration folder found", testFolder.hasValue());
		File webXml = new File(testFolder.get(), "WEB-INF/web.xml");
		assertTrue("No webXML found: " + webXml, webXml.exists());
		FileTypeMap fmap = MimetypesParser.parse(BinaryDataFactory.createBinaryData(webXml));
		
		assertEquals(BinaryData.CONTENT_TYPE_OCTET_STREAM, fmap.getContentType(""));
		assertEquals(BinaryData.CONTENT_TYPE_OCTET_STREAM, fmap.getContentType("some file name"));
		assertEquals("text/html",  		   fmap.getContentType(".htm"));
		assertEquals("text/plain", 		   fmap.getContentType("xxx.java"));
		assertEquals("text/javascript",    fmap.getContentType("balh.blub.js"));
        assertEquals("application/msword", fmap.getContentType("whatever.doc"));
	}
	

    /** Return the suite of tests to execute.
     */
    public static Test suite () {
        return new TestSuite (TestMimetypesParser.class);
    }

    /** main function for direct testing.
     */
    public static void main (String[] args) {

        junit.textui.TestRunner.run (suite ());
    }

}
