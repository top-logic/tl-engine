/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ColumnLockArguments;
import com.top_logic.layout.react.control.table.ColumnReorderArguments;
import com.top_logic.layout.react.control.table.ColumnVisibleArguments;
import com.top_logic.layout.react.control.table.GroupArguments;
import com.top_logic.layout.react.control.table.ReactColumnSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.ColumnOption;

/**
 * Test of {@link ReactColumnSelectControl}: locking keeps the locked columns as a block at the start, hiding keeps a
 * column in place, a locked column is always displayed, the result names the columns the table freezes, and the rows
 * are grouped by one column, also a hidden one, unless the table is a tree.
 */
public class TestReactColumnSelect extends TestCase {

	public void testOpensWithTheLockedColumnsAsBlock() {
		ReactColumnSelectControl control = control(Set.of("c"));

		assertEquals(List.of("c", "a", "b", "d", "e"), control.order());
		assertEquals(List.of("c"), control.lockedColumns());
	}

	public void testLockMovesToTheEndOfTheBlock() {
		ReactColumnSelectControl control = control(Set.of("a"));

		assertTrue(lock(control, "d", true));

		assertEquals(List.of("a", "d", "b", "c", "e"), control.order());
		assertEquals(List.of("a", "d"), control.lockedColumns());
	}

	public void testUnlockMovesDirectlyBehindTheBlock() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(lock(control, "a", false));

