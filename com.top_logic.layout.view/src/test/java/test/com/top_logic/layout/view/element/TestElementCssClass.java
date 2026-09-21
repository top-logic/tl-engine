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
import com.top_logic.layout.react.control.common.TextOverflow;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;

/**
 * Tests that the {@link UIElement.Config#getCssClass() CSS class} configured for an element reaches
 * the client as the CSS class of the control displaying it.
 *
 * <p>
 * The property is declared once for all elements, so the test exercises elements of different kinds
 * - a container, a piece of text, a picture - through the one seam they share: a view read the way
 * the application reads it, a control created for a view context, and the client state that control
 * publishes.
 * </p>
 */
public class TestElementCssClass extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	/** The view the elements under test are read from. */
	private static final String VIEW = "test-css-class.view.xml";

	/**
	 * State key holding the children of a layout container.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.layout.ReactLayoutControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String CHILDREN = "children";

	/**
	 * State key holding the overflow handling of a piece of text.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.common.ReactTextControl} keeps it private.
	 */
	private static final String OVERFLOW = "overflow";

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

	/** The class of a container element reaches the control arranging its children. */
	public void testContainerCssClass() {
		assertEquals("tlDemoStack", _stack.get(ReactControl.CSS_CLASS));
	}

	/** The class of a text element reaches the control displaying it. */
	public void testTextCssClass() {
		assertEquals("tlDemoText", child(1).get(ReactControl.CSS_CLASS));
	}

	/** The class of a picture reaches the control displaying it. */
	public void testImageCssClass() {
		assertEquals("tlDemoImage", child(3).get(ReactControl.CSS_CLASS));
	}

	/** An element that states no class has none, rather than an empty one. */
	public void testWithoutTheProperty() {
		assertNull("An element without the property must leave the class unset.",
			child(0).get(ReactControl.CSS_CLASS));
	}

	/**
	 * How text overflows is a property of its own, so the configured class stays what was
	 * configured.
	 */
	public void testOverflowIsNoCssClass() {
		Map<?, ?> truncated = child(2);

		assertEquals("The class must carry nothing but what was configured.",
			"tlDemoEllipsis", truncated.get(ReactControl.CSS_CLASS));
		assertEquals("The overflow handling reaches the client as a property of its own.",
			TextOverflow.ELLIPSIS.getExternalName(), truncated.get(OVERFLOW));
	}

	/** Text that states no overflow handling wraps. */
	public void testTextWrapsByDefault() {
		assertEquals(TextOverflow.WRAP.getExternalName(), child(0).get(OVERFLOW));
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
			List.of(new ClassRelativeBinaryContent(TestElementCssClass.class, VIEW)));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestElementCssClass.class);
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
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * and the services that turn the configured label of a text into the text displayed.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestElementCssClass.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
