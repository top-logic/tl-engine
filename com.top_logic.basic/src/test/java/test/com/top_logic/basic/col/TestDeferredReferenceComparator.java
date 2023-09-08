/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.DeferredReference;
import com.top_logic.basic.col.DeferredReferenceComparator;

/**
 * Tests {@link DeferredReferenceComparator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDeferredReferenceComparator extends BasicTestCase {

	public void testIt() {
		DeferredReferenceComparator referenceComparator = 
			new DeferredReferenceComparator(new Comparator() {
				@Override
				public int compare(Object o1, Object o2) {
					return ((String) o1).compareTo((String) o2);
				}
			});
		
		assertTrue(referenceComparator.compare("a", "b") == -1);
		assertTrue(referenceComparator.compare(new DeferredString("a"), "b") == -1);
		assertTrue(referenceComparator.compare("a", new DeferredString("b")) == -1);
		assertTrue(referenceComparator.compare(new DeferredString("a"), new DeferredString("b")) == -1);

		assertTrue(referenceComparator.compare("c", "b") == 1);
		assertTrue(referenceComparator.compare(new DeferredString("c"), "b") == 1);
		assertTrue(referenceComparator.compare("c", new DeferredString("b")) == 1);
		assertTrue(referenceComparator.compare(new DeferredString("c"), new DeferredString("b")) == 1);

	}
	
	private class DeferredString implements DeferredReference {

		private Object value;

		public DeferredString(Object value) {
			this.value = value;
		}

		@Override
		public Object get() {
			return value;
		}
	}

    public static Test suite () {
        return BasicTestSetup.createBasicTestSetup(new TestSuite (TestDeferredReferenceComparator.class));
    }

    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
