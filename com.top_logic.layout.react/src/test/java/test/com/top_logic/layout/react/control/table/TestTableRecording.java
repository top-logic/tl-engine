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
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.selection.SelectByKeysArguments;
import com.top_logic.layout.react.control.table.ActivateByKeyArguments;
import com.top_logic.layout.react.control.table.ActivateRowArguments;
import com.top_logic.layout.react.control.table.ExpandArguments;
import com.top_logic.layout.react.control.table.ExpandByKeyArguments;
import com.top_logic.layout.react.control.table.MoveSelectionArguments;
import com.top_logic.layout.react.control.table.SelectAllArguments;
import com.top_logic.layout.react.control.table.SelectByKeyArguments;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests that a {@link TableViewControl} records the gestures on its rows by the business identity
 * of the rows, so that a recording replays in a table showing the rows in another order - as a
 * fresh session may.
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

	/** A plain click is recorded as a selection of the row by its key. */
	public void testPlainClickIsRecordedByKey() {
		RecordedCommand recorded = recordAndApply(_recording, TableViewControl.CMD_SELECT, click(1, false, false));
		assertTrue(recorded.command() instanceof SelectByKeyArguments);
		assertEquals(TableViewControl.CMD_SELECT_BY_KEY, recorded.command().getName());

		assertReplayed(recorded);
		assertEquals(Set.of("b"), _replay.getSelectedKeys());
	}

	/**
	 * A click with {@code Ctrl} is recorded as the selection it results in, which replays in a
	 * table whose rows stand in another order.
	 */
	public void testCtrlClickIsRecordedAsResultingSelection() {
		replay(recordAndApply(_recording, TableViewControl.CMD_SELECT, click(1, false, false)));

		RecordedCommand recorded = recordAndApply(_recording, TableViewControl.CMD_SELECT, click(3, true, false));
		assertEquals(TableViewControl.CMD_SELECT_BY_KEYS, recorded.command().getName());
		assertTrue("Consecutive multi-selections coalesce into one step.", recorded.coalescing());
		assertEquals(Set.of("b", "d"), _recording.getSelectedKeys());
		assertKeys(List.of("b", "d"), recorded);

		assertReplayed(recorded);
		assertEquals(Set.of("b", "d"), _replay.getSelectedKeys());
	}

	/**
	 * A click with {@code Shift} is recorded as the range it selects, which replays independent of
	 * the anchor and the order of the rows.
	 */
	public void testShiftClickIsRecordedAsResultingSelection() {
		recordAndApply(_recording, TableViewControl.CMD_SELECT, click(1, false, false));

		RecordedCommand recorded = recordAndApply(_recording, TableViewControl.CMD_SELECT, click(3, false, true));
		assertEquals(Set.of("b", "c", "d"), _recording.getSelectedKeys());
		assertKeys(List.of("b", "c", "d"), recorded);

		// Without any anchor in the replaying table.
		assertReplayed(recorded);
		assertEquals(Set.of("b", "c", "d"), _replay.getSelectedKeys());
	}

	/** Select-all and deselect-all are recorded as the selection they result in. */
	public void testSelectAllIsRecordedAsResultingSelection() {
		RecordedCommand all = recordAndApply(_recording, TableViewControl.CMD_SELECT_ALL, selectAll(true));
		assertEquals(TableViewControl.CMD_SELECT_BY_KEYS, all.command().getName());
		assertKeys(ROWS, all);
		assertReplayed(all);
		assertEquals(Set.copyOf(ROWS), _replay.getSelectedKeys());

		RecordedCommand none = recordAndApply(_recording, TableViewControl.CMD_SELECT_ALL, selectAll(false));
		assertKeys(List.of(), none);
		assertReplayed(none);
		assertEquals(Set.of(), _replay.getSelectedKeys());
	}

	/**
	 * Keyboard navigation is recorded as the selection it results in, which replays without the
	 * cursor of the recording table.
	 */
	public void testKeyboardNavigationIsRecordedAsResultingSelection() {
		recordAndApply(_recording, TableViewControl.CMD_SELECT, click(1, false, false));

		RecordedCommand down = recordAndApply(_recording, TableViewControl.CMD_MOVE_SELECTION, move("down", false));
		assertEquals(TableViewControl.CMD_SELECT_BY_KEYS, down.command().getName());
		assertTrue("Consecutive multi-selections coalesce into one step.", down.coalescing());
		assertKeys(List.of("c"), down);
		assertReplayed(down);
		assertEquals(Set.of("c"), _replay.getSelectedKeys());

		RecordedCommand extend = recordAndApply(_recording, TableViewControl.CMD_MOVE_SELECTION, move("down", true));
		assertEquals(Set.of("c", "d"), _recording.getSelectedKeys());
		assertKeys(List.of("c", "d"), extend);
		assertReplayed(extend);
		assertEquals(Set.of("c", "d"), _replay.getSelectedKeys());
	}

	/** An activation is recorded by the key of the activated row. */
	public void testActivationIsRecordedByKey() {
		List<String> activated = new ArrayList<>();
		_replay.setActivationHandler(row -> {
			activated.add(row);
			return HandlerResult.DEFAULT_RESULT;
		});
		RecordedCommand recorded = _recording.recordCommand(TableViewControl.CMD_ACTIVATE,
			Map.of(ActivateRowArguments.ROW_INDEX, Integer.valueOf(2)));
		assertTrue(recorded.command() instanceof ActivateByKeyArguments);
		assertReplayed(recorded);
		assertEquals(List.of("c"), activated);
	}

	/**
	 * A recorded selection naming a row the replaying table does not have fails with its message
	 * instead of selecting something else.
	 */
	public void testUnresolvedKeysFail() {
		SelectByKeysArguments keys = TypedConfiguration.newConfigItem(SelectByKeysArguments.class);
		keys.setName(TableViewControl.CMD_SELECT_BY_KEYS);
		keys.getKeys().addAll(names("b", "x"));
		HandlerResult result = replay(keys);
		assertFalse(result.isSuccess());
		assertEquals("A failed replay changes nothing.", Set.of(), _replay.getSelectedKeys());

		SelectByKeyArguments key = TypedConfiguration.newConfigItem(SelectByKeyArguments.class);
		key.setName(TableViewControl.CMD_SELECT_BY_KEY);
		key.setKey(name("x"));
		assertFalse(replay(key).isSuccess());
	}

	/** Expanding a tree row is recorded by the key of the row. */
	public void testExpansionIsRecordedByKey() {
		TableViewControl<String> recording = treeTable();
		TableViewControl<String> replay = treeTable();
		assertEquals(3, replay.getView().rowCount());

		RecordedCommand recorded = recording.recordCommand(TableViewControl.CMD_EXPAND,
			Map.of(ExpandArguments.ROW_INDEX, Integer.valueOf(1), ExpandArguments.EXPANDED, Boolean.TRUE));
		assertTrue(recorded.command() instanceof ExpandByKeyArguments);
		assertEquals(ScriptingModelKey.toJson(name("b")),
			ScriptingModelKey.toJson(((ModelKeyArguments) recorded.command()).getKey()));
		assertTrue(((ExpandByKeyArguments) recorded.command()).isExpanded());

		HandlerResult result = new InteractionWithoutSession().runWithContext(
			() -> replay.executeClientCommand(recorded.command().getName(), ReactCommands.arguments(recorded.command())));
		assertTrue(result.isSuccess());
		assertEquals("The children of 'b' are displayed.", 4, replay.getView().rowCount());
	}

	/** Records the given client command on the given table and applies it there. */
	private RecordedCommand recordAndApply(TableViewControl<String> table, String command, Map<String, Object> args) {
		RecordedCommand recorded = table.recordCommand(command, args);
		assertNotNull(recorded);
		table.executeClientCommand(command, args);
		return recorded;
	}

	private void assertReplayed(RecordedCommand recorded) {
		HandlerResult result = replay(recorded);
		assertTrue("Replay failed: " + result.getEncodedErrors(), result.isSuccess());
	}

	private HandlerResult replay(RecordedCommand recorded) {
		return replay(recorded.command());
	}

	private HandlerResult replay(ReactCommand command) {
		return new InteractionWithoutSession().runWithContext(
			() -> _replay.executeClientCommand(command.getName(), ReactCommands.arguments(command)));
	}

	private static Map<String, Object> click(int rowIndex, boolean ctrl, boolean shift) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(SelectRowArguments.ROW_INDEX, Integer.valueOf(rowIndex));
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

	/** Asserts that the given recorded selection names exactly the given rows, in this order. */
	private static void assertKeys(List<String> expected, RecordedCommand recorded) {
		List<String> actual = new ArrayList<>();
		for (ModelName key : ((SelectByKeysArguments) recorded.command()).getKeys()) {
			actual.add(ScriptingModelKey.toJson(key));
		}
		List<String> expectedJson = new ArrayList<>();
		for (ModelName key : names(expected.toArray(new String[0]))) {
			expectedJson.add(ScriptingModelKey.toJson(key));
		}
		assertEquals(expectedJson, actual);
	}

	private static List<ModelName> names(String... objects) {
		List<ModelName> result = new ArrayList<>();
		for (String object : objects) {
			result.add(name(object));
		}
		return result;
	}

	private static ModelName name(String object) {
		return ScriptingModelKey.name(null, object);
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
	 * The test suite, started with the resource bundles the table's column labels need, and the
	 * {@link ModelResolver} the recorded commands name the rows with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableRecording.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
