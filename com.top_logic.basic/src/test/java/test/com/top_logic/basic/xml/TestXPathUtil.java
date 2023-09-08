/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml;

import static com.top_logic.basic.xml.XPathUtil.*;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XPathUtil;

/**
 * Test case for {@link XPathUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestXPathUtil extends TestCase {

	public void testEvalBoolean() throws XPathExpressionException {
		assertTrue(evalBoolean(stringInput("<r><x/></r>"), "/r/x"));
		assertFalse(evalBoolean(stringInput("<r></r>"), "/r/x"));
	}

	public void testEvalInt() throws XPathExpressionException {
		assertEquals(2, matchCount(stringInput("<r><x/><x/></r>"), "/r/x"));
	}

	public void testEvalDouble() throws XPathExpressionException {
		assertEquals(13.42, evalDouble(stringInput("<r><x v='13.42'/></r>"), "/r/x/@v"));
	}

	public void testEvalString() throws XPathExpressionException {
		assertEquals("13.42", evalString(stringInput("<r><x v='13.42'/></r>"), "/r/x/@v"));
	}

	public void testEvalNode() throws XPathExpressionException {
		assertEquals("13.42", evalElement(stringInput("<r><x v='13.42'/></r>"), "/r/x").getAttribute("v"));
	}

	public void testEvalNodeList() throws XPathExpressionException {
		assertEquals(2, evalNodeList(stringInput("<r><x/><x/></r>"), "/r/x").getLength());
	}

	public void testFileEval() throws XPathExpressionException, IOException {
		String fileName = TestXPathUtil.class.getSimpleName() + ".xml";
		File testFile = BasicTestCase.createNamedTestFile(fileName);
		FileUtilities.writeStringToFile("<r><x/><x/></r>", testFile);
		assertEquals(2, matchCount(testFile, "/r/x"));
	}

	public void testNodeEval() throws XPathExpressionException {
		Document document = DOMUtil.parse("<r><x/><x/></r>");
		assertEquals(2, matchCount(document, "/r/x"));
	}

	public void testEmptyEval() throws XPathExpressionException {
		assertEquals(42, evalInt(null, "40 + 2"));
	}
}
