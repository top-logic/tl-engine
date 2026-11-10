/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.SortArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.table.Column;
import com.top_logic.table.Selection;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the row ids of a {@link TableViewControl}: a row keeps its id while it is displayed, also
 * when the rows are sorted and filtered anew, a command by id hits the row it was made on, and the
 * headless projection names the rows by id in {@link ProjectionMode#AGENT} and by key in
 * {@link ProjectionMode#KEY}.
 */
public class TestTableRowIds extends TestCase {

	private static final String NAME = "name";

	/** State key of the rows as the client receives them. */
	private static final String ROWS = "rows";

	/** Entry of a row holding its id. */
	private static final String ROW_ID = "id";

	/** Projected state telling how many rows the table displays. */
	private static final String TOTAL_ROW_COUNT = "totalRowCount";

	/** The direction argument of a descending sort. */
	private static final String SORT_DESC = "desc";

	/** How many rows the table has: more than the client is sent and the projection holds. */
	private static final int ROW_COUNT = 300;

	private TestTable _table;

	/** A control exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<String> {

		TestTable(ReactContext context, com.top_logic.table.TableView<String> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

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
		List<String> elements = new ArrayList<>();
		for (int n = 0; n < ROW_COUNT; n++) {
			elements.add(name(n));
		}
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		ListRowSource<String> rows = new ListRowSource<>(elements, columns());
		TableViewState state = DefaultTableView.initialState(columns(), SortSpec.NONE, List.of());
		state.setSelection(Selection.none(SelectionMode.MULTI));
		_table = new TestTable(context, new DefaultTableView<>(columns(), rows, state));
	}

	/** Every row sent to the client carries an id of its own, the one the identities know it by. */
	public void testRowsCarryUniqueIds() {
		List<String> ids = new ArrayList<>();
		for (Map<String, Object> row : clientRows()) {
			ids.add((String) row.get(ROW_ID));
		}
		assertFalse(ids.isEmpty());
		assertEquals("Ids are unique: " + ids, ids.size(), new HashSet<>(ids).size());
		assertEquals(ids.get(3), identities().idOf(name(3)));
		assertEquals(name(3), identities().objectOf(ids.get(3)));
	}

	/**
	 * A row keeps its id when the rows are sorted anew, and a select command sent with the id the
	 * client rendered before the re-sort selects that row - not the one that moved to its position.
	 */
	public void testSelectByIdAfterResortHitsTheSameRow() {
		String idOfThird = _table.rowIdAt(2);
		assertEquals(name(2), identities().objectOf(idOfThird));

		sortDescending();
		assertFalse("The row moved.", idOfThird.equals(_table.rowIdAt(2)));
		assertEquals("The row keeps its id.", idOfThird, identities().idOf(name(2)));

		_table.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(SelectRowArguments.ROW_ID, idOfThird));
		assertEquals(Set.of(name(2)), _table.getSelectedKeys());
	}

	/**
	 * A row keeps its id while a filter lets it pass; a row the filter hides loses its id, its old
	 * id selects nothing, and it gets a fresh one when it is displayed again.
	 */
	public void testFilteredRowLosesItsId() {
		String kept = identities().idOf(name(12));
		String hidden = identities().idOf(name(3));

		_table.search(name(12));
		assertEquals(kept, identities().idOf(name(12)));
		assertNull("A row the filter hides is not displayed.", identities().idOf(name(3)));
		assertNull(identities().objectOf(hidden));
		_table.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(SelectRowArguments.ROW_ID, hidden));
		assertEquals("An id of no displayed row selects nothing.", Set.of(), _table.getSelectedKeys());

		_table.search("");
		assertEquals(kept, identities().idOf(name(12)));
		String again = identities().idOf(name(3));
		assertNotNull(again);
		assertFalse("A row displayed again gets a fresh id.", hidden.equals(again));
	}

	/**
	 * A key resolves to a row outside the rows sent to the client, since scrolling is no action,
	 * and a command with that id acts on it.
	 */
	public void testKeyResolvesBeyondTheRowsSent() {
		int last = ROW_COUNT - 1;
		for (Map<String, Object> row : clientRows()) {
			assertFalse(row.get(ROW_ID).equals(identities().idOf(name(last))));
		}
		String id = identities().idOfKey(ScriptingModelKey.name(null, name(last)));
		assertNotNull("The last row is displayed, though not sent.", id);

		_table.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(SelectRowArguments.ROW_ID, id));
		assertEquals(Set.of(name(last)), _table.getSelectedKeys());
	}

	/**
	 * The agent projection names the rows by the ids the commands take, with their labels, and
	 * tells how many rows the table displays beyond the projected ones.
	 */
	public void testAgentProjectionShowsIdsAndRowCount() {
		Map<String, Object> state = _table.scriptingScalarState(ProjectionMode.AGENT);
		assertEquals(Integer.valueOf(ROW_COUNT), state.get(TOTAL_ROW_COUNT));
		List<Map<String, Object>> rows = projectedRows(state);
		assertTrue("The projection holds fewer rows than the table displays.", rows.size() < ROW_COUNT);
		Map<String, Object> third = rows.get(2);
		assertEquals(_table.rowIdAt(2), third.get(ROW_ID));
		assertEquals(name(2), third.get(ScriptingModelKey.LABEL));
		assertFalse("An agent row carries no key.", third.containsKey(ModelKeyArguments.KEY));
	}

	/** The key projection names the rows by the business keys of their objects, without ids. */
	public void testKeyProjectionShowsKeys() {
		List<Map<String, Object>> rows = projectedRows(_table.scriptingScalarState(ProjectionMode.KEY));
		Map<String, Object> third = rows.get(2);
		assertEquals(ScriptingModelKey.toKey(null, name(2)), third.get(ModelKeyArguments.KEY));
		assertFalse("A row with a key carries no session-bound id.", third.containsKey(ROW_ID));
	}

	private ObjectIdentities identities() {
		return _table.objectIdentities();
	}

	/** Sends the client's command sorting the rows by name in descending order. */
	private void sortDescending() {
		_table.executeClientCommand(TableViewControl.CMD_SORT,
			Map.of(SortArguments.COLUMN, NAME, SortArguments.DIRECTION, SORT_DESC));
	}

	/** The rows as pushed to the client. */
	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> clientRows() {
		return (List<Map<String, Object>>) _table.clientState(ROWS);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> projectedRows(Map<String, Object> state) {
		return (List<Map<String, Object>>) state.get(ROWS);
	}

	private static String name(int n) {
		return String.format("r%03d", Integer.valueOf(n));
	}

	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(NAME, row -> row)
			.sort(() -> Comparator.<String> naturalOrder())
			.build());
	}

	/**
	 * The test suite, started with the resource bundles the table's column labels need, and the
	 * {@link ModelResolver} the keys of the rows are built with.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableRowIds.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
				ModelResolver.Module.INSTANCE));
	}

}
