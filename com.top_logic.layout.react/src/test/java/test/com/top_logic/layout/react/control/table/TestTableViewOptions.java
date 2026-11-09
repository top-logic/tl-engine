/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.InContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ApplyNamedFilterArguments;
import com.top_logic.layout.react.control.table.SearchArguments;
import com.top_logic.layout.react.control.table.SetViewOptionsArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.control.table.TableViewPreferences;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.FilterCodec;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.ViewStateStore;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Tests the view options a {@link TableViewControl} accepts from the client - density, stripes and
 * column lines, kept per person in the {@link TableViewPreferences} and not in the state of a
 * table -, the view reset, and the active filter its row count names.
 */
public class TestTableViewOptions extends TestCase {

	private static final String COLUMN_NAME = "name";

	private static final String COLUMN_STATUS = "status";

	/** The identifier of the declared filter holding the rows with an {@code a} in the name. */
	private static final String HOLDS_A = "holdsA";

	private static final String STATE_VIEW_OPTIONS = "viewOptions";

	private static final String STATE_ROW_COUNT = "rowCount";

	private static final String CMD_SET_VIEW_OPTIONS = "setViewOptions";

	private static final String CMD_RESET_VIEW = "resetView";

	private static final Item ALPHA = new Item("alpha", "open");

	private static final Item BETA = new Item("beta", "closed");

	private static final Item GAMMA = new Item("gamma", "open");

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param status
	 *        The second value shown.
	 */
	private record Item(String name, String status) {
		// Pure value type.
	}

	/** A control exposing what it pushed to the client. */
	private static final class TestTable extends TableViewControl<Item> {

		TestTable(ReactContext context, TableView<Item> view) {
			super(context, view, false);
		}

		Object clientState(String key) {
			return getState(key);
		}
	}

	/** A store keeping the last state saved, as a copy. */
	private static final class MemoryStore implements ViewStateStore {

		TableViewState _saved;

		@Override
		public TableViewState load(TableId id, FilterCodec filters) {
			return _saved == null ? null : _saved.copy();
		}

		@Override
		public void save(TableId id, TableViewState state, FilterCodec filters) {
			_saved = state.copy();
		}
	}

	private MemoryStore _store;

	private TableView<Item> _view;

