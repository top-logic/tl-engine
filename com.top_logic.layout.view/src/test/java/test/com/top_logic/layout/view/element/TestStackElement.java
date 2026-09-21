/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackJustify;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.GridElement;
import com.top_logic.layout.view.element.StackElement;

/**
 * Tests the layout options a {@code <stack>} and a {@code <grid>} state: how the free space along
 * the direction is distributed, whether the children flow into further lines, and the width the
 * container is bounded to.
 *
 * <p>
 * Each option travels under its own state key and by its external name, the default included, so
 * the client draws what was configured rather than guessing a default of its own.
 * </p>
 *
 * @see StackElement
 * @see GridElement
 */
public class TestStackElement extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	/** The view the containers under test are read from. */
	private static final String VIEW = "test-stack.view.xml";

	/**
	 * State key holding the children of a layout container.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.layout.ReactLayoutControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String CHILDREN = "children";

	/**
	 * State key holding the flow direction of a stack.
	 *
	 * @implNote Restated here because {@link ReactStackControl} keeps it private.
	 */
	private static final String DIRECTION = "direction";

	/**
	 * State key holding the distribution of the free space along the direction of a stack.
	 *
	 * @implNote Restated here because {@link ReactStackControl} keeps it private.
	 */
	private static final String JUSTIFY = "justify";

	/**
	 * State key holding whether the children of a stack flow into further lines.
	 *
	 * @implNote Restated here because {@link ReactStackControl} keeps it private.
	 */
	private static final String WRAP = "wrap";

	/**
	 * State key holding the width a layout container is bounded to.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.layout.ReactLayoutControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String MAX_WIDTH = "maxWidth";

	/** State key holding the client state of a control descriptor. */
	private static final String STATE = "state";

	private Map<?, ?> _stack;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_stack = state(createContent());
	}

	@Override
	protected void tearDown() throws Exception {
		_stack = null;

		super.tearDown();
	}

	/** A stack that states no distribution places its children one after the other from the start. */
	public void testDefaultJustify() {
		assertEquals(StackJustify.START.getExternalName(), child(0).get(JUSTIFY));
	}

	/** A stack that states nothing keeps its children in one line. */
	public void testDefaultWrap() {
		assertEquals(Boolean.FALSE, child(0).get(WRAP));
	}

	/** A stack that states no width bound spans the space its container offers. */
	public void testDefaultMaxWidth() {
		assertNull("An unbounded stack must not send a width bound.", child(0).get(MAX_WIDTH));
	}

	/** A configured distribution arrives by its external name, the hyphenated one included. */
	public void testConfiguredJustify() {
		Map<?, ?> row = child(1);

		assertEquals(StackDirection.ROW.getExternalName(), row.get(DIRECTION));
		assertEquals(StackJustify.SPACE_BETWEEN.getExternalName(), row.get(JUSTIFY));
	}

	/** A row configured to wrap reports that, so the client lets its children flow. */
	public void testConfiguredWrap() {
		assertEquals(Boolean.TRUE, child(1).get(WRAP));
	}

	/** The configured width bound reaches the client as the CSS length it was written as. */
	public void testConfiguredMaxWidth() {
		Map<?, ?> bounded = child(2);

		assertEquals("60rem", bounded.get(MAX_WIDTH));
		assertEquals("The options are independent of each other.",
			StackJustify.CENTER.getExternalName(), bounded.get(JUSTIFY));
		assertEquals("Bounding the width must leave the line behavior alone.",
			Boolean.FALSE, bounded.get(WRAP));
	}

	/** A {@code <grid>} is bounded by the same option, which reaches {@link ReactGridControl}. */
	public void testGridMaxWidth() {
		assertEquals("48rem", child(3).get(MAX_WIDTH));
	}

	/** The client state of the child at the given position of the container. */
	private Map<?, ?> child(int index) {
		List<?> children = (List<?>) _stack.get(CHILDREN);
		assertTrue("The view holds " + (index + 1) + " children, but only " + children.size() + " were created.",
			index < children.size());
		return (Map<?, ?>) ((Map<?, ?>) children.get(index)).get(STATE);
	}

	/** The control of the element the test view shows. */
	private static ReactControl createContent() throws ConfigurationException {
		ViewElement.Config view = ViewLoader.parseConfig(
			List.of(new ClassRelativeBinaryContent(TestStackElement.class, VIEW)));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestStackElement.class);
		UIElement element = context.getInstance(view.getContent());
		context.checkErrors();

		ViewContext viewContext = new DefaultViewContext(new DefaultReactContext(CONTEXT_PATH, "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));

		return (ReactControl) element.createControl(viewContext);
	}

	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestStackElement.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
