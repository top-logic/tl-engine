/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;

import com.top_logic.basic.col.BooleanComparatorNullAsFalse;

/**
 * Test case for {@link BooleanComparatorNullAsFalse}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBooleanComparatorNullAsFalse extends TestCase {
	
	public void testIt() {
		doTest(0, null, null);
		doTest(0, null, false);
		doTest(-1, null, true);
		doTest(0, false, false);
		doTest(-1, false, true);
		doTest(0, true, true);
	}

	private void doTest(int expected, Boolean o1, Boolean o2) {
		assertEquals(expected, BooleanComparatorNullAsFalse.INSTANCE.compare(o1, o2));
		assertEquals(-expected, BooleanComparatorNullAsFalse.INSTANCE.compare(o2, o1));
		assertEquals(-expected, BooleanComparatorNullAsFalse.DESCENDING_INSTANCE.compare(o1, o2));
		assertEquals(expected, BooleanComparatorNullAsFalse.DESCENDING_INSTANCE.compare(o2, o1));
	}

}
