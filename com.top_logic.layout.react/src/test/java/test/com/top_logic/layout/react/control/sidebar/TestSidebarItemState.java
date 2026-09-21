/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.sidebar;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.sidebar.CommandItem;
import com.top_logic.layout.react.control.sidebar.ExecuteCommandArguments;
import com.top_logic.layout.react.control.sidebar.ReactSidebarControl;
import com.top_logic.layout.react.control.sidebar.SidebarItem;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a {@link CommandItem} carries the executability of the command it hosts: to the
 * display, and to the execution the display is an offer of.
 *
 * <p>
 * The item follows a command that becomes invisible or unexecutable while the sidebar stands. What
 * the user is shown must therefore be re-sent when the item's state changes, and an item shown out
 * of reach - or not shown at all - must refuse its action: the command is addressable by anything
 * that can talk to the server, so the display alone does not withhold it.
 * </p>
 */
public class TestSidebarItemState extends TestCase {

	/** The wire name of {@link CommandItem#isHidden()}. */
	private static final String HIDDEN = "hidden";

	/** The wire name of {@link CommandItem#isDisabled()}. */
	private static final String DISABLED = "disabled";

	/** The state field holding the serialized sidebar items. */
	private static final String ITEMS = "items";

	/** The state field holding an item's id. */
	private static final String ID = "id";

	private static final String COMMAND_ID = "command";

	private int _executions;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_executions = 0;
	}

	/** An item in its ordinary state costs no extra field on the wire. */
	public void testAnOrdinaryItemReportsNeitherFlag() {
		Map<String, Object> state = commandItem().toStateMap();

		assertNull("An item that is shown does not report itself as hidden.", state.get(HIDDEN));
		assertNull("An item that can be activated does not report itself as disabled.",
			state.get(DISABLED));
	}

	/** Both flags reach the display once set. */
	public void testTheFlagsAreReported() {
		CommandItem item = commandItem();
		item.setHidden(true);
		item.setDisabled(true);

		Map<String, Object> state = item.toStateMap();

		assertEquals(Boolean.TRUE, state.get(HIDDEN));
		assertEquals(Boolean.TRUE, state.get(DISABLED));
	}

	/**
	 * A state change applied to an item reaches the client with the item list it is part of.
	 */
	public void testARefreshSendsTheChangedItem() {
		CommandItem item = commandItem();
		ReactSidebarControl sidebar = sidebar(item);

		assertNull("The item starts out activatable.", commandState(sidebar).get(DISABLED));

		item.setDisabled(true);
		sidebar.refreshItems();

		assertEquals("The item was disabled, but the client still holds the state it was sent "
			+ "when the sidebar was built.", Boolean.TRUE, commandState(sidebar).get(DISABLED));
	}

	/** An item shown out of reach does not run its action, however its command is addressed. */
	public void testADisabledItemRefusesItsAction() {
		CommandItem item = commandItem();
		ReactSidebarControl sidebar = sidebar(item);
		item.setDisabled(true);
		sidebar.refreshItems();

		HandlerResult result = execute(sidebar);

		assertFalse("A command the user interface shows out of reach was run.", result.isSuccess());
		assertEquals(0, _executions);
	}

	/** An item that is not shown at all does not run its action either. */
	public void testAHiddenItemRefusesItsAction() {
		CommandItem item = commandItem();
		ReactSidebarControl sidebar = sidebar(item);
		item.setHidden(true);
		sidebar.refreshItems();

		HandlerResult result = execute(sidebar);

		assertFalse("A command the user interface does not offer was run.", result.isSuccess());
		assertEquals(0, _executions);
	}

	/** The item as it is offered runs its action. */
	public void testAnOfferedItemRunsItsAction() {
		ReactSidebarControl sidebar = sidebar(commandItem());

		HandlerResult result = execute(sidebar);

		assertTrue(result.isSuccess());
		assertEquals(1, _executions);
	}

	private HandlerResult execute(ReactSidebarControl sidebar) {
		return sidebar.executeCommand(ReactSidebarControl.EXECUTE_COMMAND_COMMAND,
			Map.of(ExecuteCommandArguments.ITEM_ID, COMMAND_ID));
	}

	private CommandItem commandItem() {
		return new CommandItem(COMMAND_ID, "Command", null, context -> {
			_executions++;
			return HandlerResult.DEFAULT_RESULT;
		});
	}

	private static ReactSidebarControl sidebar(CommandItem item) {
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		List<SidebarItem> items = List.of(item);
		return new ReactSidebarControl(context, items, null, false, null, null);
	}

	/**
	 * The state of the command item as the client holds it.
	 */
	private static Map<String, Object> commandState(ReactSidebarControl sidebar) {
		for (Object item : items(sidebar)) {
			@SuppressWarnings("unchecked")
			Map<String, Object> state = (Map<String, Object>) item;
			if (COMMAND_ID.equals(state.get(ID))) {
				return state;
			}
		}
		throw new AssertionError("The sidebar does not report its command item to the client.");
	}

	private static List<?> items(ReactSidebarControl sidebar) {
		try {
			@SuppressWarnings("unchecked")
			Map<String, Object> state = (Map<String, Object>) JSON.fromString(sidebar.stateAsJSON());
			return (List<?>) state.get(ITEMS);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("The state sent to the client is not JSON.", ex);
		}
	}

}
