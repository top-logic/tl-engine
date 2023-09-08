/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.listener;


import junit.framework.TestCase;

import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.listener.ListenerRegistry;
import com.top_logic.basic.listener.ListenerRegistryFactory;

/**
 * Tests for {@link ListenerRegistry} that is thread safe but stores the {@link Listener}s directly.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestConcurrentListenerRegistry extends TestCase {

	/**
	 * Test for {@link ListenerRegistry#register(Listener)} and
	 * {@link ListenerRegistry#notify(Object)}. These cannot be tested separately.
	 */
	public void testRegisterAndNotifyListeners() {
		CommonTestListenerRegistry.testRegisterAndNotifyListeners(createListenerRegistry());
	}

	/** Test for {@link ListenerRegistry#unregister(Listener)} */
	public void testUnregister() {
		CommonTestListenerRegistry.testUnregister(createListenerRegistry());
	}

	private ListenerRegistry<Object> createListenerRegistry() {
		return ListenerRegistryFactory.getInstance().createConcurrent();
	}

}
