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

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.FloatingActionPosition;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.button.ReactFloatingActionControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.state.FloatingActionState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.element.FloatingActionElement;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests the {@link FloatingActionElement}: its configuration, and the actions of the
 * {@link ReactFloatingActionControl} it creates following the rules of their commands.
 *
 * <p>
 * The element is exercised through its public seam - a view read the way the application reads it,
 * a control created for a view context holding the channel the commands decide over, and the state
 * that control and its buttons publish once displayed.
 * </p>
 */
public class TestFloatingActionElement extends TestCase {

	/** The channel the commands of the test views take as input. */
	private static final String FLAG_CHANNEL = "flag";

	/** A command hidden while {@link #FLAG_CHANNEL} is empty. */
	private static final String HIDDEN_WHILE_EMPTY = """
			<generic-command name="hiddenWhileEmpty" inputs="%s" image="css:bi bi-eye">
				<executability>
					<null-input-hidden/>
				</executability>
			</generic-command>
			""".formatted(FLAG_CHANNEL);

	/** A command disabled, but still shown, while {@link #FLAG_CHANNEL} is empty. */
	private static final String DISABLED_WHILE_EMPTY = """
			<generic-command name="disabledWhileEmpty" inputs="%s" image="css:bi bi-pencil">
				<executability>
					<null-input-disabled/>
				</executability>
			</generic-command>
			""".formatted(FLAG_CHANNEL);

	/** A command without rules, written with the generic entry tag. */
	private static final String ALWAYS = """
			<action class="%s" name="always" image="css:bi bi-plus-lg">
				<label><en>Add</en></label>
			</action>
			""".formatted(GenericViewCommand.class.getName());

	private ViewChannel _flag;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_flag = new DefaultViewChannel(FLAG_CHANNEL);
	}

	/** A single action without further settings stands in the default corner. */
	public void testDefaults() throws Exception {
		FloatingActionElement.Config config = config("<floating-action>" + ALWAYS + "</floating-action>");

		assertEquals(1, config.getActions().size());
		assertEquals(FloatingActionPosition.BOTTOM_END, config.getPosition());
		assertNull(config.getImage());
		assertNull(config.getLabel());

		ReactFloatingActionControl control = display(config);
		assertEquals(FloatingActionPosition.BOTTOM_END, control.getPosition());
		assertEquals(1, control.actions().size());
	}

	/** The configured corner and trigger reach the control. */
	public void testPositionAndTrigger() throws Exception {
		FloatingActionElement.Config config = config("""
				<floating-action position="top-start" image="css:bi bi-gear">
					<label><en>Tools</en></label>
					%s
				</floating-action>
				""".formatted(ALWAYS));

		assertEquals(FloatingActionPosition.TOP_START, config.getPosition());
		assertNotNull(config.getImage());

		ReactFloatingActionControl control = display(config);
		assertEquals(FloatingActionPosition.TOP_START, control.getPosition());
		assertEquals("Tools", state(control).get(FloatingActionState.TRIGGER_LABEL__PROP));
	}

	/** Several actions, written by command tag or as generic entry, form one button each, in order. */
	public void testSeveralActions() throws Exception {
		FloatingActionElement.Config config = config(
			"<floating-action>" + ALWAYS + HIDDEN_WHILE_EMPTY + DISABLED_WHILE_EMPTY + "</floating-action>");
		assertEquals(3, config.getActions().size());

		List<ReactButtonControl> actions = display(config).actions();
		assertEquals("One button per action.", 3, actions.size());
		assertEquals("Add", state(actions.get(0)).get(ButtonState.LABEL__PROP));
	}

	/** Each button follows the hiding and disabling rules of its command. */
	public void testActionsFollowTheirRules() throws Exception {
		List<ReactButtonControl> actions = display(config(
			"<floating-action>" + HIDDEN_WHILE_EMPTY + DISABLED_WHILE_EMPTY + "</floating-action>")).actions();
		ReactButtonControl hidden = actions.get(0);
		ReactButtonControl disabled = actions.get(1);

		assertTrue("A command hidden by its rule hides its action.", hidden.isHidden());
		assertFalse("A disabled command keeps its action.", disabled.isHidden());
		assertEquals(Boolean.TRUE, state(disabled).get(ButtonState.DISABLED__PROP));

		_flag.set("on");
		assertFalse("The command becoming visible shows its action.", hidden.isHidden());
		assertEquals(Boolean.FALSE, state(disabled).get(ButtonState.DISABLED__PROP));

		_flag.set(null);
		assertTrue(hidden.isHidden());
		assertEquals(Boolean.TRUE, state(disabled).get(ButtonState.DISABLED__PROP));
	}

	/** An element without actions is a configuration error. */
	public void testRequiresAction() throws Exception {
		FloatingActionElement.Config config = config("<floating-action/>");
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestFloatingActionElement.class);
		context.getInstance(config);
		try {
			context.checkErrors();
			fail("A floating action without actions must be refused.");
		} catch (AbortExecutionException ex) {
			// Expected.
		}
	}

	/** The displayed control of the given element. */
	private ReactFloatingActionControl display(FloatingActionElement.Config config) throws ConfigurationException {
		DefaultInstantiationContext instantiationContext =
			new DefaultInstantiationContext(TestFloatingActionElement.class);
		UIElement element = instantiationContext.getInstance(config);
		instantiationContext.checkErrors();

		ViewContext context = new DefaultViewContext(new PageContext());
		context.registerChannel(FLAG_CHANNEL, _flag);

		ReactFloatingActionControl control = (ReactFloatingActionControl) element.createControl(context);
		// Displaying the control lets its commands follow their input.
		control.attach();
		return control;
	}

	private static FloatingActionElement.Config config(String elementXml) throws ConfigurationException {
		String view = "<view>" + elementXml + "</view>";
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, "test-floating.view.xml")));
		PolymorphicConfiguration<? extends UIElement> content = config.getContent();
		assertTrue("Unexpected content: " + content, content instanceof FloatingActionElement.Config);
		return (FloatingActionElement.Config) content;
	}

	private static Map<?, ?> state(ReactControl control) throws Exception {
		return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
	}

	/**
	 * The context of the displayed page.
	 *
	 * <p>
	 * Nothing displayed here is an object others observe, so the page reports no
	 * {@link ModelScope}, and the commands follow their input channel alone.
	 * </p>
	 */
	private static final class PageContext extends DefaultReactContext {

		PageContext() {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, which resolves the element tags of a view,
	 * and the theme and resources the buttons resolve their icons and labels with.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestFloatingActionElement.class,
			TypeIndex.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}
}
