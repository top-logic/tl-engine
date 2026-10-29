/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.selection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.react.control.selection.ItemSelection;
import com.top_logic.table.SelectionMode;

/**
 * Test case for {@link ItemSelection}.
 */
public class TestItemSelection extends TestCase {

	private int _nextKey;

	private List<List<String>> _published;

	private List<Set<Object>> _reported;

	private ItemSelection _selection;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_nextKey = 0;
		_published = new ArrayList<>();
		_reported = new ArrayList<>();
		_selection = new ItemSelection(() -> "k" + _nextKey++, _published::add);
		_selection.setListener(selection -> _reported.add(Set.copyOf(selection)));
		_selection.setItems(List.of("a", "b", "c", "d", "e"));
	}

	public void testKeysStableAcrossUpdates() {
		String keyOfB = key("b");
		String keyOfD = key("d");
		_selection.setItems(List.of("d", "x", "b"));
		assertEquals(keyOfB, key("b"));
		assertEquals(keyOfD, key("d"));
		assertNull(key("a"));
		assertFalse(_selection.displays("a"));
		assertTrue(_selection.displays("x"));
		assertEquals("x", _selection.item(key("x")));
		assertNull(_selection.item("unknown"));
		assertEquals(List.of("d", "x", "b"), _selection.getItems());
	}

	public void testSingleReplaces() {
		select("b", false, false);
		assertSelection("b");
		select("d", false, false);
		assertSelection("d");
		assertEquals(List.of(key("d")), _published.get(_published.size() - 1));
		assertEquals(2, _reported.size());
		assertEquals(Set.of("d"), _reported.get(1));
	}

	public void testSingleIgnoresRangeAndToggleAddsNothing() {
		select("b", false, false);
		select("d", true, true);
		assertSelection("d");
	}

	public void testSingleToggleOfSelectedClears() {
		select("b", false, false);
		select("b", true, false);
		assertSelection();
		assertEquals(Set.of(), _reported.get(1));
	}

	public void testMultiToggleAddsAndRemoves() {
		_selection.setMode(SelectionMode.MULTI);
		select("b", false, false);
		select("d", true, false);
		assertSelection("b", "d");
		select("b", true, false);
		assertSelection("d");
	}

	public void testMultiRangeFromAnchor() {
		_selection.setMode(SelectionMode.MULTI);
		select("b", false, false);
		select("d", false, true);
		assertSelection("b", "c", "d");
		// The anchor stays at the item selected last without a range.
		select("a", false, true);
		assertSelection("b", "c", "d", "a");
	}

	public void testMultiRangeKeepsToggledSelection() {
		_selection.setMode(SelectionMode.MULTI);
		select("a", false, false);
		select("d", true, false);
		select("e", false, true);
		assertSelection("a", "d", "e");
	}

	public void testMultiRangeWithoutAnchorReplaces() {
		_selection.setMode(SelectionMode.MULTI);
		select("c", false, true);
		assertSelection("c");
	}

	public void testMultiPlainClickReplaces() {
		_selection.setMode(SelectionMode.MULTI);
		select("a", false, false);
		select("c", true, false);
		select("e", false, false);
		assertSelection("e");
	}

	public void testUnknownKeyIsNoOp() {
		select("b", false, false);
		int published = _published.size();
		assertFalse(_selection.select("unknown", false, false));
		assertSelection("b");
		assertEquals(1, _reported.size());
		assertEquals(published, _published.size());
	}

	public void testVetoRestoresSelection() {
		_selection.setMode(SelectionMode.MULTI);
		select("a", false, false);
		select("b", true, false);
		RuntimeException veto = new RuntimeException("veto");
		_selection.setListener(selection -> {
			throw veto;
		});
		try {
			select("d", false, false);
			fail("Veto expected.");
		} catch (RuntimeException ex) {
			assertSame(veto, ex);
		}
		assertSelection("a", "b");
		assertEquals(List.of(key("a"), key("b")), _published.get(_published.size() - 1));

		// The refused selection did not move the anchor.
		_selection.setListener(null);
		select("c", false, true);
		assertSelection("a", "b", "c");
	}

	public void testUndisplayedSelectionKept() {
		_selection.setMode(SelectionMode.MULTI);
		select("a", false, false);
		select("c", true, false);
		_selection.setItems(List.of("c", "d"));
		assertSelection("a", "c");
		assertEquals(List.of(key("c")), _selection.selectedKeys());
		assertEquals(List.of("c"), _selection.displayedSelection());

		_selection.setItems(List.of("a", "b", "c"));
		assertEquals(List.of(key("a"), key("c")), _selection.selectedKeys());
	}

	public void testProgrammaticSelectionNotReported() {
		_selection.setSelection(List.of("e", "x"));
		assertSelection("e", "x");
		assertTrue(_reported.isEmpty());
		assertEquals(List.of(key("e")), _published.get(_published.size() - 1));
	}

	public void testDisplayedSelectionInDisplayOrder() {
		_selection.setSelection(List.of("d", "a"));
		assertEquals(List.of("a", "d"), _selection.displayedSelection());
	}

	public void testItemsBetween() {
		assertEquals(List.of("b", "c", "d"), _selection.itemsBetween("d", "b"));
		assertEquals(List.of("c"), _selection.itemsBetween("c", "c"));
	}

	private String key(Object item) {
		return _selection.keyOf(item);
	}

	private void select(Object item, boolean toggle, boolean range) {
		assertTrue(_selection.select(key(item), toggle, range));
	}

	private void assertSelection(Object... expected) {
		assertEquals(List.of(expected), new ArrayList<>(_selection.getSelection()));
	}

}
