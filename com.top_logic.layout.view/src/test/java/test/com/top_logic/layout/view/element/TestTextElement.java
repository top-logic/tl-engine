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
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.common.TextAppearance;
import com.top_logic.layout.react.control.common.TextOverflow;
import com.top_logic.layout.react.control.common.TextTone;
import com.top_logic.layout.react.control.common.TextVariant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.TextElement;

/**
 * Tests that the display roles a {@code <text>} states - what the text is for, what its color
 * means, the shape it takes - reach the client as properties of their own.
 *
 * <p>
 * Each role travels under its own state key and by its external name, the default of the role
 * included, so the client draws what was configured rather than guessing a default of its own. How
 * the text overflows stays the property it already was.
 * </p>
 *
 * @see TextElement
 * @see ReactTextControl
 */
public class TestTextElement extends TestCase {

	private static final String CONTEXT_PATH = "/app";

	/** The view the texts under test are read from. */
	private static final String VIEW = "test-text.view.xml";

	/**
	 * State key holding the children of a layout container.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.layout.ReactLayoutControl} keeps it
	 *           protected for its subclasses.
	 */
	private static final String CHILDREN = "children";

	/**
	 * State key holding the typographic role of a piece of text.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.common.ReactTextControl} keeps it private.
	 */
	private static final String VARIANT = "variant";

	/**
	 * State key holding the color role of a piece of text.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.common.ReactTextControl} keeps it private.
	 */
	private static final String TONE = "tone";

	/**
	 * State key holding the shape a piece of text is drawn in.
	 *
	 * @implNote Restated here because
	 *           {@link com.top_logic.layout.react.control.common.ReactTextControl} keeps it private.
	 */
	private static final String APPEARANCE = "appearance";

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

	/** Text that states no role is running text, so the client needs no default of its own. */
	public void testDefaultVariant() {
		assertEquals(TextVariant.BODY.getExternalName(), child(0).get(VARIANT));
	}

	/** Text that states no color role is read in the primary color. */
	public void testDefaultTone() {
		assertEquals(TextTone.PRIMARY.getExternalName(), child(0).get(TONE));
	}

	/** Text that states no shape is plain text. */
	public void testDefaultAppearance() {
		assertEquals(TextAppearance.TEXT.getExternalName(), child(0).get(APPEARANCE));
	}

	/** A configured typographic role arrives by its external name. */
	public void testConfiguredVariant() {
		assertEquals(TextVariant.HEADLINE.getExternalName(), child(1).get(VARIANT));
	}

	/** A configured color role arrives by its external name, the hyphenated one included. */
	public void testConfiguredTone() {
		assertEquals(TextTone.ON_COLOR.getExternalName(), child(1).get(TONE));
	}

	/** A configured shape arrives by its external name. */
	public void testConfiguredAppearance() {
		assertEquals(TextAppearance.PILL.getExternalName(), child(1).get(APPEARANCE));
	}

	/** The roles are independent of each other: stating one leaves the others at their default. */
	public void testRolesAreIndependent() {
		Map<?, ?> truncated = child(2);

		assertEquals(TextOverflow.ELLIPSIS.getExternalName(), truncated.get(OVERFLOW));
		assertEquals(TextTone.ERROR.getExternalName(), truncated.get(TONE));
		assertEquals("Stating a color role must leave the typographic role at its default.",
			TextVariant.BODY.getExternalName(), truncated.get(VARIANT));
		assertEquals("Stating a color role must leave the shape at its default.",
			TextAppearance.TEXT.getExternalName(), truncated.get(APPEARANCE));
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
			List.of(new ClassRelativeBinaryContent(TestTextElement.class, VIEW)));

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTextElement.class);
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
			ServiceTestSetup.createSetup(TestTextElement.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
