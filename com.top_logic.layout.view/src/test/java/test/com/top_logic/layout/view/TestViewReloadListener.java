/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewReloadListener;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests for {@link ViewReloadListener} registration and propagation through the
 * {@link ViewContext} hierarchy.
 */
public class TestViewReloadListener extends TestCase {

	/**
	 * Tests that a listener on a root context receives events fired on that context.
	 */
	public void testListenerOnRootContext() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		List<Set<String>> received = new ArrayList<>();
		root.addViewReloadListener(received::add);
		root.fireViewChanged(Set.of("/WEB-INF/views/app.view.xml"));
		assertEquals(1, received.size());
		assertTrue(received.get(0).contains("/WEB-INF/views/app.view.xml"));
	}

	/**
	 * Tests that a listener on a child context (created via the constructor) receives events fired
	 * on the root context.
	 */
	public void testListenerOnChildContextReceivesRootEvents() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		ViewContext child = new DefaultViewContext(root);
		List<Set<String>> received = new ArrayList<>();
		child.addViewReloadListener(received::add);
		root.fireViewChanged(Set.of("/WEB-INF/views/sidebar.view.xml"));
		assertEquals(1, received.size());
	}

	/**
	 * Tests that a listener on a derived child context (created via
	 * {@link ViewContext#childContext(String)}) receives events fired on the root.
	 */
	public void testListenerOnDerivedChildContext() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		ViewContext derived = root.childContext("panel");
		List<Set<String>> received = new ArrayList<>();
		derived.addViewReloadListener(received::add);
		root.fireViewChanged(Set.of("/WEB-INF/views/panel.view.xml"));
		assertEquals(1, received.size());
	}

	/**
	 * Tests that a removed listener no longer receives events.
	 */
	public void testRemoveListener() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		List<Set<String>> received = new ArrayList<>();
		ViewReloadListener listener = received::add;
		root.addViewReloadListener(listener);
		root.removeViewReloadListener(listener);
		root.fireViewChanged(Set.of("/WEB-INF/views/app.view.xml"));
		assertTrue(received.isEmpty());
	}

	/**
	 * Tests that a listener on a deeply nested context (grandchild) receives events fired on the
	 * root context.
	 */
	public void testDeeplyNestedContexts() {
		ViewContext root = new DefaultViewContext(new TestReactContext());
		ViewContext child = new DefaultViewContext(root);
		ViewContext grandchild = new DefaultViewContext(child);
		List<Set<String>> received = new ArrayList<>();
		grandchild.addViewReloadListener(received::add);
		root.fireViewChanged(Set.of("/WEB-INF/views/deep.view.xml"));
		assertEquals(1, received.size());
	}

	/**
	 * Minimal {@link ReactContext} stub for testing.
	 */
	private static class TestReactContext implements ReactContext {

		private int _nextId;

		@Override
		public String allocateId() {
			return "id-" + (_nextId++);
		}

		@Override
		public String getWindowName() {
			return "test-window";
		}

		@Override
		public String getContextPath() {
			return "/test";
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
	}
}
