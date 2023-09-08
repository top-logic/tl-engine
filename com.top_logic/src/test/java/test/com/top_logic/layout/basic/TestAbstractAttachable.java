/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.basic.AbstractAttachable;

/**
 * The class {@link TestAbstractAttachable} tests the methods in {@link AbstractAttachable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestAbstractAttachable extends BasicTestCase {

	/*package protected*/ static class TestedAbstractAttachable extends AbstractAttachable {

		private int attachedCount = 0;

		@Override
		protected void internalAttach() {
			attachedCount++;
		}

		@Override
		protected void internalDetach() {
			attachedCount--;
		}
		
		public void overriddenCheckAttached() {
			checkAttached();
		}
		
	}

	public void testAttach() {
		TestedAbstractAttachable testedObj = new TestedAbstractAttachable();
		boolean result = testedObj.attach();
		assertTrue(result);
		assertTrue(testedObj.isAttached());
		assertEquals(1, testedObj.attachedCount);

		result = testedObj.attach();
		assertFalse(result);
		assertTrue(testedObj.isAttached());
		assertEquals(1, testedObj.attachedCount);
	}
	
	public void testCheckAttached() {
		TestedAbstractAttachable testedObj = new TestedAbstractAttachable();

		testedObj.attach();
		testedObj.overriddenCheckAttached();

		testedObj.detach();
		try {
			testedObj.overriddenCheckAttached();
			fail();
		} catch (IllegalStateException er) {
			//must happen
		} catch (Exception ex) {
			throw new AssertionError("Thrown exception is not IllegalStateException!");
		}

		testedObj.attach();
		testedObj.overriddenCheckAttached();
	}
	
	public void testDetach() {
		TestedAbstractAttachable testedObj = new TestedAbstractAttachable();
		boolean result = testedObj.detach();
		assertFalse(result);
		assertFalse(testedObj.isAttached());
		assertEquals(0, testedObj.attachedCount);
		
		result = testedObj.attach();
		assertTrue(result);
		assertTrue(testedObj.isAttached());
		assertEquals(1, testedObj.attachedCount);
		
		result = testedObj.detach();
		assertTrue(result);
		assertFalse(testedObj.isAttached());
		assertEquals(0, testedObj.attachedCount);
		
		result = testedObj.detach();
		assertFalse(result);
		assertFalse(testedObj.isAttached());
		assertEquals(0, testedObj.attachedCount);
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestAbstractAttachable.class);
	}
}
