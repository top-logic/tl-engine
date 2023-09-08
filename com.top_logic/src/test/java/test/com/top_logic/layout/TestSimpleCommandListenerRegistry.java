/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.util.Map;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.layout.basic.SimpleControlScope;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CommandListenerRegistry;
import com.top_logic.layout.SimpleCommandListenerRegistry;

/**
 * The class {@link TestSimpleCommandListenerRegistry} tests the
 * {@link SimpleCommandListenerRegistry}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSimpleCommandListenerRegistry extends BasicTestCase {

	public void testRemoveCommandListener() {
		CommandListener listener1 = new DummyCommandListener("listener1", null);
		CommandListener listener2 = new DummyCommandListener("listener2", null);

		CommandListenerRegistry registry = new SimpleCommandListenerRegistry();
		registry.addCommandListener(listener1);
		assertFalse(registry.removeCommandListener(listener2));
		assertTrue(registry.removeCommandListener(listener1));
		assertFalse(registry.removeCommandListener(listener1));
	}

	public void testClear() {
		SimpleControlScope simpleControlScope = new SimpleControlScope();
		String commandListenerFieldName = "lazyCommandListener";

		assertTrue(CollectionUtil.isEmptyOrNull((Map<?, ?>) ReflectionUtils.getValue(simpleControlScope, commandListenerFieldName)));

		SimpleCommandListenerRegistry testedScope = new SimpleCommandListenerRegistry();

		CommandListener listener1 = new DummyCommandListener("listener1", null);
		testedScope.addCommandListener(listener1);
		testedScope.clear();

		assertFalse(testedScope.removeCommandListener(listener1));

		assertTrue(CollectionUtil.isEmptyOrNull((Map<?, ?>) ReflectionUtils.getValue(simpleControlScope, commandListenerFieldName)));
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestSimpleCommandListenerRegistry.class);
	}
}