		assertEquals(List.of("b", "a", "c", "d", "e"), control.order());
		assertEquals(List.of("b"), control.lockedColumns());
	}

	public void testReleasingAReleasedColumnKeepsTheOrder() {
		ReactColumnSelectControl control = control(Set.of("a"));

		assertTrue(lock(control, "d", false));

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(List.of("a"), control.lockedColumns());
	}

	public void testLockingALockedColumnKeepsTheOrder() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(lock(control, "a", true));

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(List.of("a", "b"), control.lockedColumns());
	}

	public void testReorderIntoTheBlockLocks() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(reorder(control, "d", 1));

		assertEquals(List.of("a", "d", "b", "c", "e"), control.order());
		assertEquals(List.of("a", "d", "b"), control.lockedColumns());
	}

	public void testReorderOutOfTheBlockUnlocks() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(reorder(control, "a", 3));

		assertEquals(List.of("b", "c", "d", "a", "e"), control.order());
		assertEquals(List.of("b"), control.lockedColumns());
	}

	public void testReorderToTheLineKeepsTheState() {
		// The position directly at the line keeps what the moved column was: a locked one ends the block, an unlocked
		// one starts the rest.
		ReactColumnSelectControl locked = control(Set.of("a", "b"));
		assertTrue(reorder(locked, "a", 1));
		assertEquals(List.of("b", "a"), locked.lockedColumns());

		ReactColumnSelectControl unlocked = control(Set.of("a", "b"));
		assertTrue(reorder(unlocked, "e", 2));
		assertEquals(List.of("a", "b"), unlocked.lockedColumns());
		assertEquals(List.of("a", "b", "e", "c", "d"), unlocked.order());
	}

	public void testHidingALockedColumnReleasesIt() {
		ReactColumnSelectControl control = control(Set.of("a", "b", "c"));

		assertTrue(visible(control, "b", false));

		assertEquals(List.of("a", "c", "b", "d", "e"), control.order());
		assertEquals(List.of("a", "c"), control.lockedColumns());

		// Shown again, the column stays released: no lock waited behind the closed eye.
		assertTrue(visible(control, "b", true));
		assertEquals(List.of("a", "c"), control.lockedColumns());
	}

	public void testLockingAHiddenColumnShowsIt() {
		ReactColumnSelectControl control = control(Set.of("a"));
		assertTrue(visible(control, "d", false));

		assertTrue(lock(control, "d", true));

		assertEquals(List.of("a", "d", "b", "c", "e"), control.order());
		assertEquals(List.of("a", "d"), control.lockedColumns());
		assertTrue(control.visibleColumns().contains("d"));
	}

	public void testDroppingAHiddenColumnIntoTheBlockShowsIt() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));
		assertTrue(visible(control, "d", false));

		assertTrue(reorder(control, "d", 1));

		assertEquals(List.of("a", "d", "b"), control.lockedColumns());
		assertTrue(control.visibleColumns().contains("d"));
	}

	public void testDroppingAHiddenColumnBehindTheBlockKeepsItHidden() {
		ReactColumnSelectControl control = control(Set.of("a"));
		assertTrue(visible(control, "d", false));

		assertTrue(reorder(control, "d", 2));

		assertEquals(List.of("a", "b", "d", "c", "e"), control.order());
		assertFalse(control.visibleColumns().contains("d"));
	}

	public void testTheLastVisibleColumnStays() {
		ReactColumnSelectControl control = new ReactColumnSelectControl(context(),
			List.of(option("a", true), option("b", false)), Set.of("a"), null, true);

		visible(control, "a", false);

		assertEquals(List.of("a"), control.visibleColumns());
		assertEquals(List.of("a"), control.lockedColumns());
	}

	public void testUnknownColumnChangesNothing() {
		ReactColumnSelectControl control = control(Set.of("a"));

		lock(control, "x", true);

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(List.of("a"), control.lockedColumns());
	}

	public void testOpensWithTheGrouping() {
		ReactColumnSelectControl control = control(Set.of(), "b", true);

		assertEquals("b", control.groupedColumn());
		assertEquals(Boolean.TRUE, control.scriptingScalarState().get(ReactColumnSelectControl.GROUPABLE));
	}

	public void testGroupingMovesToTheChosenColumn() {
		ReactColumnSelectControl control = control(Set.of(), "b", true);

		assertTrue(group(control, "c"));

		assertEquals("c", control.groupedColumn());
	}

	public void testChoosingNoColumnRemovesTheGrouping() {
		ReactColumnSelectControl control = control(Set.of(), "b", true);

		assertTrue(group(control, ""));

		assertNull(control.groupedColumn());
	}

	public void testAHiddenColumnCanBeGroupedBy() {
		ReactColumnSelectControl control = control(Set.of(), null, true);
		assertTrue(visible(control, "d", false));

		assertTrue(group(control, "d"));

		assertEquals("d", control.groupedColumn());
		assertFalse("Grouping does not display the column.", control.visibleColumns().contains("d"));
	}

	public void testGroupingByAnUnknownColumnChangesNothing() {
		ReactColumnSelectControl control = control(Set.of(), "b", true);

		group(control, "x");

		assertEquals("b", control.groupedColumn());
	}

	public void testATreeOffersNoGrouping() {
		ReactColumnSelectControl control = control(Set.of(), null, false);
		assertEquals(Boolean.FALSE, control.scriptingScalarState().get(ReactColumnSelectControl.GROUPABLE));

		group(control, "a");

		assertNull("A tree cannot be grouped.", control.groupedColumn());
	}

	private static boolean group(ReactColumnSelectControl control, String column) {
		return control.executeCommand(ReactColumnSelectControl.CMD_GROUP, Map.of(GroupArguments.COLUMN, column))
			.isSuccess();
	}

	private static boolean lock(ReactColumnSelectControl control, String column, boolean locked) {
		return control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_LOCK,
			Map.of(ColumnLockArguments.COLUMN, column, ColumnLockArguments.LOCKED, Boolean.valueOf(locked))).isSuccess();
	}

	private static boolean reorder(ReactColumnSelectControl control, String column, int target) {
		return control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_REORDER,
			Map.of(ColumnReorderArguments.COLUMN, column, ColumnReorderArguments.TARGET_INDEX, Integer.valueOf(target)))
			.isSuccess();
	}

	private static boolean visible(ReactColumnSelectControl control, String column, boolean visible) {
		return control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_VISIBLE,
			Map.of(ColumnVisibleArguments.COLUMN, column, ColumnVisibleArguments.VISIBLE, Boolean.valueOf(visible)))
			.isSuccess();
	}

	/** Five visible columns a to e, the rows not grouped. */
	private static ReactColumnSelectControl control(Set<String> locked) {
		return control(locked, null, true);
	}

	/** Five visible columns a to e, grouped by the given column. */
	private static ReactColumnSelectControl control(Set<String> locked, String grouped, boolean groupable) {
		return new ReactColumnSelectControl(context(),
			List.of(option("a", true), option("b", true), option("c", true), option("d", true), option("e", true)),
			locked, grouped, groupable);
	}

	private static ColumnOption option(String name, boolean visible) {
		return new ColumnOption(name, ResKey.text(name.toUpperCase()), visible);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/** Suite requiring the resources the entries resolve their labels with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestReactColumnSelect.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
