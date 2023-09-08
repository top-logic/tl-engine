/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.compare;

import static com.top_logic.config.xdiff.compare.XDiffSchemaConstants.*;
import static com.top_logic.config.xdiff.util.Utils.*;
import static test.com.top_logic.config.xdiff.TestingUtil.*;

import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.config.xdiff.apply.XApply;
import com.top_logic.config.xdiff.compare.XDiff;
import com.top_logic.config.xdiff.compare.XDiffSchemaConstants;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Node;

/**
 * Test case for the {@link XDiff} algorithm.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestXDiff extends TestCase {

	private static final String D = "xmlns:d='" + XDiffSchemaConstants.XDIFF_NS + "'";

	public void testInsertElement() throws XMLStreamException, FactoryConfigurationError {
		doTestDiff(
			"<a><c a1='v1'/></a>",
			"<a><c a1='v1'/><c a1='v2'/></a>",
			"<a><c a1='v1'/>" + insert("<c a1='v2'/>") + "</a>");
	}

	public void testUpdateAttribute() throws XMLStreamException, FactoryConfigurationError {
		doTestDiff(
			"<a><c a1='v2'/><c a1='v1'/></a>",
			"<a><c a1='v2'/><c a1='v3'/></a>",
			"<a><c a1='v2'/><c>" + attributes("a1='v1'", "a1='v3'") + "</c></a>");
	}

	public void testInsertAttribute() throws XMLStreamException, FactoryConfigurationError {
		doTestDiff(
			"<a><c a1='v1'/></a>",
			"<a><c a1='v1' a2='v2'/></a>",
			"<a><c a1='v1'>" + attributes(null, "a2='v2'") + "</c></a>");
	}

	public void testInsertAmbiguity() throws XMLStreamException, FactoryConfigurationError {
		doTestDiff(
			"<a><c a1='v1'/></a>",
			"<a><c a1='v1'/><c a1='v1'/></a>");
	}

	public void testChangeElement() throws XMLStreamException, FactoryConfigurationError {
		doTestDiff(
			"<a><c a1='v1'/><c a1='v1'/></a>",
			"<a><b a1='v1'/><c a1='v1'/></a>",
			"<a>" + delete("<c a1='v1'/>") + insert("<b a1='v1'/>") + "<c a1='v1'/></a>");
	}

	public void testRootChange() throws XMLStreamException, FactoryConfigurationError {
		doTestDiff(
			"<a><c a1='v1'/><c a1='v1'/></a>",
			"<b><c a1='v1'/><c a1='v1'/></b>",
			root(delete("<a><c a1='v1'/><c a1='v1'/></a>") + insert("<b><c a1='v1'/><c a1='v1'/></b>")));
	}

	private void doTestDiff(String xml1, String xml2, String... expectedDiffs) throws XMLStreamException {
		checkDiff(xml1, xml2);
		checkDiff(xml2, xml1);

		checkExpected(xml1, xml2, expectedDiffs);
	}

	private void checkExpected(String xml1, String xml2, String... expectedDiffs) throws XMLStreamException {
		List<String> expectedList = list(expectedDiffs);
		if (expectedList.isEmpty()) {
			return;
		}

		Node diff = diff(fixture(xml1), fixture(xml2));
		diff.init();

		for (String expectedDiff : expectedList) {
			if (fixture(expectedDiff).equals(diff)) {
				return;
			}
		}

		fail("Unexpected difference: " + diff + ", expected one of " + expectedList);
	}

	/**
	 * Test, whether the diff is technically correct (it applies to its target during apply and to
	 * its source during reverse apply).
	 */
	private void checkDiff(String xml1, String xml2) throws XMLStreamException, FactoryConfigurationError {
		assertEquals(fixture(xml2), apply(diff(fixture(xml1), fixture(xml2)), false));
		assertEquals(fixture(xml1), apply(diff(fixture(xml1), fixture(xml2)), true));
	}

	private static Node diff(Document d1, Document d2) {
		return XDiff.diff(d1, d2);
	}

	private static Document apply(Node diff, boolean reverse) throws XMLStreamException {
		String xml = diff.toString();
		org.w3c.dom.Document applied;
		try {
			applied = DOMUtil.parse(xml);
		} catch (IllegalArgumentException ex) {
			Logger.error("Invalid XML: " + xml, TestXDiff.class);
			throw ex;
		}
		new XApply(reverse).apply(applied);
		return fixture(DOMUtil.toString(applied));
	}

	private static String root(String xml) {
		return "<d:" + DIFF_ELEMENT + " " + D + ">" + xml + "</d:" + DIFF_ELEMENT + ">";
	}

	private static String insert(String xml) {
		return "<d:" + INSERT_ELEMENT + " " + D + ">" + xml + "</d:" + INSERT_ELEMENT + ">";
	}

	private static String delete(String xml) {
		return "<d:" + DELETE_ELEMENT + " " + D + ">" + xml + "</d:" + DELETE_ELEMENT + ">";
	}

	private static String attributes(String oldAttr, String newAttr) {
		return ""
			+ "<d:" + ATTRIBUTES_ELEMENT + " " + D + ">"
			+ (oldAttr != null ? "<d:" + REMOVE_ELEMENT + " " + oldAttr + "/>" : "")
			+ (newAttr != null ? "<d:" + ADD_ELEMENT + " " + newAttr + "/>" : "")
			+ "</d:" + ATTRIBUTES_ELEMENT + ">";
	}

}
