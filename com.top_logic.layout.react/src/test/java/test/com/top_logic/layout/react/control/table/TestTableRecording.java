/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.identity.ObjectIdTranslation;
import com.top_logic.layout.react.control.table.ActivateRowArguments;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.GroupArguments;
import com.top_logic.layout.react.control.table.MoveSelectionArguments;
import com.top_logic.layout.react.control.table.SelectAllArguments;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.table.Column;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a {@link TableViewControl} records the gestures on its rows with the business key of
 * the row's object in place of the row id, so that a recording replays in a table whose rows have
 * other ids and stand in another order - as in a fresh session.
 *
 * <p>
 * A recording is taken from a table showing {@link #ROWS} in their order and replayed into a table
 * showing them in reverse order.
 * </p>
 */
public class TestTableRecording extends TestCase {

	private static final List<String> ROWS = List.of("a", "b", "c", "d", "e");

	private static final String NAME = "name";

	private TableViewControl<String> _recording;

	private TableViewControl<String> _replay;

	/**
	 * Runs the test within an interaction of a session, as the naming of an object - which asks
	 * the session for script variables naming it - happens in one.
	 */
	@Override
	protected void runTest() throws Throwable {
		InteractionWithoutSession.inSession().runWithContext((ComputationEx2<Void, Throwable, RuntimeException>) () -> {
			super.runTest();
			return null;
		});
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_recording = flatTable(ROWS);
		List<String> reversed = new ArrayList<>(ROWS);
		Collections.reverse(reversed);
		_replay = flatTable(reversed);
	}

	/**
	 * A plain click is recorded with the key of the row's object and replays in a table whose rows
	 * have other ids and stand in another order.
	 */
	public void testPlainClickIsRecordedWithKey() {
		ReactCommand step = gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "b", false, false));
		assertEquals(TableViewControl.CMD_SELECT, step.getName());
		assertKey("b", ((SelectRowArguments) step).getRowId());
		assertFalse("The row ids differ between the tables.",
			idOf(_recording, "b").equals(idOf(_replay, "b")));

		assertReplayed(_replay, step);
		assertEquals(Set.of("b"), _replay.getSelectedKeys());
	}

	/**
	 * A click with {@code Ctrl} or {@code Shift} is recorded with its modifiers, click by click;
	 * replaying the clicks in order reproduces the selection in a table whose rows stand in
	 * another order.
	 */
	public void testMultiSelectionReplaysClickByClick() {
		assertReplayed(_replay, gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "b", false, false)));

		ReactCommand toggle = gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "e", true, false));
		assertTrue(((SelectRowArguments) toggle).isCtrlKey());
		assertKey("e", ((SelectRowArguments) toggle).getRowId());
		assertReplayed(_replay, toggle);
		assertEquals(Set.of("b", "e"), _replay.getSelectedKeys());

		ReactCommand range = gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "c", false, true));
		assertTrue(((SelectRowArguments) range).isShiftKey());
		assertEquals(Set.of("b", "c", "d", "e"), _recording.getSelectedKeys());
		assertReplayed(_replay, range);
		assertEquals("The range from the anchor 'e' to 'c' holds the same rows in reverse order.",
			Set.of("b", "c", "d", "e"), _replay.getSelectedKeys());
	}

	/**
	 * Select-all and keyboard navigation address no row and are recorded as they are; they replay
	 * on the rows, the cursor and the anchor the steps before them leave.
	 */
	public void testGesturesWithoutRowAreRecordedAsTheyAre() {
		TableViewControl<String> fresh = flatTable(ROWS);

		assertReplayed(fresh, gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "b", false, false)));

		ReactCommand down = gesture(_recording, TableViewControl.CMD_MOVE_SELECTION, move("down", false));
		assertEquals(TableViewControl.CMD_MOVE_SELECTION, down.getName());
		assertTrue(down instanceof MoveSelectionArguments);
		assertReplayed(fresh, down);
		assertEquals(Set.of("c"), fresh.getSelectedKeys());

		ReactCommand all = gesture(_recording, TableViewControl.CMD_SELECT_ALL, selectAll(true));
		assertEquals(TableViewControl.CMD_SELECT_ALL, all.getName());
		assertReplayed(fresh, all);
		assertEquals(Set.copyOf(ROWS), fresh.getSelectedKeys());
	}

	/** An activation is recorded with the key of the activated row. */
	public void testActivationIsRecordedWithKey() {
		List<String> activated = new ArrayList<>();
		_replay.setActivationHandler(activated::add);

		ReactCommand step = gesture(_recording, TableViewControl.CMD_ACTIVATE,
			Map.of(ActivateRowArguments.ROW_ID, idOf(_recording, "c")));
		assertKey("c", ((ActivateRowArguments) step).getRowId());

		assertReplayed(_replay, step);
		assertEquals(List.of("c"), activated);
		assertEquals(Set.of("c"), _replay.getSelectedKeys());
	}

	/**
	 * A recorded key of an object the replaying table has no row for fails with the message that
	 * the object is not displayed, instead of selecting something else.
	 */
	public void testUnresolvedKeyFails() {
		ReactCommand step = gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "b", false, false));
		TableViewControl<String> other = flatTable(List.of("a", "c"));

		assertNotDisplayed(replay(other, step));
		assertEquals("A failed replay changes nothing.", Set.of(), other.getSelectedKeys());
	}

	/** Expanding a tree row is recorded with the key of the row's object. */
	public void testExpansionIsRecordedWithKey() {
		TableViewControl<String> recording = treeTable();
		TableViewControl<String> replay = treeTable();
		assertEquals(3, replay.getView().rowCount());

		ReactCommand step = gesture(recording, TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_ID, idOf(recording, "b"), ExpandArguments.EXPANDED, Boolean.TRUE));
		assertKey("b", ((ExpandArguments) step).getRowId());
		assertTrue(((ExpandArguments) step).isExpanded());

		assertReplayed(replay, step);
		assertEquals("The children of 'b' are displayed.", 4, replay.getView().rowCount());
	}

	/**
	 * A recorded key of a row below a collapsed tree row fails with the message that the object
	 * is not displayed; the replay expands nothing and selects nothing.
	 */
	public void testKeyOfRowBelowCollapsedTreeRowFails() {
		TableViewControl<String> recording = treeTable();
		TableViewControl<String> replay = treeTable();
		gesture(recording, TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_ID, idOf(recording, "b"), ExpandArguments.EXPANDED, Boolean.TRUE));
		ReactCommand select = gesture(recording, TableViewControl.CMD_SELECT, click(recording, "b1", false, false));
		assertKey("b1", ((SelectRowArguments) select).getRowId());

		assertNotDisplayed(replay(replay, select));
		assertEquals("The replay expands nothing.", 3, replay.getView().rowCount());
		assertEquals(Set.of(), replay.getSelectedKeys());
	}

	/**
	 * A recorded key of a row inside a collapsed group fails with the message that the object is
	 * not displayed; the replay expands no group.
	 */
	public void testKeyOfRowInCollapsedGroupFails() {
		ReactCommand select = gesture(_recording, TableViewControl.CMD_SELECT, click(_recording, "b", false, false));

		_replay.executeClientCommand(TableViewControl.CMD_GROUP, Map.of(GroupArguments.COLUMN, NAME));
		int headerOfB = indexOf(_replay, "b") - 1;
		assertEquals(RowKind.GROUP_HEADER, row(_replay, headerOfB).kind());
		_replay.executeClientCommand(TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_ID, _replay.rowIdAt(headerOfB), ExpandArguments.EXPANDED, Boolean.FALSE));
		assertEquals("The row of 'b' is hidden in its collapsed group.", -1, indexOf(_replay, "b"));
		int rowCount = _replay.getView().rowCount();

		assertNotDisplayed(replay(_replay, select));
		assertEquals("The replay expands no group.", rowCount, _replay.getView().rowCount());
		assertEquals(Set.of(), _replay.getSelectedKeys());
	}

	/**
	 * Records the given gesture on the given table as the browser sends it, applies it, and
	 * returns the recorded step.
	 */
	private static ReactCommand gesture(TableViewControl<String> table, String command, Map<String, Object> arguments) {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();
		recorder.capture(table, table, command, arguments);
		table.executeClientCommand(command, arguments);
		recorder.stop();

		List<ReactCommand> steps = recorder.steps();
		assertEquals(1, steps.size());
		return steps.get(0);
	}

	private static void assertReplayed(TableViewControl<String> table, ReactCommand step) {
		TopLogicException failure = replay(table, step);
		assertNull("Replay failed: " + failure, failure);
	}

	/** Replays the given step in the given table, and answers with its failure, if any. */
	private static TopLogicException replay(TableViewControl<String> table, ReactCommand step) {
		return CommandFailures.outcome(() -> ScriptingSession.forRoot(table).replay(step));
	}

	private static void assertNotDisplayed(TopLogicException failure) {
		assertNotNull("The object is not displayed.", failure);
		assertEquals(I18NConstants.ERROR_OBJECT_NOT_DISPLAYED__KEY.getKey(), failure.getErrorKey().plain().getKey());
	}

	private static void assertKey(String expected, String recorded) {
		assertNotNull("A key in place of the row id: " + recorded, ObjectIdTranslation.parseKey(recorded));
		assertEquals(ScriptingModelKey.toJson(ScriptingModelKey.name(null, expected)), recorded);
	}

	/** The id of the row of the given object in the given table. */
	private static String idOf(TableViewControl<String> table, String object) {
		String id = table.objectIdentities().idOf(object);
		assertNotNull("The table displays '" + object + "'.", id);
		return id;
	}

	/** The index of the data row of the given object, {@code -1} if the table displays none. */
	private static int indexOf(TableViewControl<String> table, String object) {
		List<Row<String>> rows = table.getView().rows(0, table.getView().rowCount());
		for (int n = 0; n < rows.size(); n++) {
			if (rows.get(n).kind() == RowKind.DATA && object.equals(rows.get(n).data())) {
				return n;
			}
		}
		return -1;
	}

	private static Row<String> row(TableViewControl<String> table, int index) {
		return table.getView().rows(index, index + 1).get(0);
	}

	private static Map<String, Object> click(TableViewControl<String> table, String object, boolean ctrl,
			boolean shift) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(SelectRowArguments.ROW_ID, idOf(table, object));
		result.put(SelectRowArguments.CTRL_KEY, Boolean.valueOf(ctrl));
		result.put(SelectRowArguments.SHIFT_KEY, Boolean.valueOf(shift));
		return result;
	}

	private static Map<String, Object> move(String direction, boolean extend) {
		return Map.of(MoveSelectionArguments.DIRECTION, direction, MoveSelectionArguments.EXTEND,
			Boolean.valueOf(extend), MoveSelectionArguments.MOVE, Boolean.FALSE);
	}

	private static Map<String, Object> selectAll(boolean selected) {
		return Map.of(SelectAllArguments.SELECTED, Boolean.valueOf(selected));
	}

	private static TableViewControl<String> flatTable(List<String> elements) {
		ListRowSource<String> rows = new ListRowSource<>(elements, columns());
		TableViewState state = DefaultTableView.initialState(columns(), SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.MULTI));
		return new TableViewControl<>(context(), new DefaultTableView<>(columns(), rows, state), false);
	}

	private static TableViewControl<String> treeTable() {
		Map<String, List<String>> children = Map.of("a", List.of(), "b", List.of("b1"), "c", List.of());
		TreeStructure<String, String> structure = new TreeStructure<>() {
			@Override
			public List<String> roots() {
				return List.of("a", "b", "c");
			}

			@Override
			public List<String> children(String node) {
				return children.getOrDefault(node, List.of());
			}

			@Override
			public boolean isLeaf(String node) {
				return children(node).isEmpty();
			}

			@Override
			public String businessObject(String node) {
				return node;
			}

			@Override
			public Object rootParent() {
				return null;
			}
		};
		return new TableViewControl<>(context(),
			DefaultTableView.create(columns(), new TreeRowSource<>(structure, columns())), true);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(NAME, row -> row).build());
	}

	/**
	 * The test suite, started with the resource bundles the table's column labels and a failure
	 * message need, the type index the recorder binds the commands with, and the
	 * {@link ModelResolver} the recorded commands name the rows with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableRecording.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
