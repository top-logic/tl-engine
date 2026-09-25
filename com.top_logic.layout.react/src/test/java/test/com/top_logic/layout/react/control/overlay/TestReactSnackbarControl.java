/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DismissArguments;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.SnackbarState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactSnackbarControl} shows every message it is given: one at a time, in
 * arrival order.
 *
 * <p>
 * A burst of notifications (e.g. one per file of a failed upload) arrives within milliseconds. The
 * snackbar displays a single message, so the messages behind the visible one must wait for their
 * turn rather than replace it.
 * </p>
 */
public class TestReactSnackbarControl extends TestCase {

	private int _dismissed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_dismissed = 0;
	}

	/**
	 * A message arriving while another one is shown waits, and takes the screen when that one is
	 * dismissed.
	 */
	public void testASecondMessageWaitsForItsTurn() {
		ReactSnackbarControl snackbar = snackbar();

		snackbar.showHtml("first", Variant.ERROR);
		snackbar.showHtml("second", Variant.ERROR);

		assertEquals("first", content(snackbar));
		assertTrue(visible(snackbar));
		int firstGeneration = generation(snackbar);

		dismiss(snackbar, firstGeneration);

		assertEquals("second", content(snackbar));
		assertTrue("The snackbar stays on screen while the queue feeds it.", visible(snackbar));
		assertTrue("Each message is shown under a generation of its own.",
			generation(snackbar) > firstGeneration);
		assertEquals("The snackbar has not gone off screen yet.", 0, _dismissed);
	}

	/** The queue is worked off in arrival order, one message per dismiss. */
	public void testMessagesAreShownInArrivalOrder() {
		ReactSnackbarControl snackbar = snackbar();

		snackbar.showHtml("first", Variant.ERROR);
		snackbar.showHtml("second", Variant.ERROR);
		snackbar.showHtml("third", Variant.ERROR);

		assertEquals("first", content(snackbar));
		dismiss(snackbar, generation(snackbar));
		assertEquals("second", content(snackbar));
		dismiss(snackbar, generation(snackbar));
		assertEquals("third", content(snackbar));

		dismiss(snackbar, generation(snackbar));
		assertFalse(visible(snackbar));
		assertEquals(1, _dismissed);
	}

	/** A dismiss reporting an outdated generation belongs to a message already gone: it is ignored. */
	public void testAStaleDismissChangesNothing() {
		ReactSnackbarControl snackbar = snackbar();

		snackbar.showHtml("first", Variant.ERROR);
		snackbar.showHtml("second", Variant.ERROR);
		int shown = generation(snackbar);

		dismiss(snackbar, shown - 1);

		assertEquals("first", content(snackbar));
		assertTrue(visible(snackbar));
		assertEquals(shown, generation(snackbar));
		assertEquals(0, _dismissed);
	}

	/** With nothing left to show, a dismiss takes the snackbar off screen and reports that. */
	public void testTheLastDismissEndsTheSeries() {
		ReactSnackbarControl snackbar = snackbar();

		snackbar.showHtml("only", Variant.ERROR);
		dismiss(snackbar, generation(snackbar));

		assertFalse(visible(snackbar));
		assertEquals(1, _dismissed);
	}

	/** A message given while nothing is shown goes on screen right away. */
	public void testAMessageGivenToAnEmptySnackbarIsShownAtOnce() {
		ReactSnackbarControl snackbar = snackbar();

		snackbar.showHtml("first", Variant.ERROR);
		dismiss(snackbar, generation(snackbar));
		snackbar.showHtml("later", Variant.INFO);

		assertEquals("later", content(snackbar));
		assertTrue(visible(snackbar));
	}

	private void dismiss(ReactSnackbarControl snackbar, int generation) {
		snackbar.executeCommand(ReactSnackbarControl.DISMISS_COMMAND,
			Map.of(DismissArguments.GENERATION, Integer.valueOf(generation)));
	}

	private static String content(ReactSnackbarControl snackbar) {
		return (String) state(snackbar).get(SnackbarState.CONTENT__PROP);
	}

	private static boolean visible(ReactSnackbarControl snackbar) {
		return Boolean.TRUE.equals(state(snackbar).get(SnackbarState.VISIBLE__PROP));
	}

	private static int generation(ReactSnackbarControl snackbar) {
		Object generation = state(snackbar).get(SnackbarState.GENERATION__PROP);
		assertTrue("The client is told a generation: " + generation, generation instanceof Number);
		return ((Number) generation).intValue();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> state(ReactSnackbarControl snackbar) {
		try {
			return (Map<String, Object>) JSON.fromString(snackbar.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private ReactSnackbarControl snackbar() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		return new ReactSnackbarControl(context, "", Variant.SUCCESS, () -> _dismissed++);
	}

}
