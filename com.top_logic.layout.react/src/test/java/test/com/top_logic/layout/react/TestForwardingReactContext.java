/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests for {@link ForwardingReactContext}.
 */
public class TestForwardingReactContext extends TestCase {

	/**
	 * A minimal {@link ReactContext} whose accessors return distinguishable marker values.
	 */
	static class StubContext implements ReactContext {

		final RouteManager _routeManager = new RouteManager();

		@Override
		public String allocateId() {
			return "id";
		}

		@Override
		public String getWindowName() {
			return "window";
		}

		@Override
		public String getContextPath() {
			return "/ctx";
		}

		@Override
		public SSEUpdateQueue getSSEQueue() {
			return null;
		}

		@Override
		public ReactWindowRegistry getWindowRegistry() {
			return null;
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}

		@Override
		public RouteManager getRouteManager() {
			return _routeManager;
		}
	}

	/**
	 * {@link ForwardingReactContext} must override every method of {@link ReactContext}.
	 *
	 * <p>
	 * A method inherited from the interface — in particular a {@code default} method — silently
	 * detaches the wrapped context from its delegate: the wrapper answers with the interface
	 * default instead of the delegate's value. Services reachable only through such a method
	 * (e.g. the {@link RouteManager} behind {@link ReactContext#getRouteManager()}) then vanish
	 * for every control created under a wrapped context.
	 * </p>
	 */
	public void testForwardsEveryContextMethod() throws NoSuchMethodException {
		for (Method contextMethod : ReactContext.class.getMethods()) {
			Method implementation = ForwardingReactContext.class
				.getMethod(contextMethod.getName(), contextMethod.getParameterTypes());
			assertEquals(
				"ForwardingReactContext must forward ReactContext#" + contextMethod.getName()
					+ "() to its delegate instead of inheriting the interface default.",
				ForwardingReactContext.class, implementation.getDeclaringClass());
		}
	}

	/**
	 * {@link ForwardingReactContext#getRouteManager()} answers the delegate's
	 * {@link RouteManager}, keeping routing intact under wrapped contexts.
	 */
	public void testForwardsRouteManager() {
		StubContext delegate = new StubContext();
		ForwardingReactContext wrapper = new ForwardingReactContext(delegate);
		assertSame(delegate._routeManager, wrapper.getRouteManager());
	}
}