	private TestTable _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_store = new MemoryStore();
		List<Column<Item, ?>> columns = columns();
		_view = DefaultTableView.create(columns,
			new ListRowSource<>(new ArrayList<>(List.of(ALPHA, BETA, GAMMA)), columns), _store,
			new TableId("t-view-options"), SortSpec.NONE, List.of(),
			List.of(NamedFilter.declared(HOLDS_A, ResKey.text("Holds a"), Map.of(), TextFilterState.contains("a"))),
			null);
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		_table = new TestTable(context, _view);
	}

	/**
	 * Tests that a table offers the view options, and that its state no longer carries the options
	 * themselves: those are the person's, not the table's.
	 */
	public void testInitialState() {
		assertEquals(Boolean.TRUE, _table.clientState(STATE_VIEW_OPTIONS));
		assertNull(_table.clientState("viewDensity"));
		assertNull(_table.clientState("striped"));
		assertNull(_table.clientState("columnLines"));
	}

	/**
	 * Tests that a table without a place to keep the view does not offer the view options: every
	 * choice would be lost at once.
	 */
	public void testNoViewOptionsWithoutStore() {
		List<Column<Item, ?>> columns = columns();
		TableView<Item> view = DefaultTableView.create(columns,
			new ListRowSource<>(new ArrayList<>(List.of(ALPHA, BETA, GAMMA)), columns));
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));

		assertFalse(view.isPersonalized());
		assertEquals(Boolean.FALSE, new TestTable(context, view).clientState(STATE_VIEW_OPTIONS));
	}

	/** Tests that a table with a store and an identity offers the view options. */
	public void testViewOptionsWithStore() {
		assertTrue(_view.isPersonalized());
		assertEquals(Boolean.TRUE, _table.clientState(STATE_VIEW_OPTIONS));
	}

	/**
	 * Tests that the command stores the options for the person, and neither changes nor persists
	 * the state of the table.
	 */
	public void testSetViewOptionsStoresForThePerson() {
		inContext(() -> {
			setViewOptions("compact", true, true);

			assertEquals("compact", TableViewPreferences.density());
			assertTrue(TableViewPreferences.striped());
			assertTrue(TableViewPreferences.columnLines());
			assertNull("The table persists nothing of it.", _store._saved);
			assertNull(_table.clientState("viewDensity"));

			setViewOptions("normal", false, true);

			assertEquals("normal", TableViewPreferences.density());
			assertFalse(TableViewPreferences.striped());
			assertTrue(TableViewPreferences.columnLines());
		});
	}

	/**
	 * Tests that a density the table does not know is the default.
	 */
	public void testUnknownDensityIsDefault() {
		inContext(() -> {
			setViewOptions("compact", false, false);
			assertEquals("compact", TableViewPreferences.density());

			setViewOptions("roomy", false, false);

			assertEquals("", TableViewPreferences.density());
		});
	}

	/**
	 * Tests that the empty density the client sends for "like everywhere" is accepted, and that a
	 * command without a density means the same.
	 */
	public void testEmptyAndMissingDensityIsDefault() {
		inContext(() -> {
			setViewOptions("compact", true, false);

			setViewOptions("", true, false);
			assertEquals("", TableViewPreferences.density());
			assertTrue(TableViewPreferences.striped());

			setViewOptions("compact", false, false);
			_table.executeClientCommand(CMD_SET_VIEW_OPTIONS, Map.of(
				SetViewOptionsArguments.STRIPED, Boolean.TRUE,
				SetViewOptionsArguments.COLUMN_LINES, Boolean.FALSE));
			assertEquals("", TableViewPreferences.density());
			assertTrue(TableViewPreferences.striped());
		});
	}

	/**
	 * Tests that the view reset restores the arrangement - column order, sort and grouping - and
	 * leaves the options of the person alone: those are not part of the table.
	 */
	public void testResetViewKeepsOptionsOfThePerson() {
		inContext(() -> {
			setViewOptions("compact", true, true);
			_view.setColumnOrder(List.of(COLUMN_STATUS, COLUMN_NAME));
			_view.sort(SortSpec.ascending(COLUMN_NAME));
			_view.group(new GroupSpec(List.of(COLUMN_STATUS)));

			_table.executeClientCommand(CMD_RESET_VIEW, Map.of());

			assertEquals("compact", TableViewPreferences.density());
			assertTrue(TableViewPreferences.striped());
			assertTrue(TableViewPreferences.columnLines());
			assertEquals(List.of(COLUMN_NAME, COLUMN_STATUS), _view.state().getColumnOrder());
			assertTrue(_view.state().getSort().isEmpty());
			assertTrue(_view.state().getGrouping().columns().isEmpty());
		});
	}

	/**
	 * Tests that the view reset keeps the filters, as the column dialog's reset does.
	 */
	public void testResetViewKeepsFilters() {
		search("zzz");

		_table.executeClientCommand(CMD_RESET_VIEW, Map.of());

		assertNotNull(_view.state().getSearch());
		assertEquals("The search still narrows the rows.",
			text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(0, 3)) + " \u00b7 "
				+ text(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(1)),
			_table.clientState(STATE_ROW_COUNT));
	}

	/**
	 * Tests that the row count of a table showing the rows of a named filter names that filter.
	 */
	public void testRowCountNamesActivePreset() {
		_table.executeClientCommand("applyNamedFilter", Map.of(ApplyNamedFilterArguments.ID, HOLDS_A));

		assertEquals(text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(3, 3)) + " \u00b7 "
			+ text(I18NConstants.TABLE_FILTER_PRESET__NAME.fill("Holds a")), _table.clientState(STATE_ROW_COUNT));
	}

	/**
	 * Tests that the row count of a table filtered by columns and search tells how many filters are
	 * active.
	 */
	public void testRowCountCountsColumnFilters() {
		_view.filter(COLUMN_NAME, TextFilterState.contains("m"));
		_view.filter(COLUMN_STATUS, TextFilterState.contains("o"));
		search("zzz");

		assertEquals(text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(0, 3)) + " \u00b7 "
			+ text(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(3)), _table.clientState(STATE_ROW_COUNT));

		search("");
		assertEquals("Two column filters remain.",
			text(I18NConstants.TABLE_ROW_COUNT_FILTERED__MATCHING_TOTAL.fill(1, 3)) + " \u00b7 "
				+ text(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(2)),
			_table.clientState(STATE_ROW_COUNT));
	}

	/**
	 * Tests that an unfiltered table names no filter.
	 */
	public void testRowCountNamesNothingWithoutFilter() {
		assertEquals(text(I18NConstants.TABLE_ROW_COUNT__COUNT.fill(3)), _table.clientState(STATE_ROW_COUNT));
	}

	/**
	 * Tests the texts naming the filter in English and German.
	 */
	public void testFilterTexts() {
		Resources en = Resources.getInstance(Locale.ENGLISH);
		assertEquals("Filter: Open", en.getString(I18NConstants.TABLE_FILTER_PRESET__NAME.fill("Open")));
		assertEquals("1 filter active", en.getString(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(1)));
		assertEquals("2 filters active", en.getString(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(2)));

		Resources de = Resources.getInstance(Locale.GERMAN);
		assertEquals("Filter: Offen", de.getString(I18NConstants.TABLE_FILTER_PRESET__NAME.fill("Offen")));
		assertEquals("1 Filter aktiv", de.getString(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(1)));
		assertEquals("2 Filter aktiv", de.getString(I18NConstants.TABLE_FILTERS_ACTIVE__COUNT.fill(2)));
	}

	/** Runs the given test body as a person: the options are stored in a user context. */
	private static void inContext(InContext body) {
		ThreadContext.inSystemContext(TestTableViewOptions.class, body);
	}

	private void setViewOptions(String density, boolean striped, boolean columnLines) {
		_table.executeClientCommand(CMD_SET_VIEW_OPTIONS, Map.of(
			SetViewOptionsArguments.DENSITY, density,
			SetViewOptionsArguments.STRIPED, Boolean.valueOf(striped),
			SetViewOptionsArguments.COLUMN_LINES, Boolean.valueOf(columnLines)));
	}

	private void search(String term) {
		_table.executeClientCommand(TableViewControl.CMD_SEARCH, Map.of(SearchArguments.TERM, term));
	}

	private static String text(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name).filter(TextColumnFilter.forStrings())
				.sort(() -> Comparator.<String> naturalOrder()).build(),
			DefaultColumn.<Item, String> builder(COLUMN_STATUS, Item::status).filter(TextColumnFilter.forStrings())
				.build());
	}

	/** Suite requiring the resources the table builds its texts from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableViewOptions.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
