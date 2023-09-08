/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.ms;

import junit.framework.AssertionFailedError;

import org.w3c.dom.Document;

import com.top_logic.config.xdiff.ms.MSXApply;
import com.top_logic.config.xdiff.ms.MSXApplyError;

/**
 * Test case for {@link MSXApply}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMSXApply extends AbstractMSXTestCase {

	public void test1() {
		doTestApply("test-1");
	}

	public void test2() {
		doTestApply("test-2");
	}

	public void test2a() {
		doTestApply("test-2", "diff-minimal");
	}

	public void test3() {
		doTestApply("test-3");
	}

	public void test4() {
		doTestApply("test-4");
	}

	public void test5() {
		doTestApply("test-5");
	}

	public void testAttributeDelete() {
		try {
			doTestApply("test-6", "diff-fail");
			fail("Error expected: Must not allow removing attribute nodes.");
		} catch (MSXApplyError ex) {
			// Expected.
		}
	}

	public void testTextDelete() {
		try {
			doTestApply("test-7", "diff-fail");
			fail("Error expected: Must not allow removing text nodes.");
		} catch (MSXApplyError ex) {
			// Expected.
		}
	}

	public void testElementDelete() {
		doTestApply("test-8");
	}

	public void testTextAdd() {
		try {
			doTestApply("test-9", "diff-fail");
			fail("Error expected: Must not allow adding text with NodeAdd.");
		} catch (MSXApplyError ex) {
			// Expected.
		}
	}

	public void testEntityReferences() {
		try {
			doTestApply("test-A");
		} catch (AssertionFailedError ex) {
			/* Exptected due to the known bug in ticket #18869: Unresolved entity in attribute value
			 * not allowed. */
			return;
		}

		fail(
			"Test should fail due to the known bug in ticket #18869: Unresolved entity in attribute value not allowed.");
	}

	public void testCData() {
		doTestApply("test-B");
	}

	public void testMultipleInsert() {
		doTestApply("test-C");
	}

	public void testMultipleAppend() {
		doTestApply("test-D");
	}

	/**
	 * Test that the parser does not coalesce character data sections to be able to detect the
	 * correct creation of CData sections in other tests.
	 */
	public void testCDataFail() {
		doTestApplyFail("test-B", "diff-fail");
	}

	public void doTestApply(String baseName) {
		doTestApply(baseName, "diff");
	}

	public void doTestApply(String baseName, String diffSuffix) {
		Document actual = doApply(baseName, diffSuffix);
		Document expected = getExpected(baseName);

		assertEqualsDocument("Applied difference does not produce expected document", expected, actual);
	}

	public void doTestApplyFail(String baseName, String diffSuffix) {
		Document actual = doApply(baseName, diffSuffix);
		Document expected = getExpected(baseName);

		assertNotEqualsDocument(expected, actual);
	}

	private Document doApply(String baseName, String diffSuffix) {
		Document before = getBefore(baseName);
		Document diff = getDiff(baseName, diffSuffix);
		MSXApply.apply(diff, before);
		return before;
	}

}
