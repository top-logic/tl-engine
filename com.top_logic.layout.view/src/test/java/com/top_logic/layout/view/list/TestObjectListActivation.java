/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.view.command.ArgumentsProbe;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ActivateItemArguments;
import com.top_logic.layout.react.control.layout.ItemAction;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.DisabledIf;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.VisibleIf;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests the activation of the elements of an {@link ObjectListElement} by its
 * {@link ObjectListElement.Config#getOnActivate() activation command}: the command runs with the
 * activated element under the name {@link ObjectListElement.Config#getElementAs() element-as} gives
 * it, its executability rules decide per element whether the element is activatable, and in a
 * selectable list the element becomes the selection before the command runs.
 */
public class TestObjectListActivation extends BasicTestCase {

	/** Name of the views given inline. */
	private static final String INLINE_VIEW = "inline-object-list-activation.view.xml";

	/** Name of the selection channel. */
	private static final String SELECTED = "selected";

	/** The name the element is given by {@link ObjectListElement.Config#ELEMENT_AS}. */
	private static final String ELEMENT_AS = "project";

	/** An element the activation command accepts. */
	private static final String OPEN = "open";

	/** An element the {@link VisibleIf} rule of the activation command hides the command for. */
	private static final String HIDDEN = "hidden";

	/** An element the {@link DisabledIf} rule of the activation command disables the command for. */
	private static final String DISABLED = "disabled";

	/** The reason the {@link DisabledIf} rule gives for the {@link #DISABLED} element. */
	private static final String LOCKED = "The element is locked.";

	/** The displayed elements, in display order. */
	private static final List<String> ELEMENTS = List.of(OPEN, HIDDEN, DISABLED);

	/** The list attribute declaring the selection channel. */
	private static final String WITH_SELECTION = ObjectListElement.Config.SELECTION + "=\"" + SELECTED + "\"";

	private ViewContext _context;

	private ViewChannel _selected;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ArgumentsProbe.reset();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_selected = new DefaultViewChannel(SELECTED);
		_context.registerChannel(SELECTED, _selected);
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		_context = null;
		_selected = null;

		super.tearDown();
	}

	/**
	 * Activating an element the rules accept runs the command once, with the element as its main
	 * argument named by {@link ObjectListElement.Config#ELEMENT_AS}.
	 */
	public void testActivationRunsCommandWithElement() throws Exception {
		ReactControl display = createList("", true);
		display.attach();
		try {
			activate(display, OPEN);

			assertEquals("The command runs once.", 1, ArgumentsProbe.SEEN.size());
			assertEquals("The command runs with the activated element.", OPEN, ArgumentsProbe.SEEN.get(0).value());
			assertEquals("The element is named as element-as says.", OPEN,
				ArgumentsProbe.SEEN.get(0).get(ELEMENT_AS));
			assertNull("A list without selection channel selects nothing.", _selected.get());
		} finally {
			display.detach();
		}
	}

	/**
	 * Each element carries what activating it offers: an element the rules hide the command for is
	 * not activatable, one they disable it for is marked disabled with the reason of the rule.
	 */
	public void testActivationState() throws Exception {
		ReactControl display = createList("", true);
		display.attach();
		try {
			Map<?, ?> state = state(elements(display));
			List<?> actions = (List<?>) state.get(ReactLayoutControl.ITEM_ACTIONS);
			assertEquals("An action per element.", ELEMENTS.size(), actions.size());

			Map<?, ?> open = (Map<?, ?>) actions.get(ELEMENTS.indexOf(OPEN));
			assertEquals("An accepted element is enabled.", Boolean.FALSE, open.get(ItemAction.DISABLED));
			assertNotNull("An activatable element is named.", open.get(ItemAction.LABEL));

			assertNull("An element the command is hidden for is not activatable.",
				actions.get(ELEMENTS.indexOf(HIDDEN)));

			Map<?, ?> disabled = (Map<?, ?>) actions.get(ELEMENTS.indexOf(DISABLED));
			assertEquals("A refused element shows it.", Boolean.TRUE, disabled.get(ItemAction.DISABLED));
			assertEquals("A refused element says why.", LOCKED, disabled.get(ItemAction.TOOLTIP));

			assertNull("A list without selection channel is not selectable.",
				state.get(ReactLayoutControl.SELECTABLE));
		} finally {
			display.detach();
		}
	}

	/**
	 * Activating an element the rules hide or disable the command for runs nothing and reports
	 * nothing.
	 */
	public void testRefusedElementsRunNothing() throws Exception {
		ReactControl display = createList("", true);
		display.attach();
		try {
			for (String element : List.of(HIDDEN, DISABLED)) {
				assertNull("Activating a refused element reports nothing: " + element,
					CommandFailures.outcome(() -> activate(display, element)));
				assertEquals("The command does not run for " + element, List.of(), ArgumentsProbe.SEEN);
			}
		} finally {
			display.detach();
		}
	}

	/**
	 * In a selectable list, the activated element becomes the selection before the command runs: the
	 * command reading the selection channel sees the element there.
	 */
	public void testSelectionBeforeCommand() throws Exception {
		ReactControl display = createList(WITH_SELECTION, true);
		display.attach();
		try {
			Map<?, ?> state = state(elements(display));
			assertEquals("The list is selectable.", Boolean.TRUE, state.get(ReactLayoutControl.SELECTABLE));
			assertEquals("The elements show the open button.", Boolean.TRUE,
				state.get(ReactLayoutControl.ACTIVATION_BUTTON));

			activate(display, OPEN);

			assertEquals("The activated element is selected.", OPEN, _selected.get());
			assertEquals("The command runs once.", 1, ArgumentsProbe.SEEN.size());
			assertEquals("The command sees the element in the selection already.", OPEN,
				ArgumentsProbe.SEEN.get(0).get(SELECTED));
		} finally {
			display.detach();
		}
	}

	/**
	 * In a selectable list, activating an element the rules disable the command for only selects it,
	 * and an element the command is hidden for is not activated at all, not even selected.
	 */
	public void testSelectableRefusedElements() throws Exception {
		ReactControl display = createList(WITH_SELECTION, true);
		display.attach();
		try {
			activate(display, DISABLED);
			assertEquals("A disabled element is selected.", DISABLED, _selected.get());
			assertEquals("The command does not run.", List.of(), ArgumentsProbe.SEEN);

			activate(display, HIDDEN);
			assertEquals("An element that cannot be activated is not selected either.", DISABLED, _selected.get());
			assertEquals("The command does not run.", List.of(), ArgumentsProbe.SEEN);
		} finally {
			display.detach();
		}
	}

	/**
	 * The open button is switched off by {@link ObjectListElement.Config#ACTIVATION_BUTTON}.
	 */
	public void testActivationButtonOff() throws Exception {
		ReactControl display = createList(WITH_SELECTION, false);
		assertEquals("The elements show no open button.", Boolean.FALSE,
			state(elements(display)).get(ReactLayoutControl.ACTIVATION_BUTTON));
	}

	/**
	 * A list without activation command has no activation state, and its elements are not
	 * activated.
	 */
	public void testWithoutActivation() throws Exception {
		ReactControl display = createControl(view("", ""));
		display.attach();
		try {
			ReactLayoutControl elements = elements(display);
			assertFalse("The elements are not activatable.", elements.isActivatable());
			Map<?, ?> state = state(elements);
			assertNull("No action state.", state.get(ReactLayoutControl.ITEM_ACTIONS));
			assertNull("No button state.", state.get(ReactLayoutControl.ACTIVATION_BUTTON));
			assertNull("No item keys.", state.get(ReactLayoutControl.ITEM_KEYS));
		} finally {
			display.detach();
		}
	}

	/**
	 * Activates the item displaying the given element, as the client does.
	 */
	private static void activate(ReactControl display, String element) {
		ReactLayoutControl elements = elements(display);
		List<?> keys = (List<?>) state(elements).get(ReactLayoutControl.ITEM_KEYS);
		elements.executeClientCommand(ReactLayoutControl.CMD_ACTIVATE_ITEM,
			Map.of(ActivateItemArguments.KEY, keys.get(ELEMENTS.indexOf(element))));
	}

	/**
	 * The container arranging the elements: the first child of the displayed list.
	 */
	private static ReactLayoutControl elements(ReactControl display) {
		return (ReactLayoutControl) ((ReactLayoutControl) display).displayedChildren().get(0);
	}

	/** The state of the given control as the client parses it. */
	private static Map<?, ?> state(ReactControl control) {
		String json = control.stateAsJSON();
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * A list of the {@link #ELEMENTS} with an activation command deciding by rules over the
	 * activated element, and recording its runs in the {@link ArgumentsProbe}.
	 *
	 * @param attributes
	 *        Further attributes of the list.
	 * @param button
	 *        The value of {@link ObjectListElement.Config#ACTIVATION_BUTTON}.
	 */
	private ReactControl createList(String attributes, boolean button) throws ConfigurationException {
		String onActivate = """
			<on-activate class="%s" inputs="%s">
				<executability>
					<%s expr='$%s != "%s"'/>
					<%s expr='if($%s == "%s", "%s")'/>
				</executability>
				<action class="%s"/>
			</on-activate>
			""".formatted(GenericViewCommand.class.getName(), SELECTED,
			VisibleIf.Config.TAG_NAME, ELEMENT_AS, HIDDEN,
			DisabledIf.Config.TAG_NAME, ELEMENT_AS, DISABLED, LOCKED,
			ArgumentsProbe.class.getName());
		return createControl(view(attributes + " " + ObjectListElement.Config.ACTIVATION_BUTTON + "=\"" + button
			+ "\" " + ObjectListElement.Config.ELEMENT_AS + "=\"" + ELEMENT_AS + "\"", onActivate));
	}

	/**
	 * A view holding a list of the {@link #ELEMENTS}.
	 */
	private static String view(String attributes, String content) {
		return """
			<view>
				<object-list %s>
					<items>["%s"]</items>
					<item>
						<text input="element"/>
					</item>
					%s
				</object-list>
			</view>
			""".formatted(attributes, String.join("\", \"", ELEMENTS), content);
	}

	private ReactControl createControl(String view) throws ConfigurationException {
		ObjectListElement element = (ObjectListElement) instantiate(view);
		return (ReactControl) element.createControl(_context);
	}

	private static Object instantiate(String view) throws ConfigurationException {
		ViewElement.Config config =
			ViewLoader.parseConfig(List.of(CharacterContents.newContent(view, INLINE_VIEW)));
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestObjectListActivation.class);
		Object element = instantiation.getInstance(config.getContent());
		instantiation.checkErrors();
		return element;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The elements and the rules are TL-Script expressions, whose compilation and
	 *           evaluation need the application model and the
	 *           {@link com.top_logic.knowledge.service.KnowledgeBase} they are executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestObjectListActivation.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE, LabelProviderService.Module.INSTANCE));
	}

}
