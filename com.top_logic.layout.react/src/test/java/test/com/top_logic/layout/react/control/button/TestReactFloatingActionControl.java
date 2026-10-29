/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.button;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonDisplayMode;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.CommandPlacement;
import com.top_logic.layout.react.control.button.FloatingActionPosition;
import com.top_logic.layout.react.control.button.Icons;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.button.ReactFloatingActionControl;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.state.FloatingActionState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the state a {@link ReactFloatingActionControl} sends to the client.
 */
public class TestReactFloatingActionControl extends TestCase {

	/** The actions are sent as button controls, in their order, each with the state of its command. */
	public void testActionsAreChildButtons() {
		ReactButtonControl create = button(model("create", "Create"));
		ReactButtonControl upload = button(model("upload", "Upload"));
		ReactFloatingActionControl fab = new ReactFloatingActionControl(context(), List.of(create, upload));

		List<Map<String, Object>> actions = actions(fab);
		assertEquals(2, actions.size());
		assertEquals(create.getID(), actions.get(0).get(ChildControl.CONTROL_ID__PROP));
		assertEquals("TLButton", actions.get(0).get(ChildControl.MODULE__PROP));
		assertEquals(upload.getID(), actions.get(1).get(ChildControl.CONTROL_ID__PROP));
		assertEquals("Create", childState(actions.get(0)).get(ButtonState.LABEL__PROP));
		assertEquals(Icons.FLOATING_ACTION_DIAL.resolve().toEncodedForm(),
			childState(actions.get(0)).get(ButtonState.IMAGE__PROP));
	}

	/**
	 * An action shows its icon only, a round button; a command asking for its label keeps it (an
	 * extended floating action button).
	 */
	public void testActionsShowTheirIconUnlessTheCommandAsksForTheLabel() {
		ReactButtonControl plain = button(model("plain", "Plain"));
		ReactButtonControl extended = button(new LabeledModel(model("extended", "Extended")));
		ReactFloatingActionControl fab = new ReactFloatingActionControl(context(), List.of(plain, extended));

		List<Map<String, Object>> actions = actions(fab);
		assertEquals(ButtonDisplayMode.ICON_ONLY.getExternalName(),
			childState(actions.get(0)).get(ButtonState.DISPLAY_MODE__PROP));
		assertEquals(ButtonDisplayMode.ICON_LABEL.getExternalName(),
			childState(actions.get(1)).get(ButtonState.DISPLAY_MODE__PROP));
	}

	/**
	 * Hidden actions are sent with their hidden state: the client decides on the presentation by
	 * the actions not hidden, and renders nothing while all are hidden. The control itself is not
	 * hidden, so that an action becoming visible shows up.
	 */
	public void testHiddenActionsKeepTheirState() {
		SimpleCommandModel first = model("first", "First").setVisible(() -> false);
		SimpleCommandModel second = model("second", "Second").setVisible(() -> false);
		ReactFloatingActionControl fab =
			new ReactFloatingActionControl(context(), List.of(button(first), button(second)));

		Map<String, Object> state = state(fab);
		assertNotSame(Boolean.TRUE, state.get(ControlState.HIDDEN__PROP));
		List<Map<String, Object>> actions = actions(fab);
		assertEquals(2, actions.size());
		for (Map<String, Object> action : actions) {
			assertEquals(Boolean.TRUE, childState(action).get(ControlState.HIDDEN__PROP));
		}
	}

	/** The default corner is not sent; another corner is sent by its external name. */
	public void testPosition() {
		ReactFloatingActionControl fab = new ReactFloatingActionControl(context(), List.of());
		assertNull(state(fab).get(FloatingActionState.POSITION__PROP));
		assertEquals(FloatingActionPosition.BOTTOM_END, fab.getPosition());

		fab.setPosition(FloatingActionPosition.TOP_START);
		assertEquals("top-start", state(fab).get(FloatingActionState.POSITION__PROP));
		assertEquals(FloatingActionPosition.TOP_START, fab.getPosition());

		fab.setPosition(null);
		assertNull(state(fab).get(FloatingActionState.POSITION__PROP));
	}

	/** The trigger of the speed dial has an icon and a label by default. */
	public void testTriggerDefaults() {
		ReactFloatingActionControl fab = new ReactFloatingActionControl(context(), List.of());
		Map<String, Object> state = state(fab);
		assertEquals(Icons.FLOATING_ACTION_DIAL.resolve().toEncodedForm(),
			state.get(FloatingActionState.TRIGGER_IMAGE__PROP));
		Object label = state.get(FloatingActionState.TRIGGER_LABEL__PROP);
		assertTrue("The trigger is named: " + label, label instanceof String && !((String) label).isEmpty());

		fab.setTriggerLabel("More");
		assertEquals("More", state(fab).get(FloatingActionState.TRIGGER_LABEL__PROP));
	}

	/** Replacing the actions disposes the ones no longer shown. */
	public void testReplacedActionsAreDisposed() {
		ReactButtonControl kept = button(model("kept", "Kept"));
		ReactButtonControl dropped = button(model("dropped", "Dropped"));
		ReactFloatingActionControl fab = new ReactFloatingActionControl(context(), List.of(kept, dropped));

		fab.setActions(List.of(kept));

		assertEquals(List.of(kept), fab.actions());
		assertTrue(dropped.isDisposed());
		assertFalse(kept.isDisposed());
	}

	private ReactButtonControl button(CommandModel model) {
		return new ReactButtonControl(context(), model);
	}

	private static SimpleCommandModel model(String name, String label) {
		return SimpleCommandModel.create(name, label, ctx -> HandlerResult.DEFAULT_RESULT)
			.setImage(Icons.FLOATING_ACTION_DIAL);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> actions(ReactFloatingActionControl fab) {
		return (List<Map<String, Object>>) state(fab).get(FloatingActionState.ACTIONS__PROP);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> childState(Map<String, Object> child) {
		return (Map<String, Object>) child.get(ChildControl.STATE__PROP);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> state(ReactFloatingActionControl fab) {
		try {
			return (Map<String, Object>) JSON.fromString(fab.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * A command asking to show its icon and its label.
	 */
	private static final class LabeledModel implements CommandModel {

		private final CommandModel _inner;

		LabeledModel(CommandModel inner) {
			_inner = inner;
		}

		@Override
		public ButtonDisplayMode getDisplayMode() {
			return ButtonDisplayMode.ICON_LABEL;
		}

		@Override
		public String getName() {
			return _inner.getName();
		}

		@Override
		public String getLabel() {
			return _inner.getLabel();
		}

		@Override
		public ThemeImage getImage() {
			return _inner.getImage();
		}

		@Override
		public boolean isExecutable() {
			return _inner.isExecutable();
		}

		@Override
		public boolean isVisible() {
			return _inner.isVisible();
		}

		@Override
		public HandlerResult perform(ReactContext context) {
			return _inner.perform(context);
		}

		@Override
		public CommandPlacement getPlacement() {
			return _inner.getPlacement();
		}

		@Override
		public void addStateChangeListener(Runnable listener) {
			_inner.addStateChangeListener(listener);
		}

		@Override
		public void removeStateChangeListener(Runnable listener) {
			_inner.removeStateChangeListener(listener);
		}
	}

	/**
	 * The suite of tests, requiring the theme the icons come from and the resources the default
	 * label of the trigger comes from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestReactFloatingActionControl.class,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
