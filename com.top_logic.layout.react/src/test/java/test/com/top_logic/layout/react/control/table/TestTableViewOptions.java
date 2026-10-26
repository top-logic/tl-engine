/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

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
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.FilterCodec;
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
 * column lines -, the view reset, and the active filter its row count names.
 */
public class TestTableViewOptions extends TestCase {

	private static final String COLUMN_NAME = "name";

	private static final String COLUMN_STATUS = "status";

	/** The identifier of the declared filter holding the rows with an {@code a} in the name. */
	private static final String HOLDS_A = "holdsA";

	private static final String STATE_VIEW_DENSITY = "viewDensity";

	private static final String STATE_STRIPED = "striped";

	private static final String STATE_COLUMN_LINES = "columnLines";

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
	 * Tests that a table without any view option set tells the client the factory settings, and
	 * offers the view options.
	 */
	public void testInitialState() {
		assertEquals("", _table.clientState(STATE_VIEW_DENSITY));
		assertEquals(Boolean.FALSE, _table.clientState(STATE_STRIPED));
		assertEquals(Boolean.FALSE, _table.clientState(STATE_COLUMN_LINES));
		assertEquals(Boolean.TRUE, _table.clientState(STATE_VIEW_OPTIONS));
	}

	/**
	 * Tests that the command changes the view, pushes the new options to the client, and persists
	 * them.
	 */
	public void testSetViewOptionsUpdatesStateAndPersists() {
		setViewOptions("compact", true, true);

		assertEquals("compact", _table.clientState(STATE_VIEW_DENSITY));
		assertEquals(Boolean.TRUE, _table.clientState(STATE_STRIPED));
		assertEquals(Boolean.TRUE, _table.clientState(STATE_COLUMN_LINES));
		assertEquals("compact", _view.state().getDensity());
		assertTrue(_view.state().isStriped());
		assertTrue(_view.state().isColumnLines());

		assertNotNull("The options are persisted.", _store._saved);
		assertEquals("compact", _store._saved.getDensity());
		assertTrue(_store._saved.isStriped());
		assertTrue(_store._saved.isColumnLines());

		setViewOptions("normal", false, true);
		assertEquals("normal", _table.clientState(STATE_VIEW_DENSITY));
		assertEquals(Boolean.FALSE, _table.clientState(STATE_STRIPED));
		assertEquals(Boolean.TRUE, _table.clientState(STATE_COLUMN_LINES));
	}

	/**
	 * Tests that a density the table does not know is the default.
	 */
	public void testUnknownDensityIsDefault() {
		setViewOptions("compact", false, false);
		setViewOptions("roomy", false, false);
		assertEquals("", _table.clientState(STATE_VIEW_DENSITY));
		assertEquals("", _view.state().getDensity());
	}

	/**
	 * Tests that the view reset restores the view options, and tells the client so.
	 */
	public void testResetViewRestoresOptions() {
		setViewOptions("compact", true, true);

		_table.executeClientCommand(CMD_RESET_VIEW, Map.of());

		assertEquals("", _table.clientState(STATE_VIEW_DENSITY));
		assertEquals(Boolean.FALSE, _table.clientState(STATE_STRIPED));
		assertEquals(Boolean.FALSE, _table.clientState(STATE_COLUMN_LINES));
		assertEquals("", _view.state().getDensity());
		assertFalse(_store._saved.isStriped());
	}

	/**
	 * Tests that the view reset keeps the filters, as the column dialog's reset does.
	 */
	public void testResetViewKeepsFilters() {
		search("a");
		setViewOptions("compact", true, true);

		_table.executeClientCommand(CMD_RESET_VIEW, Map.of());

		assertNotNull(_view.state().getSearch());
		assertEquals(Integer.valueOf(3), _table.clientState("totalRowCount"));
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
				.build(),
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
