/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.view.navigation.FixtureViews;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.WindowState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.element.WindowElement;

/**
 * Tests the size options of {@link WindowElement}: {@link WindowElement.Config#getWidth()} and
 * {@link WindowElement.Config#getHeight()} in pixels and percent, and invalid sizes being reported
 * when the view is loaded.
 */
public class TestWindowElement extends TestCase {

	/** A window with a height in pixels, one sized in percent, and one written without sizes. */
	private static final String VIEW = "window.view.xml";

	/** A window with a height that is no size. */
	private static final String INVALID_HEIGHT_VIEW = "window-invalid-height.view.xml";

	/** A window with a width that is no size. */
	private static final String INVALID_WIDTH_VIEW = "window-invalid-width.view.xml";

	private ViewContext _context;

	private List<WindowElement> _windows;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("/app", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_windows = windows();
	}

	@Override
	protected void tearDown() throws Exception {
		_windows = null;
		_context = null;
		super.tearDown();
	}

	/**
	 * Tests that a height in pixels reaches the control.
	 */
	public void testHeightInPixels() {
		Map<?, ?> state = state(create(0));
		assertEquals("600px", state.get(WindowState.HEIGHT__PROP));
	}

	/**
	 * Tests that width and height in percent reach the control, instead of falling back to a
	 * default width.
	 */
	public void testSizeInPercent() {
		Map<?, ?> state = state(create(1));
		assertEquals("80%", state.get(WindowState.WIDTH__PROP));
		assertEquals("75%", state.get(WindowState.HEIGHT__PROP));
	}

	/**
	 * Tests that a window written without sizes has the default width and leaves its height to
	 * its content.
	 */
	public void testDefaults() {
		Map<?, ?> state = state(create(2));
		assertEquals("500px", state.get(WindowState.WIDTH__PROP));
		assertNull("No height is set.", state.get(WindowState.HEIGHT__PROP));
	}

	/**
	 * Tests that a height that is no size is a configuration error of the view.
	 */
	public void testInvalidHeight() {
		assertInvalid(INVALID_HEIGHT_VIEW);
	}

	/**
	 * Tests that a width that is no size is a configuration error of the view.
	 */
	public void testInvalidWidth() {
		assertInvalid(INVALID_WIDTH_VIEW);
	}

	private static void assertInvalid(String view) {
		try {
			new FixtureViews(TestWindowElement.class).getView(view);
			fail("The invalid size is reported: " + view);
		} catch (ConfigurationException ex) {
			// Expected.
		}
	}

	private ReactWindowControl create(int index) {
		return (ReactWindowControl) _windows.get(index).createControl(_context);
	}

	private static List<WindowElement> windows() throws ConfigurationException {
		ViewElement view = new FixtureViews(TestWindowElement.class).getView(VIEW);
		UIElement stack = ((ChildGroup.Elements) view.getChildGroups().get(0)).children().get(0);
		List<WindowElement> result = new ArrayList<>();
		for (UIElement child : ((ChildGroup.Elements) stack.getChildGroups().get(0)).children()) {
			result.add((WindowElement) child);
		}
		return result;
	}

	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new AssertionError("Not a state object: " + json, ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The toolbars of the window are built from the command cliques.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestWindowElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE));
	}
}
