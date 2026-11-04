/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
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
import com.top_logic.layout.react.control.table.ColumnMoveArguments;
import com.top_logic.layout.react.control.table.ColumnVisibleArguments;
import com.top_logic.layout.react.control.table.GroupArguments;
import com.top_logic.layout.react.control.table.ReactColumnSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.ColumnOption;

/**
 * Test of {@link ReactColumnSelectControl}: locking keeps the locked columns as a block at the start, hiding keeps a
 * column in place, a locked column is always displayed, a move changes the section (frozen, movable, hidden) and the
 * position of a column in one command, the result names the columns the table freezes, the rows are grouped by one
 * column, also a hidden one, unless the table is a tree, and every command counts in the revision.
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

	public void testReorderIntoTheBlockStaysOutside() {
		// Dragging stays within the own block: a drop into the frozen block lands at its end, the column stays released.
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(move(control, "d", 1, false, true));

		assertEquals(List.of("a", "b", "d", "c", "e"), control.order());
		assertEquals(List.of("a", "b"), control.lockedColumns());
	}

	public void testReorderOutOfTheBlockStaysInside() {
		// A frozen column dropped behind the block lands at the end of the block, it stays frozen.
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(move(control, "a", 3, true, true));

		assertEquals(List.of("b", "a", "c", "d", "e"), control.order());
		assertEquals(List.of("b", "a"), control.lockedColumns());
	}

	public void testReorderWithinTheBlockMoves() {
		ReactColumnSelectControl control = control(Set.of("a", "b", "c"));

		assertTrue(move(control, "a", 2, true, true));

		assertEquals(List.of("b", "c", "a", "d", "e"), control.order());
		assertEquals(List.of("b", "c", "a"), control.lockedColumns());
	}

	public void testReorderToTheLineKeepsTheState() {
		// The position directly at the line keeps what the moved column was: a locked one ends the block, an unlocked
		// one starts the rest.
		ReactColumnSelectControl locked = control(Set.of("a", "b"));
		assertTrue(move(locked, "a", 1, true, true));
		assertEquals(List.of("b", "a"), locked.lockedColumns());

		ReactColumnSelectControl unlocked = control(Set.of("a", "b"));
		assertTrue(move(unlocked, "e", 2, false, true));
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

	/** Tests that a movable column dropped into the frozen block becomes frozen, at the dropped place. */
	public void testMoveMovableToFrozenAtTheDroppedPlace() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));

		assertTrue(move(control, "c", 1, true, true));

		assertEquals(List.of("a", "c", "b", "d", "e"), control.order());
		assertEquals(List.of("a", "c", "b"), control.lockedColumns());
	}

	/** Tests that a frozen column dropped behind the block becomes movable, at the dropped place. */
	public void testMoveFrozenToMovableAtTheDroppedPlace() {
		ReactColumnSelectControl control = control(Set.of("a", "b", "c"));

		assertTrue(move(control, "a", 4, false, true));

		assertEquals(List.of("b", "c", "d", "e", "a"), control.order());
		assertEquals(List.of("b", "c"), control.lockedColumns());
	}

	/** Tests that a hidden column dropped among the movable ones is displayed at the target index. */
	public void testMoveHiddenToVisibleAtTheTargetIndex() {
		ReactColumnSelectControl control = control(Set.of("a"));
		assertTrue(visible(control, "b", false));
		assertTrue(visible(control, "d", false));
		assertEquals(List.of("a", "c", "e"), control.visibleColumns());

		assertTrue(move(control, "d", 2, false, true));

		assertEquals(List.of("a", "b", "d", "c", "e"), control.order());
		assertEquals(List.of("a", "d", "c", "e"), control.visibleColumns());
		assertEquals(List.of("a"), control.lockedColumns());
	}

	/** Tests that a hidden column dropped into the frozen block is displayed and frozen. */
	public void testMoveHiddenToFrozen() {
		ReactColumnSelectControl control = control(Set.of("a", "b"));
		assertTrue(visible(control, "d", false));

		assertTrue(move(control, "d", 0, true, true));

		assertEquals(List.of("d", "a", "b", "c", "e"), control.order());
		assertEquals(List.of("d", "a", "b"), control.lockedColumns());
		assertTrue(control.visibleColumns().contains("d"));
	}

	/** Tests that hiding by a move keeps the place of the column and ignores the target index. */
	public void testMoveVisibleToHiddenKeepsThePlace() {
		ReactColumnSelectControl control = control(Set.of("a"));

		assertTrue(move(control, "c", 0, false, false));

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(List.of("a", "b", "d", "e"), control.visibleColumns());
		assertEquals(List.of("a"), control.lockedColumns());
	}

	/** Tests that a frozen column hidden by a move is released and rests directly behind the block. */
	public void testMoveFrozenToHiddenIsReleased() {
		ReactColumnSelectControl control = control(Set.of("a", "b", "c"));

		assertTrue(move(control, "a", 0, true, false));

		assertEquals(List.of("b", "c", "a", "d", "e"), control.order());
		assertEquals(List.of("b", "c"), control.lockedColumns());
		assertFalse(control.visibleColumns().contains("a"));
	}

	/** Tests that the last displayed column is not hidden by a move, and that the refusal reports nothing. */
	public void testMoveTheLastVisibleToHiddenIsRefused() {
		ReactColumnSelectControl control = new ReactColumnSelectControl(context(),
			List.of(option("a", true), option("b", false)), Set.of(), null, true);
		int[] changes = { 0 };
		control.setOnChange(() -> changes[0]++);

		assertTrue(move(control, "a", 1, false, false));

		assertEquals(List.of("a"), control.visibleColumns());
		assertEquals(List.of("a", "b"), control.order());
		assertEquals(0, changes[0]);
	}

	/** Tests that a target index beyond the list is clamped. */
	public void testMoveTargetIndexIsClamped() {
		ReactColumnSelectControl control = control(Set.of("a"));

		assertTrue(move(control, "b", 99, false, true));
		assertEquals(List.of("a", "c", "d", "e", "b"), control.order());

		assertTrue(move(control, "e", -5, true, true));
		assertEquals(List.of("e", "a", "c", "d", "b"), control.order());
		assertEquals(List.of("e", "a"), control.lockedColumns());
	}

	/**
	 * Tests that a target index that is no number is rejected and leaves the columns as they were, and that the
	 * rejected command counts in the revision all the same: the client counts every command it sent.
	 */
	public void testNonIntegerTargetIndexIsRejected() {
		ReactColumnSelectControl control = control(Set.of("a"));

		assertFalse(control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_MOVE, Map.of(
			ColumnMoveArguments.COLUMN, "c",
			ColumnMoveArguments.TARGET_INDEX, "x",
			ColumnMoveArguments.LOCKED, Boolean.FALSE,
			ColumnMoveArguments.VISIBLE, Boolean.TRUE)).isSuccess());

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(1, revision(control));
	}

	/** Tests that a command whose arguments are incomplete counts in the revision, exactly once. */
	public void testMissingArgumentsCountInTheRevision() {
		ReactColumnSelectControl control = control(Set.of("a"));

		assertFalse(control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_MOVE,
			Map.of(ColumnMoveArguments.COLUMN, "c")).isSuccess());

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(1, revision(control));
	}

	/**
	 * Tests that a command that ends with an exception counts in the revision exactly once - here the listener the
	 * handler tells throws, after the change is pushed.
	 */
	public void testFailingCommandCountsInTheRevisionOnce() {
		ReactColumnSelectControl control = control(Set.of("a"));
		control.setOnChange(() -> {
			throw new IllegalStateException("Listener fails.");
		});

		assertFalse(visible(control, "c", false));

		assertEquals(1, revision(control));
		assertEquals(List.of("a", "b", "d", "e"), control.visibleColumns());

		// The next command counts on from there.
		assertFalse(lock(control, "d", true));
		assertEquals(2, revision(control));
	}

	/** Tests that hiding a hidden column by a move keeps the order, and counts in the revision. */
	public void testMoveHiddenToHiddenKeepsTheOrder() {
		ReactColumnSelectControl control = control(Set.of("a"));
		assertTrue(visible(control, "c", false));
		assertEquals(1, revision(control));

		assertTrue(move(control, "c", 4, false, false));

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
		assertEquals(List.of("a", "b", "d", "e"), control.visibleColumns());
		assertEquals(List.of("a"), control.lockedColumns());
		assertEquals(2, revision(control));
	}

	/** Tests that the revision starts at zero and that each command of the column selection counts exactly once. */
	public void testEveryCommandCountsInTheRevision() {
		ReactColumnSelectControl control = control(Set.of("a", "b"), "c", true);
		assertEquals(0, revision(control));

		// Every variant of columnMove.
		assertTrue(move(control, "d", 0, true, true));
		assertEquals(1, revision(control));
		assertTrue(move(control, "d", 4, false, true));
		assertEquals(2, revision(control));
		assertTrue(move(control, "e", 0, false, false));
		assertEquals(3, revision(control));
		assertTrue(move(control, "e", 1, false, true));
		assertEquals(4, revision(control));
		assertTrue(move(control, "e", 0, true, true));
		assertEquals(5, revision(control));
		assertTrue(move(control, "a", 0, true, false));
		assertEquals(6, revision(control));

		// The other commands.
		assertTrue(visible(control, "b", false));
		assertEquals(7, revision(control));
		assertTrue(lock(control, "c", true));
		assertEquals(8, revision(control));
		assertTrue(group(control, "d"));
		assertEquals(9, revision(control));
	}

	/**
	 * Tests that a command the control refuses or that changes nothing counts as well: the client counts every command
	 * it sent, and must see the answer to each.
	 */
	public void testRefusedCommandsCountInTheRevision() {
		ReactColumnSelectControl control = control(Set.of("a"), "b", true);

		// Unknown column.
		assertTrue(move(control, "x", 0, false, true));
		assertEquals(1, revision(control));
		assertTrue(visible(control, "x", false));
		assertEquals(2, revision(control));
		assertTrue(lock(control, "x", true));
		assertEquals(3, revision(control));
		assertTrue(group(control, "x"));
		assertEquals(4, revision(control));

		// No change: a locked column locked again, a released one released again, the grouped column grouped again.
		assertTrue(lock(control, "a", true));
		assertEquals(5, revision(control));
		assertTrue(lock(control, "d", false));
		assertEquals(6, revision(control));
		assertTrue(group(control, "b"));
		assertEquals(7, revision(control));

		// A drop where the column already is.
		assertTrue(move(control, "c", 2, false, true));
		assertEquals(8, revision(control));

		assertEquals(List.of("a", "b", "c", "d", "e"), control.order());
	}

	/** Tests that the refusal to hide the last displayed column counts, by either command. */
	public void testRefusedLastVisibleCountsInTheRevision() {
		ReactColumnSelectControl control = new ReactColumnSelectControl(context(),
			List.of(option("a", true), option("b", false)), Set.of(), null, true);

		assertTrue(visible(control, "a", false));
		assertEquals(1, revision(control));
		assertTrue(move(control, "a", 0, false, false));
		assertEquals(2, revision(control));
	}

	/** Tests that a grouping a tree cannot have counts as well. */
	public void testGroupingATreeCountsInTheRevision() {
		ReactColumnSelectControl control = control(Set.of(), null, false);

		assertTrue(group(control, "a"));

		assertEquals(1, revision(control));
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

	/** Tests that every command that changed something reports the change, and each once. */
	public void testEveryChangeIsReported() {
		ReactColumnSelectControl control = control(Set.of("a"), "b", true);
		int[] changes = { 0 };
		control.setOnChange(() -> changes[0]++);

		assertTrue(visible(control, "d", false));
		assertEquals(1, changes[0]);
		assertTrue(lock(control, "c", true));
		assertEquals(2, changes[0]);
		assertTrue(move(control, "e", 2, false, true));
		assertEquals(3, changes[0]);
		assertTrue(group(control, "c"));
		assertEquals(4, changes[0]);
	}

	/**
	 * Tests that the report comes after the change: the owner reads the selection it is told about, and finds
	 * the changed columns there.
	 */
	public void testTheChangeIsMadeWhenItIsReported() {
		ReactColumnSelectControl control = control(Set.of(), null, true);
		List<List<String>> seen = new ArrayList<>();
		control.setOnChange(() -> seen.add(control.visibleColumns()));

		assertTrue(visible(control, "b", false));

		assertEquals(List.of(List.of("a", "c", "d", "e")), seen);
	}

	/** Tests that a command that changes nothing reports nothing: no lock moved, no column hidden, no grouping set. */
	public void testNoChangeIsNoReport() {
		ReactColumnSelectControl control = control(Set.of("a"), "b", true);
		int[] changes = { 0 };
		control.setOnChange(() -> changes[0]++);

		// Locking a locked column, releasing a released one, showing a shown one, grouping by the grouped one.
		assertTrue(lock(control, "a", true));
		assertTrue(lock(control, "d", false));
		assertTrue(visible(control, "c", true));
		assertTrue(group(control, "b"));
		// An unknown column.
		assertTrue(lock(control, "x", true));
		assertTrue(group(control, "x"));
		// A drop where the column already is.
		assertTrue(move(control, "c", 2, false, true));

		assertEquals(0, changes[0]);
	}

	/** Tests that the refusal to hide the last displayed column reports nothing. */
	public void testRefusedHidingIsNoReport() {
		ReactColumnSelectControl control = new ReactColumnSelectControl(context(),
			List.of(option("a", true), option("b", false)), Set.of(), null, true);
		int[] changes = { 0 };
		control.setOnChange(() -> changes[0]++);

		assertTrue(visible(control, "a", false));

		assertEquals(0, changes[0]);
		assertEquals(List.of("a"), control.visibleColumns());
	}

	/** Tests that a tree, which cannot be grouped, reports no grouping. */
	public void testGroupingATreeIsNoReport() {
		ReactColumnSelectControl control = control(Set.of(), null, false);
		int[] changes = { 0 };
		control.setOnChange(() -> changes[0]++);

		assertTrue(group(control, "a"));

		assertEquals(0, changes[0]);
	}

	private static boolean group(ReactColumnSelectControl control, String column) {
		return control.executeCommand(ReactColumnSelectControl.CMD_GROUP, Map.of(GroupArguments.COLUMN, column))
			.isSuccess();
	}

	private static boolean lock(ReactColumnSelectControl control, String column, boolean locked) {
		return control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_LOCK,
			Map.of(ColumnLockArguments.COLUMN, column, ColumnLockArguments.LOCKED, Boolean.valueOf(locked))).isSuccess();
	}

	private static boolean move(ReactColumnSelectControl control, String column, int target, boolean locked,
			boolean visible) {
		return control.executeCommand(ReactColumnSelectControl.CMD_COLUMN_MOVE, Map.of(
			ColumnMoveArguments.COLUMN, column,
			ColumnMoveArguments.TARGET_INDEX, Integer.valueOf(target),
			ColumnMoveArguments.LOCKED, Boolean.valueOf(locked),
			ColumnMoveArguments.VISIBLE, Boolean.valueOf(visible)))
			.isSuccess();
	}

	/** The revision the control pushes: the number of commands it has handled. */
	private static int revision(ReactColumnSelectControl control) {
		return ((Integer) control.scriptingScalarState().get(ReactColumnSelectControl.REVISION)).intValue();
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
