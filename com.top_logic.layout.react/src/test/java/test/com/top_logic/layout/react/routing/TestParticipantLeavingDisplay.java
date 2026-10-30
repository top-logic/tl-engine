/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.routing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.tabbar.ReactTabBarControl;
import com.top_logic.layout.react.control.tabbar.TabDefinition;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that the address bar is corrected when a routed participant leaves the display, whichever
 * way the container showing it takes it away.
 *
 * <p>
 * A container exchanging its content - a {@code <switch>} changing its case, a slot losing its
 * contribution - detaches the content it drops and removes it from its state, in either order. A
 * participant detached while its container still holds it in its state has left the display all
 * the same: its segment must leave the URL with it, or the address bar keeps naming something no
 * longer shown, which the next reload silently drops.
 * </p>
 */
public class TestParticipantLeavingDisplay extends TestCase {

	/** The state key under which the {@link Holder} displays its content. */
	private static final String CONTENT = "content";

	/** The tab the tab bar displays. */
	private static final String DETAILS = "details";

	/** Another tab. */
	private static final String HISTORY = "history";

	/** The React module of the content a tab displays. */
	private static final String CONTENT_MODULE = "TestContent";

	/**
	 * A container displaying one control, which it can drop in either order of detaching it and
	 * removing it from its state.
	 */
	private static final class Holder extends ReactControl {

		private ReactControl _content;

		Holder(ReactContext context, ReactControl content) {
			super(context, null, "TestHolder");
			_content = content;
			putState(CONTENT, content);
		}

		/** Removes the content from the state first, then detaches it. */
		void dropAfterUpdate() {
			ReactControl content = _content;
			_content = null;
			putState(CONTENT, null);
			content.detach();
		}

		/** Detaches the content first, then removes it from the state. */
		void dropBeforeUpdate() {
			ReactControl content = _content;
			_content = null;
			content.detach();
			putState(CONTENT, null);
		}
	}

	private ReactContext _context;

	private RouteManager _routeManager;

	private Holder _holder;

	/** The URLs reported to the client, in the order reported. */
	private List<String> _reported;

	/** Whether each report replaces the URL rather than adding a history entry. */
	private List<Boolean> _replaced;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SSEUpdateQueue queue = new SSEUpdateQueue();
		_context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));
		_routeManager = _context.getRouteManager();
		queue.setRouteManager(_routeManager);
		_reported = new ArrayList<>();
		_replaced = new ArrayList<>();
		_routeManager.setUrlChangeHandler((url, title, replace) -> {
			_reported.add(url);
			_replaced.add(Boolean.valueOf(replace));
		});

		ReactTabBarControl tabs = new ReactTabBarControl(_context, null, List.of(
			new TabDefinition(DETAILS, DETAILS, this::content, null).withRoute(DETAILS),
			new TabDefinition(HISTORY, HISTORY, this::content, null).withRoute(HISTORY)), DETAILS);
		_holder = new Holder(_context, tabs);
		queue.setRootControl(_holder);
		_holder.attach();
		render();
		assertEquals(DETAILS, _routeManager.currentUrl());
		_reported.clear();
		_replaced.clear();
	}

	/**
	 * A tab bar dropped from the state before it is detached takes its segment out of the URL, as a
	 * replacement.
	 */
	public void testDroppedAfterStateUpdate() {
		_holder.dropAfterUpdate();
		assertDropped();
	}

	/**
	 * A tab bar detached while its container still holds it in its state takes its segment out of
	 * the URL as well.
	 */
	public void testDetachedBeforeStateUpdate() {
		_holder.dropBeforeUpdate();
		assertDropped();
	}

	private void assertDropped() {
		assertEquals("The segment of a tab bar no longer displayed stays in the URL.", "",
			_routeManager.currentUrl());
		assertEquals("The address bar is not corrected.", List.of(""), _reported);
		assertEquals("Leaving the display is no navigation of the user.", List.of(Boolean.TRUE), _replaced);
	}

	private ReactControl content() {
		return new ReactControl(_context, null, CONTENT_MODULE);
	}

	private void render() {
		try {
			_holder.write(new TagWriter());
		} catch (IOException ex) {
			throw new AssertionError("Rendering the page failed.", ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * <p>
	 * The page title is resolved by {@link com.top_logic.util.Resources}, which the services of the
	 * test setup provide.
	 * </p>
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestParticipantLeavingDisplay.class),
				ResourcesModule.Module.INSTANCE));
	}

}
