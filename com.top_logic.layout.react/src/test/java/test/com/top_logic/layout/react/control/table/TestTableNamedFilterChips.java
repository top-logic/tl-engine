/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests what a {@link TableViewControl} tells the client about the presentation of the chips of its
 * {@link NamedFilter}s: the {@link NamedFilter#cssClass() CSS class} and the number of rows a
 * {@link NamedFilter#counted() counted} filter selects.
 *
 * <p>
 * A filter without presentation of its own is sent exactly as before, the count is that of the
 * filter's criteria alone - the live search does not change it -, and it follows the rows of the
 * table.
 * </p>
 */
public class TestTableNamedFilterChips extends TestCase {

	/** The name of the column holding the row label. */
	private static final String COLUMN_NAME = "name";

	/** The name of the column telling whether a row is open. */
	private static final String COLUMN_OPEN = "open";

	/** The identifier of the counted filter selecting the open rows. */
	private static final String OPEN = "open";

	/** The identifier of the filter selecting the closed rows, neither counted nor styled. */
	private static final String CLOSED = "closed";

	/** The CSS class of the chip of {@link #OPEN}. */
	private static final String OPEN_CLASS = "tl-preset--open";

	/** State key of the number of rows the table displays. */
	private static final String TOTAL_ROW_COUNT = "totalRowCount";

	/** State key of the named filters the client draws as chips. */
	private static final String NAMED_FILTERS = "namedFilters";

	/** Entry key of a named filter's identifier. */
	private static final String NAMED_FILTER_ID = "id";

	/** Entry key of a named filter's displayed name. */
	private static final String NAMED_FILTER_LABEL = "label";

	/** Entry key telling whether a named filter may be deleted. */
	private static final String NAMED_FILTER_DELETABLE = "deletable";

	/** Entry key of a named filter's CSS class. */
	private static final String NAMED_FILTER_CSS_CLASS = "cssClass";

	/** Entry key of the number of rows a named filter selects. */
	private static final String NAMED_FILTER_COUNT = "count";

	/** The entry keys of a named filter without presentation of its own. */
	private static final List<String> PLAIN_KEYS = List.of(NAMED_FILTER_ID, NAMED_FILTER_LABEL, NAMED_FILTER_DELETABLE);

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label.
	 * @param open
	 *        Whether the row is open.
	 */
	private record Item(String name, boolean open) {
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

	private ListRowSource<Item> _rows;

	private DefaultTableView<Item> _view;

	private TestTable _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Column<Item, ?>> columns = columns();
		_rows = new ListRowSource<>(new ArrayList<>(List.of(
			new Item("a", true), new Item("b", true), new Item("c", false))), columns);
		_view = DefaultTableView.create(columns, _rows, null, null, SortSpec.NONE, List.of(),
			List.of(openFilter(true), closedFilter()), null);
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test"));
		_table = new TestTable(context, _view);
		_table.setFilterBar(true);
	}

	/**
	 * Tests that a counted filter is sent with the number of rows it selects and its CSS class.
	 */
	public void testCountedFilterCarriesCountAndClass() {
		Map<String, Object> open = filterState(OPEN);
		assertEquals(Integer.valueOf(2), open.get(NAMED_FILTER_COUNT));
		assertEquals(OPEN_CLASS, open.get(NAMED_FILTER_CSS_CLASS));
	}

	/**
	 * Tests that a filter without presentation of its own is sent without count and class, exactly
	 * with the keys it was always sent with.
	 */
	public void testPlainFilterCarriesNoPresentation() {
		Map<String, Object> closed = filterState(CLOSED);
		assertFalse(closed.containsKey(NAMED_FILTER_COUNT));
		assertFalse(closed.containsKey(NAMED_FILTER_CSS_CLASS));
		assertEquals(PLAIN_KEYS, new ArrayList<>(closed.keySet()));
	}

	/**
	 * Tests that the count follows the rows of the table when its data changes.
	 */
	public void testCountFollowsData() {
		_rows.setElements(new ArrayList<>(List.of(
			new Item("a", true), new Item("b", true), new Item("c", true), new Item("d", false))));
		_table.refreshData();

		assertEquals(Integer.valueOf(3), filterState(OPEN).get(NAMED_FILTER_COUNT));
	}

	/**
	 * Tests that neither the live search nor another active named filter changes the count: it is
	 * that of the filter's criteria alone.
	 */
	public void testCountIgnoresLiveFiltering() {
		_table.search("a");
		assertEquals("The search narrows the displayed rows.", Integer.valueOf(1), totalRowCount());
		assertEquals(Integer.valueOf(2), filterState(OPEN).get(NAMED_FILTER_COUNT));

		_table.applyNamedFilter(CLOSED);
		assertEquals(Integer.valueOf(2), filterState(OPEN).get(NAMED_FILTER_COUNT));
	}

	/**
	 * Tests that declared filters replaced on the view directly reach the client by
	 * {@link TableViewControl#refreshFilters()}: a filter no longer counted is sent without count.
	 */
	public void testRefreshFiltersSendsReplacedFilters() {
		_view.setDeclaredFilters(List.of(openFilter(false), closedFilter()));
		_table.refreshFilters();

		assertFalse(filterState(OPEN).containsKey(NAMED_FILTER_COUNT));
		assertEquals(OPEN_CLASS, filterState(OPEN).get(NAMED_FILTER_CSS_CLASS));
	}

	/** The filter selecting the open rows, with a CSS class. */
	private static NamedFilter openFilter(boolean counted) {
		return NamedFilter.declared(OPEN, ResKey.text("Open"),
			Map.of(COLUMN_OPEN, new BooleanFilterState(true, false, false)), null, OPEN_CLASS, counted);
	}

	/** The filter selecting the closed rows, without presentation of its own. */
	private static NamedFilter closedFilter() {
		return NamedFilter.declared(CLOSED, ResKey.text("Closed"),
			Map.of(COLUMN_OPEN, new BooleanFilterState(false, true, false)), null);
	}

	/** The number of rows the table displays, as pushed to the client. */
	private Object totalRowCount() {
		return _table.clientState(TOTAL_ROW_COUNT);
	}

	/** The state the table pushed to the client for the named filter with the given identifier. */
	@SuppressWarnings("unchecked")
	private Map<String, Object> filterState(String id) {
		for (Map<String, Object> filterState : (List<Map<String, Object>>) _table.clientState(NAMED_FILTERS)) {
			if (id.equals(filterState.get(NAMED_FILTER_ID))) {
				return filterState;
			}
		}
		throw new AssertionError("No state for named filter " + id + ".");
	}

	/** A searchable name column, and a filterable column telling whether a row is open. */
	private static List<Column<Item, ?>> columns() {
		return List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name)
				.renderer(name -> CellContent.text(name))
				.filter(TextColumnFilter.forStrings())
				.build(),
			DefaultColumn.<Item, Boolean> builder(COLUMN_OPEN, Item::open)
				.renderer(open -> CellContent.text(open.booleanValue() ? "open" : "closed"))
				.filter(BooleanColumnFilter.INSTANCE)
				.build());
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableNamedFilterChips.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
