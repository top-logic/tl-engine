/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactGridControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl.Item;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.layout.SelectItemArguments;
import com.top_logic.layout.react.control.selection.SelectionModifiers;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.SelectionMode;

/**
 * Tests the selectable mode of a {@link ReactLayoutControl}: the selection state it hands its React
 * component and the {@link ReactLayoutControl#CMD_SELECT_ITEM} command.
 */
public class TestReactLayoutControlSelection extends TestCase {

	/**
	 * A grid that hands out its own state, which is otherwise visible to the control only.
	 */
	private static final class GridProbe extends ReactGridControl {

		GridProbe(ReactContext context) {
			super(context, "16rem", null, StackGap.DEFAULT, List.of());
		}

		Object state(String key) {
			return getState(key);
		}

		Object children() {
			return getState(CHILDREN);
		}
	}

	private ReactContext _context;

	private GridProbe _grid;

	private List<Set<Object>> _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_grid = new GridProbe(_context);
		_reported = new ArrayList<>();
	}

	/**
	 * A container whose children are set without objects publishes no selection state and keeps its
	 * selection commands out of the headless projection.
	 */
	public void testNotSelectablePublishesNoSelection() {
		_grid.setChildren(List.of(text("a"), text("b")));

		assertFalse(_grid.isSelectable());
		for (String key : List.of(ReactLayoutControl.SELECTABLE, ReactLayoutControl.ITEM_KEYS,
			ReactLayoutControl.SELECTED, ReactLayoutControl.MULTI_SELECT)) {
			assertNull("A container that is not selectable must not send '" + key + "'.", _grid.state(key));
		}
		assertEquals(Set.of(), _grid.getSelection());
		assertFalse(_grid.scriptingCommands().contains(ReactLayoutControl.CMD_SELECT_ITEM));
		assertFalse(_grid.scriptingCommands().contains(ReactLayoutControl.CMD_SELECT_ITEM_BY_KEY));
	}

	/**
	 * The keys of the items' objects are published aligned with the children.
	 */
	public void testSelectableStatePublished() {
		ReactControl a = text("a");
		ReactControl b = text("b");
		_grid.setItems(List.of(new Item("A", a), new Item("B", b)));

		assertTrue(_grid.isSelectable());
		assertEquals(Boolean.TRUE, _grid.state(ReactLayoutControl.SELECTABLE));
		assertEquals(Boolean.FALSE, _grid.state(ReactLayoutControl.MULTI_SELECT));
		assertEquals(List.of(a, b), _grid.children());
		List<String> keys = itemKeys();
		assertEquals(2, keys.size());
		assertNotNull(keys.get(0));
		assertNotNull(keys.get(1));
		assertFalse(keys.get(0).equals(keys.get(1)));
		assertEquals(List.of(), _grid.state(ReactLayoutControl.SELECTED));
		assertTrue(_grid.scriptingCommands().contains(ReactLayoutControl.CMD_SELECT_ITEM));
	}

	/**
	 * The selection set from outside is published as keys, without a report to the listener.
	 */
	public void testProgrammaticSelectionPublished() {
		_grid.setItems(List.of(new Item("A", text("a")), new Item("B", text("b"))));
		_grid.setSelectionListener(_reported::add);

		_grid.setSelection(List.of("B"));

		assertEquals(List.of(itemKeys().get(1)), _grid.state(ReactLayoutControl.SELECTED));
		assertEquals(Set.of("B"), _grid.getSelection());
		assertTrue(_reported.isEmpty());
	}

	/**
	 * A click selects the item's object and reports it to the listener.
	 */
	public void testSelectItemCommandSelects() {
		_grid.setItems(List.of(new Item("A", text("a")), new Item("B", text("b"))));
		_grid.setSelectionListener(_reported::add);

		click(itemKeys().get(1), false);

		assertEquals(Set.of("B"), _grid.getSelection());
		assertEquals(List.of(Set.of("B")), _reported);
		assertEquals(List.of(itemKeys().get(1)), _grid.state(ReactLayoutControl.SELECTED));

		click(itemKeys().get(0), false);
		assertEquals(Set.of("A"), _grid.getSelection());
	}

	/**
	 * In the selection mode {@link SelectionMode#MULTI}, a toggling click adds an item to the
	 * selection and takes it out again.
	 */
	public void testMultiToggle() {
		_grid.setItems(List.of(new Item("A", text("a")), new Item("B", text("b")), new Item("C", text("c"))));
		_grid.setSelectionMode(SelectionMode.MULTI);
		_grid.setSelectionListener(_reported::add);
		assertEquals(Boolean.TRUE, _grid.state(ReactLayoutControl.MULTI_SELECT));

		click(itemKeys().get(0), false);
		click(itemKeys().get(2), true);
		assertEquals(Set.of("A", "C"), _grid.getSelection());

		click(itemKeys().get(0), true);
		assertEquals(Set.of("C"), _grid.getSelection());
		assertEquals(3, _reported.size());
	}

	/**
	 * Objects displayed before and after an update of the items keep their keys, and the selection
	 * of an object survives the update.
	 */
	public void testKeysStableAcrossItemUpdates() {
		_grid.setItems(List.of(new Item("A", text("a")), new Item("B", text("b"))));
		List<String> before = itemKeys();
		_grid.setSelection(List.of("B"));

		_grid.setItems(List.of(new Item("C", text("c")), new Item("B", text("b2")), new Item("A", text("a2"))));

		List<String> after = itemKeys();
		assertEquals(before.get(1), after.get(1));
		assertEquals(before.get(0), after.get(2));
		assertFalse(before.contains(after.get(0)));
		assertEquals(List.of(before.get(1)), _grid.state(ReactLayoutControl.SELECTED));
	}

	/**
	 * A click on a key no longer displayed changes nothing.
	 */
	public void testStaleKeyIgnored() {
		_grid.setItems(List.of(new Item("A", text("a")), new Item("B", text("b"))));
		String keyOfA = itemKeys().get(0);
		_grid.setItems(List.of(new Item("B", text("b"))));
		_grid.setSelectionListener(_reported::add);

		click(keyOfA, false);

		assertEquals(Set.of(), _grid.getSelection());
		assertTrue(_reported.isEmpty());
	}

	/**
	 * A selectable container displays its children only together with their objects.
	 */
	public void testSetChildrenRefusedWhenSelectable() {
		_grid.setItems(List.of(new Item("A", text("a"))));
		try {
			_grid.setChildren(List.of(text("x")));
			fail("Children without objects must be refused by a selectable container.");
		} catch (IllegalStateException expected) {
			// Expected.
		}
	}

	/**
	 * A stack is elided from the headless projection only while it is not selectable.
	 */
	public void testSelectableStackIsNotTransparent() {
		ReactStackControl stack = new ReactStackControl(_context, List.of());
		assertTrue(stack.scriptingTransparent());

		stack.setItems(List.of(new Item("A", text("a"))));
		assertFalse(stack.scriptingTransparent());
	}

	@SuppressWarnings("unchecked")
	private List<String> itemKeys() {
		return (List<String>) _grid.state(ReactLayoutControl.ITEM_KEYS);
	}

	private void click(String key, boolean toggle) {
		_grid.executeCommand(ReactLayoutControl.CMD_SELECT_ITEM, Map.of(
			SelectItemArguments.KEY, key,
			SelectionModifiers.TOGGLE, Boolean.valueOf(toggle),
			SelectionModifiers.RANGE, Boolean.FALSE));
	}

	private ReactControl text(String text) {
		return new ReactTextControl(_context, text);
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactLayoutControlSelection.class,
				TypeIndex.Module.INSTANCE, ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
