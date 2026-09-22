/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.ReactDialogControl;
import com.top_logic.layout.react.control.overlay.ReactDialogManagerControl;
import com.top_logic.layout.react.control.overlay.ReactWindowControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactDialogManagerControl} keeps a dialog on screen that its opener has
 * marked as not {@link DialogHandle#isClosable() closable}.
 *
 * <p>
 * A dialog hosting work that is still running must not disappear under that work: neither Escape,
 * nor the close button, nor a close from the application, nor the cascade of closing a dialog
 * below it takes it off screen before its opener allows that again.
 * </p>
 */
public class TestReactDialogManagerControl extends TestCase {

	private ReactContext _context;

	private ReactDialogManagerControl _manager;

	private List<String> _closed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = context();
		_manager = new ReactDialogManagerControl(_context);
		_closed = new ArrayList<>();
	}

	/** Closing the topmost dialog takes it off the stack and reports its result. */
	public void testTheTopDialogIsClosedWithItsResult() {
		open("a");

		_manager.closeTopDialog(DialogResult.ok(null));

		assertEquals(List.of("a:ok"), _closed);
		assertEquals(0, openDialogs());
	}

	/** A dialog that is not closable survives every close addressed to it. */
	public void testASuspendedDialogStaysOpen() {
		DialogHandle dialog = open("a");
		dialog.setClosable(false);

		_manager.closeTopDialog(DialogResult.cancelled());
		dialog.close(DialogResult.ok(null));

		assertEquals("A dialog that cannot be closed reports no result.", List.of(), _closed);
		assertEquals(1, openDialogs());
	}

	/** A dialog is only closed together with everything above it, so a suspended dialog holds the one below it open, too. */
	public void testADialogUnderASuspendedOneStaysOpen() {
		DialogHandle below = open("below");
		DialogHandle suspended = open("suspended");
		suspended.setClosable(false);

		below.close(DialogResult.ok(null));
		_manager.closeTopDialog(DialogResult.cancelled());

		assertEquals("Neither the suspended dialog nor the one it covers is closed.", List.of(), _closed);
		assertEquals(2, openDialogs());
	}

	/** Revealing a dialog closes what covers it down to the first suspended dialog. */
	public void testCloseDialogsAboveStopsAtASuspendedDialog() {
		DialogHandle bottom = open("bottom");
		DialogHandle suspended = open("suspended");
		open("top");
		suspended.setClosable(false);

		_manager.closeDialogsAbove(bottom);

		assertEquals("Closing gives up at the dialog that must stay open.", List.of("top:cancelled"), _closed);
		assertEquals(2, openDialogs());
	}

	/** A dialog closes as usual once its opener allows that again. */
	public void testADialogClosableAgainIsClosed() {
		DialogHandle dialog = open("a");
		dialog.setClosable(false);
		_manager.closeTopDialog(DialogResult.cancelled());

		dialog.setClosable(true);
		_manager.closeTopDialog(DialogResult.cancelled());

		assertEquals(List.of("a:cancelled"), _closed);
		assertEquals(0, openDialogs());
	}

	/** The dismiss the client sends on Escape or a backdrop click is ignored by a suspended dialog. */
	public void testTheDismissOfASuspendedDialogIsIgnored() {
		ReactDialogControl dialog = new ReactDialogControl(_context, true, () -> _closed.add("dismissed"));
		dialog.open();

		dialog.setClosable(false);
		dialog.executeCommand(ReactDialogControl.CLOSE_COMMAND, Map.of());
		assertEquals(List.of(), _closed);

		dialog.setClosable(true);
		dialog.executeCommand(ReactDialogControl.CLOSE_COMMAND, Map.of());
		assertEquals(List.of("dismissed"), _closed);
	}

	/** The close button of a suspended window is inactive: its command is ignored. */
	public void testTheCloseCommandOfASuspendedWindowIsIgnored() {
		ReactWindowControl window =
			new ReactWindowControl(_context, "title", DisplayDimension.px(450), () -> _closed.add("closed"));

		window.setClosable(false);
		window.executeCommand(ReactWindowControl.CLOSE_COMMAND, Map.of());
		assertEquals(List.of(), _closed);

		window.setClosable(true);
		window.executeCommand(ReactWindowControl.CLOSE_COMMAND, Map.of());
		assertEquals(List.of("closed"), _closed);
	}

	private DialogHandle open(String name) {
		ReactWindowControl window = new ReactWindowControl(_context, name, DisplayDimension.px(450), () -> {
			// The window's own close button is not exercised here.
		});
		return _manager.openDialog(true, window,
			result -> _closed.add(name + (result.isOk() ? ":ok" : ":cancelled")));
	}

	/**
	 * The number of dialogs the client is told to display.
	 */
	private int openDialogs() {
		Object dialogs = state(_manager).get(ReactDialogManagerControl.DIALOGS);
		assertTrue("The client is told a list of dialogs: " + dialogs, dialogs instanceof List<?>);
		return ((List<?>) dialogs).size();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> state(ReactDialogManagerControl manager) {
		try {
			return (Map<String, Object>) JSON.fromString(manager.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

}
