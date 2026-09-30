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

import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.overlay.MenuSelectItemArguments;
import com.top_logic.layout.react.control.overlay.ReactMenuControl;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests the selection of an entry of a {@link ReactMenuControl}: a disabled entry is refused with
 * its state, and the result of an offered entry's select handler is the result of the selection.
 */
public class TestReactMenuControl extends TestCase {

	private static final String ENTRY = "entry";

	private final List<String> _selected = new ArrayList<>();

	private ReactContext createTestContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private ReactMenuControl createMenu(MenuEntry entry, HandlerResult selectResult) {
		return new ReactMenuControl(createTestContext(), null, List.of(entry),
			itemId -> {
				_selected.add(itemId);
				return selectResult;
			},
			() -> {
				// Not observed.
			});
	}

	private static HandlerResult select(ReactMenuControl menu, String itemId) {
		return menu.executeClientCommand(ReactMenuControl.SELECT_ITEM_COMMAND,
			Map.of(MenuSelectItemArguments.ITEM_ID, itemId));
	}

	/**
	 * A disabled entry is not selected, whatever the client sends, and the refusal carries the
	 * reason of the entry's state.
	 */
	public void testADisabledEntryIsRefusedWithItsReason() {
		ResKey reason = ResKey.text("Only for open tickets.");
		ReactMenuControl menu = createMenu(
			MenuEntry.item(ENTRY, "Close", null, ExecutableState.createDisabledState(reason), null, false),
			HandlerResult.DEFAULT_RESULT);

		HandlerResult result = select(menu, ENTRY);

		assertEquals("A disabled entry must not be selected.", List.of(), _selected);
		assertFalse(result.isSuccess());
		assertEquals("A refusal is no malfunction.", ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals("The user learns why.", reason, result.getErrorMessage());
	}

	/** The result of the select handler - a failure included - is the result of the selection. */
	public void testTheSelectHandlersResultIsReturned() {
		HandlerResult failure = HandlerResult.error(ResKey.text("Storage is full."));
		ReactMenuControl menu = createMenu(MenuEntry.item(ENTRY, "Save"), failure);

		HandlerResult result = select(menu, ENTRY);

		assertEquals(List.of(ENTRY), _selected);
		assertSame("The selection's result must not be dropped.", failure, result);
	}

	/** An offered entry is selected, and its success is the result of the selection. */
	public void testAnOfferedEntryIsSelected() {
		ReactMenuControl menu = createMenu(MenuEntry.item(ENTRY, "Save"), HandlerResult.DEFAULT_RESULT);

		HandlerResult result = select(menu, ENTRY);

		assertEquals(List.of(ENTRY), _selected);
		assertTrue(result.isSuccess());
	}

}
