/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.basic.ObservableBase;

/**
 * Test the {@link ObservableBase} (using a non-standard pattern :-).
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestObservableBase extends TestCase {

	private LocalObservableBase observable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.observable = new LocalObservableBase();
	}

	@Override
	protected void tearDown() throws Exception {
		this.observable = null;
		super.tearDown();
	}

	public void testObservable() {
		Class<TestObservableBase> testClass = TestObservableBase.class;
		assertTrue(observable._getListeners(testClass).isEmpty());
		assertFalse(observable._hasListeners(testClass));
		assertFalse(observable._hasListener(testClass, this));

        assertTrue(observable._addListener(testClass, this));
		assertTrue(observable._getListeners(testClass).contains(this));
		assertTrue(observable._hasListeners(testClass));
		assertTrue(observable._hasListener(testClass, this));
            
        assertFalse(observable._addListener(testClass, this));
		assertTrue(observable._removeListener(testClass, this));
		assertFalse(observable._removeListener(testClass, this));
		assertFalse(observable._hasListeners(testClass));
		assertFalse(observable._hasListener(testClass, this));

        assertTrue(observable._getListeners(testClass).isEmpty());
		assertTrue(observable._getListeners(String.class).isEmpty());
    }

	public void testHasListeners() {
		// add listener of some class
		observable._addListener(TestObservableBase.class, this);

		// check listener of some different class
		try {
			assertFalse(observable._hasListeners(Object.class));
			assertFalse(observable._hasListener(Object.class, new Object()));
		} catch (NullPointerException npe) {
			throw BasicTestCase.fail("see Ticket #5093", npe);
		}
	}
    
    public static Test suite() {
        return new TestSuite(TestObservableBase.class);
    }

	private static class LocalObservableBase extends ObservableBase {

		LocalObservableBase() {
			// nothing to do here
		}

		final <T> boolean _hasListener(Class<T> listenerInterface, T someListener) {
			return super.hasListener(listenerInterface, someListener);
		}

		final <T> boolean _hasListeners(Class<T> listenerInterface) {
			return super.hasListeners(listenerInterface);
		}
		
		<T> Collection<T> _getListenersDirect(Class<T> listenerInterface) {
			return super.getListenersDirect(listenerInterface);
		}

		final <T> List<T> _getListeners(Class<T> listenerInterface) {
			return super.getListeners(listenerInterface);
		}

		final <T> boolean _addListener(Class<T> listenerInterface, T listener) {
			return super.addListener(listenerInterface, listener);
		}

		final <T> boolean _removeListener(Class<T> listenerInterface, T listener) {
			return super.removeListener(listenerInterface, listener);
		}
	}

}

