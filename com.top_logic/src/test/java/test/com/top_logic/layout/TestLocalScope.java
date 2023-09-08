/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.util.Collection;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.layout.basic.SimpleControlScope;
import test.com.top_logic.layout.basic.SimpleFrameScope;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.DefaultUpdateQueue;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.LocalScope;
import com.top_logic.layout.basic.DummyDisplayContext;

/**
 * The class {@link TestLocalScope} tests {@link LocalScope}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLocalScope extends BasicTestCase {

	public void testRemoveUpdateListener() {
		DummyUpdateListener listener1 = new DummyUpdateListener();
		DummyUpdateListener listener2 = new DummyUpdateListener();

		LocalScope testedScope = new LocalScope(SimpleFrameScope.createSimpleFrameScope(null), false);
		testedScope.addUpdateListener(listener1);
		assertEquals("Listener1 was attached.", 1, listener1.numberOfListenedModels());
		testedScope.addUpdateListener(listener1);
		assertEquals("Listener1 was not attached twice.", 1, listener1.numberOfListenedModels());
		assertEquals("Listener2 was not attached.", 0, listener2.numberOfListenedModels());
		assertFalse(testedScope.removeUpdateListener(listener2));
		assertTrue(testedScope.removeUpdateListener(listener1));
		assertEquals("Listener1 was detached.", 0, listener1.numberOfListenedModels());
		assertFalse(testedScope.removeUpdateListener(listener1));
	}

	public void testClear() {
		SimpleControlScope simpleControlScope = new SimpleControlScope();
		String updateListenerFieldName = "lazyUpdateListener";

		assertTrue(CollectionUtil.isEmptyOrNull((Collection<?>) ReflectionUtils.getValue(simpleControlScope, updateListenerFieldName)));

		LocalScope testedScope = new LocalScope(SimpleFrameScope.createSimpleFrameScope(null), false);

		DummyUpdateListener listener2 = new DummyUpdateListener();
		testedScope.addUpdateListener(listener2);
		assertEquals("Listener2 was attached.", 1, listener2.numberOfListenedModels());
		testedScope.clear();
		assertEquals("Scope was cleared.", 0, listener2.numberOfListenedModels());

		assertFalse(testedScope.removeUpdateListener(listener2));

		assertTrue(CollectionUtil.isEmptyOrNull((Collection<?>) ReflectionUtils.getValue(simpleControlScope, updateListenerFieldName)));
	}

	public void testRevalidate() {
		LocalScope testedScope = new LocalScope(SimpleFrameScope.createSimpleFrameScope(null), false);

		DummyUpdateListener listener = new DummyUpdateListener();
		listener.setInvalid(false);

		testedScope.addUpdateListener(listener);
		assertFalse(testedScope.hasUpdates());
		listener.setInvalid(true);
		assertTrue(testedScope.hasUpdates());

		DefaultUpdateQueue actions = new DefaultUpdateQueue();
		testedScope.revalidate(DummyDisplayContext.newInstance(), actions);
		assertEquals(Collections.singletonList(listener.getRevalidationAction()), actions.getStorage());
		assertFalse(listener.isInvalid());
		assertFalse(testedScope.hasUpdates());
	}
	
	public void testGetFrameScope() {
		FrameScope frame = SimpleFrameScope.createSimpleFrameScope(null);
		LocalScope testedScope = new LocalScope(frame, false);
		
		assertSame(frame, testedScope.getFrameScope());
	}

	/**
	 * the suite of Tests to execute
	 */
	static public Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestLocalScope.class));
	}

	/**
	 * main function for direct testing.
	 */
	static public void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
