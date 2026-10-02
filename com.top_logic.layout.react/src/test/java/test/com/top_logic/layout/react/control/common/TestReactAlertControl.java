/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.common;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.Icons;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.overlay.DismissArguments;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.AlertState;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.state.ControlState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests the state a {@link ReactAlertControl} sends to the client and how it answers a dismiss.
 */
public class TestReactAlertControl extends TestCase {

	private int _dismissed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dismissed = 0;
	}

	/** A new alert is hidden and gives an information. */
	public void testInitialState() {
		ReactAlertControl alert = alert();

		assertTrue(hidden(alert));
		assertEquals(Variant.INFO.getExternalName(), state(alert).get(AlertState.VARIANT__PROP));
		assertEquals(Icons.ALERT_INFO.resolve().toEncodedForm(), state(alert).get(AlertState.ICON__PROP));
		assertEquals(Boolean.FALSE, state(alert).get(AlertState.CLOSABLE__PROP));
	}

	/** The setters reach the client state; the variant brings its icon. */
	public void testStateAfterSetters() {
		ReactAlertControl alert = alert();
		alert.show(Variant.WARNING, "Title", "Message");
		alert.setClosable(true);

		Map<String, Object> state = state(alert);
		assertFalse(hidden(alert));
		assertEquals("warning", state.get(AlertState.VARIANT__PROP));
		assertEquals(Icons.ALERT_WARNING.resolve().toEncodedForm(), state.get(AlertState.ICON__PROP));
		assertEquals("Title", state.get(AlertState.TITLE__PROP));
		assertEquals("Message", state.get(AlertState.MESSAGE_TEXT__PROP));
		assertEquals(Boolean.TRUE, state.get(AlertState.CLOSABLE__PROP));
		assertEquals(Variant.WARNING, alert.getVariant());
	}

	/** The actions are sent as child controls. */
	public void testActions() {
		ReactAlertControl alert = alert();
		ReactButtonControl button = new ReactButtonControl(context(), "Do", ctx -> HandlerResult.DEFAULT_RESULT);
		alert.setActions(List.of(button));

		List<?> actions = (List<?>) state(alert).get(AlertState.ACTIONS__PROP);
		assertEquals(1, actions.size());
		assertEquals(button.getID(), ((Map<?, ?>) actions.get(0)).get(ChildControl.CONTROL_ID__PROP));
	}

	/** Each content shown has a generation of its own. */
	public void testGenerationCountsTheContentsShown() {
		ReactAlertControl alert = alert();
		alert.show(Variant.INFO, null, "first");
		int first = generation(alert);

		alert.setMessage("second");
		int second = generation(alert);
		assertTrue(second > first);

		alert.hide();
		alert.setMessage("hidden change");
		assertEquals("A hidden alert shows nothing, a change does not count.", second, generation(alert));

		alert.show();
		assertTrue(generation(alert) > second);
	}

	/**
	 * Setting the content the alert already shows keeps its generation, so a dismiss sent for it
	 * still applies.
	 */
	public void testUnchangedContentKeepsTheGeneration() {
		ReactAlertControl alert = closableAlert();
		int shown = generation(alert);

		alert.show(Variant.ERROR, "Title", "Message");
		alert.setVariant(Variant.ERROR);
		alert.setTitle("Title");
		alert.setMessage("Message");
		assertEquals(shown, generation(alert));

		dismiss(alert, shown);
		assertTrue(hidden(alert));
		assertEquals(1, _dismissed);
	}

	/** Dismissing hides the alert and runs the dismiss handler. */
	public void testDismissHidesAndCallsHandler() {
		ReactAlertControl alert = closableAlert();

		dismiss(alert, generation(alert));

		assertTrue(hidden(alert));
		assertEquals(1, _dismissed);
	}

	/** A dismiss of a content no longer shown changes nothing. */
	public void testStaleDismissIsIgnored() {
		ReactAlertControl alert = closableAlert();
		int stale = generation(alert);
		alert.setMessage("replaced");

		dismiss(alert, stale);

		assertFalse(hidden(alert));
		assertEquals(0, _dismissed);
	}

	/** An alert that cannot be dismissed is not dismissed, whatever the client sends. */
	public void testNotClosableIsNotDismissed() {
		ReactAlertControl alert = closableAlert();
		alert.setClosable(false);

		dismiss(alert, generation(alert));

		assertFalse(hidden(alert));
		assertEquals(0, _dismissed);
	}

	/** A hidden alert is not dismissed again. */
	public void testHiddenIsNotDismissed() {
		ReactAlertControl alert = closableAlert();
		alert.hide();

		dismiss(alert, generation(alert));

		assertEquals(0, _dismissed);
	}

	private ReactAlertControl closableAlert() {
		ReactAlertControl alert = alert();
		alert.setClosable(true);
		alert.show(Variant.ERROR, "Title", "Message");
		return alert;
	}

	private ReactAlertControl alert() {
		ReactAlertControl alert = new ReactAlertControl(context());
		alert.setDismissHandler(ctx -> {
			_dismissed++;
			return HandlerResult.DEFAULT_RESULT;
		});
		return alert;
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private static void dismiss(ReactAlertControl alert, int generation) {
		alert.executeCommand(ReactAlertControl.DISMISS_COMMAND,
			Map.of(DismissArguments.GENERATION, Integer.valueOf(generation)));
	}

	private static boolean hidden(ReactAlertControl alert) {
		return Boolean.TRUE.equals(state(alert).get(ControlState.HIDDEN__PROP));
	}

	private static int generation(ReactAlertControl alert) {
		Object generation = state(alert).get(AlertState.GENERATION__PROP);
		assertTrue("The client is told a generation: " + generation, generation instanceof Number);
		assertEquals(alert.getGeneration(), ((Number) generation).intValue());
		return ((Number) generation).intValue();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> state(ReactAlertControl alert) {
		try {
			return (Map<String, Object>) JSON.fromString(alert.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	/**
	 * The suite of tests, requiring the theme the alert takes its icons from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestReactAlertControl.class,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
