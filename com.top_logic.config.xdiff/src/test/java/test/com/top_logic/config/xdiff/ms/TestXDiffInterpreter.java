/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.ms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.AssertionFailedError;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.config.xdiff.compare.XDiff;
import com.top_logic.config.xdiff.model.Document;
import com.top_logic.config.xdiff.model.Node;
import com.top_logic.config.xdiff.ms.MSXApply;
import com.top_logic.config.xdiff.ms.MSXMatchDecision;
import com.top_logic.config.xdiff.ms.XDiffInterpreter;

/**
 * Test case for {@link XDiffInterpreter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestXDiffInterpreter extends AbstractMSXTestCase {

	private static final boolean RECREATE = false;

	public void test1() {
		doTest("test-1");
	}

	public void test2() {
		doTest("test-2");
	}

	public void test3() {
		doTest("test-3");
	}

	public void testUpdateTextNodes() {
		doTest("test-4");
	}

	public void testUpdateTextNodesAfterInsert() {
		doTest("test-4a");
	}

	public void testUpdateTextNodesAfterDelete() {
		doTest("test-4b");
	}

	public void test5() {
		doTest("test-5");
	}

	public void test6() {
		doTest("test-6");
	}

	public void testDeleteTextNode() {
		doTest("test-7");
	}

	public void testDeleteTextNodeAfterInsert() {
		doTest("test-7a");
	}

	public void test8() {
		doTest("test-8");
	}

	public void test9() {
		doTest("test-9");
	}

	public void testEntityReferences() {
		try {
			doTest("test-A");
		} catch (AssertionFailedError ex) {
			/* Exptected due to the known bug in ticket #17922: Serialization contains CDATA
			 * section. Diff algorithm breaks with such sections. */
			return;
		}

		fail(
			"Test should fail due to the known bug in ticket #17922: Serialization contains CDATA section. Diff algorithm breaks with such sections.");
	}

	public void testCData() {
		try {
			doTest("test-B");
		} catch (AssertionFailedError ex) {
			/* Exptected due to the known bug in ticket #17922: Serialization contains CDATA
			 * section. Diff algorithm breaks with such sections. */
			return;
		}

		fail(
			"Test should fail due to the known bug in ticket #17922: Serialization contains CDATA section. Diff algorithm breaks with such sections.");
	}

	public void testMultiplePrepend() {
		doTest("test-C");
	}

	public void testMultipleAppend() {
		doTest("test-D");
	}

	public void testMultiplePrependOtherElement() {
		doTest("test-E");
	}

	public void testMultipleAppendOtherElement() {
		doTest("test-F");
	}

	public void testMultipleInsertManyElements() {
		doTest("test-G");
	}

	public void testAttributeRemovalOnRootElement() {
		doTest("test-H");
	}

	public void testUnchangedCommentInUnchangedElement() {
		doTest("test-I");
	}

	public void testUnchangedCommentInChangedElement() {
		doTest("test-J");
	}

	public void testChangedCommentInChangedElement() {
		doTest("test-K");
	}

	public void testChangedCommentInUnchangedElement() {
		doTest("test-L");
	}

	private void doTest(String name) {
		Document beforeX = getBeforeX(name);
		Document expectedX = getExpectedX(name);
		Node xDiff = XDiff.diff(MSXMatchDecision.INSTANCE, beforeX, expectedX);

		org.w3c.dom.Document msDiff = XDiffInterpreter.transform(xDiff, name);

		boolean ok = false;
		try {
			recreateActual(name, msDiff);
			
			org.w3c.dom.Document before = getBefore(name);
			MSXApply.apply(msDiff, before);
			org.w3c.dom.Document expected = getExpected(name);

			assertEqualsDocument("Applied difference does not produce expected document",
				normalize(expected),
				normalize(before));

			checkAgainstFormerResult(name, msDiff);

			ok = true;
		} catch (IOException ex) {
			fail("Cannot dump result.");
		} finally {
			if (!ok) {
				try {
					System.out.println();
					System.out.println("Diff:");
					DOMUtil.serializeXMLDocument(System.out, true, msDiff);
				} catch (IOException ex) {
					// Ignore inner exception.
					Logger.error("Cannot dump diff.", ex, TestMSXApply.class);
				}
			}
		}
	}

	private org.w3c.dom.Document normalize(org.w3c.dom.Document document) {
		try {
			transformCData(document);
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			DOMUtil.serializeXMLDocument(buffer, true, document);
			InputSource source = new InputSource(new ByteArrayInputStream(buffer.toByteArray()));
			org.w3c.dom.Document result = DOMUtil.getDocumentBuilder().parse(source);
			return result;
		} catch (IOException ex) {
			throw new UnreachableAssertion(ex);
		} catch (SAXException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	/**
	 * Replaces CDATA nodes with regular text nodes.
	 */
	private org.w3c.dom.Node transformCData(org.w3c.dom.Node node) {
		org.w3c.dom.Node result;
		if (node.getNodeType() == org.w3c.dom.Node.CDATA_SECTION_NODE) {
			result = node.getOwnerDocument().createTextNode(node.getTextContent());
			org.w3c.dom.Node parent = node.getParentNode();
			parent.replaceChild(result, node);
		} else {
			result = node;
		}
		for (org.w3c.dom.Node child = result.getFirstChild(); child != null; child = child.getNextSibling()) {
			child = transformCData(child);
		}
		return result;
	}

	private void checkAgainstFormerResult(String name, org.w3c.dom.Document diff) {
		org.w3c.dom.Document expectedDiff = getDiff(name, "diff-actual");
		BasicTestCase.assertEqualsDocument(
			"Difference has changed, manual check required", expectedDiff, normalize(diff));
	}

	@SuppressWarnings("unused")
	private void recreateActual(String name, org.w3c.dom.Document diff) throws FileNotFoundException, IOException {
		// Enable to update the expected diffs.
		if (RECREATE) {
			FileOutputStream out = new FileOutputStream(getFile(name, "diff-actual"));
			DOMUtil.serializeXMLDocument(out, true, diff);
			out.close();
		}
	}

}
