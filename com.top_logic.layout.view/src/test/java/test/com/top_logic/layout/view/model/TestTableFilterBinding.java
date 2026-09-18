/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.TableFilterBinding;
import com.top_logic.table.Column;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableViewState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests for {@link TableFilterBinding}.
 *
 * <p>
 * The filtering is what a table is linked by: the identifier of the named filter it matches and the
 * text it searches for travel in both directions, and a value nothing in the table answers to is
 * corrected to what the table actually shows.
 * </p>
 */
public class TestTableFilterBinding extends TestCase {

	/** The name of the single column, showing the row object itself. */
	private static final String COLUMN_NAME = "name";

	/** Identifier of the declared filter selecting the rows holding an {@code a}. */
	private static final String HOLDS_A = "holds-a";

	/** Identifier of the declared filter searching for {@code li}. */
	private static final String HOLDS_LI = "holds-li";

	/** A row holding an {@code a} and an {@code li}. */
	private static final String ALICE = "Alice";

	/** A row holding neither. */
	private static final String BOB = "Bob";

	/** A row holding an {@code a}. */
	private static final String CARLA = "Carla";

	private ReactContext _context;

	private ViewChannel _activeFilter;

	private ViewChannel _searchTerm;

	private TableViewControl<String> _table;

	private TableFilterBinding _binding;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_activeFilter = new DefaultViewChannel("activeFilter");
		_searchTerm = new DefaultViewChannel("searchTerm");
		_table = table(null);
		_binding = new TableFilterBinding(_table, _activeFilter, _searchTerm);
	}

	/**
	 * Tests that a filter applied in the table reaches the channels, and that clearing it empties
	 * them again.
	 */
	public void testAppliedFilterReachesTheChannels() {
		assertNull("The table starts out unfiltered.", _activeFilter.get());
		assertNull("The table starts out searching for nothing.", _searchTerm.get());

		_table.applyNamedFilter(HOLDS_LI);

		assertEquals(HOLDS_LI, _activeFilter.get());
		assertEquals("The filter's own search term is what the table searches for.", "li", _searchTerm.get());
		assertEquals("Only the row holding an li is displayed.", 1, rowCount());

		_table.clearFilter();

		assertNull(_activeFilter.get());
		assertNull(_searchTerm.get());
		assertEquals(3, rowCount());
	}

	/**
	 * Tests that a column filter set in the table - which no named filter carries - empties the
	 * channel naming the matched filter.
	 */
	public void testCriteriaOfNoNameEmptyTheChannel() {
		_table.applyNamedFilter(HOLDS_A);
		assertEquals(HOLDS_A, _activeFilter.get());

		_table.getView().filter(COLUMN_NAME, TextFilterState.contains("bo"));

		assertNull("The criteria are no longer the ones of a named filter.", _activeFilter.get());
	}

	/**
	 * Tests that an identifier written to the channel filters the table by that named filter.
	 */
	public void testChannelValueFiltersTheTable() {
		_activeFilter.set(HOLDS_A);

		assertEquals(HOLDS_A, _table.getView().activeNamedFilter().id());
		assertEquals("Alice and Carla hold an a.", 2, rowCount());

		_activeFilter.set(null);

		assertNull("The table is unfiltered again.", _table.getView().activeNamedFilter());
		assertEquals(3, rowCount());
	}

	/**
	 * Tests that an identifier no offered filter carries leaves the table unfiltered and is
	 * corrected to what the table shows, so a link that has outlived its preset heals itself.
	 */
	public void testUnknownIdentifierCorrectsItself() {
		_activeFilter.set(HOLDS_A);

		_activeFilter.set("nonesuch");

		assertNull("The value names no filter, so the table shows every row.", _activeFilter.get());
		assertEquals(3, rowCount());
	}

	/**
	 * Tests that a text written to the channel is searched for, and that it ends the match with a
	 * named filter the table was filtered by.
	 */
	public void testChannelTermSearchesTheTable() {
		_activeFilter.set(HOLDS_A);

		_searchTerm.set("bo");

		assertEquals(TextFilterState.contains("bo"), _table.getView().state().getSearch());
		assertNull("The searched text is no criterion of the filter, so the match is over.",
			_activeFilter.get());
		assertEquals("Only Bob is searched for, and Bob holds no a.", 0, rowCount());

		_searchTerm.set(null);

		assertNull("The search is given up again.", _table.getView().state().getSearch());
	}

	/**
	 * Tests that a term the table itself is searched for reaches the channel.
	 */
	public void testSearchedTermReachesTheChannel() {
		_table.search("bo");

		assertEquals("bo", _searchTerm.get());
		assertEquals(1, rowCount());
	}

	/**
	 * Tests that a channel already holding an identifier when the binding is created is what the
	 * table is filtered by, while an empty channel takes the filtering the table starts with.
	 */
	public void testCreationLetsTheChannelSpeakFirst() {
		ViewChannel activeFilter = new DefaultViewChannel("activeFilter");
		activeFilter.set(HOLDS_LI);
		TableViewControl<String> table = table(HOLDS_A);
		TableFilterBinding binding = new TableFilterBinding(table, activeFilter, null);
		try {
			assertEquals("The value someone asked for wins over the filter the table starts with.",
				HOLDS_LI, table.getView().activeNamedFilter().id());

			TableViewControl<String> initial = table(HOLDS_A);
			TableFilterBinding initialBinding =
				new TableFilterBinding(initial, new DefaultViewChannel("empty"), null);
			try {
				assertEquals("The table keeps the filter it starts with.",
					HOLDS_A, initial.getView().activeNamedFilter().id());
			} finally {
				initialBinding.dispose();
			}
		} finally {
			binding.dispose();
		}
	}

	/**
	 * Tests that the filter the table starts with is published on the channel, so an address bound
	 * to it names what the user sees.
	 */
	public void testInitialFilterReachesTheChannel() {
		ViewChannel activeFilter = new DefaultViewChannel("activeFilter");
		TableViewControl<String> table = table(HOLDS_A);
		TableFilterBinding binding = new TableFilterBinding(table, activeFilter, null);
		try {
			assertEquals(HOLDS_A, activeFilter.get());
		} finally {
			binding.dispose();
		}
	}

	/**
	 * Tests that a binding over the search term alone leaves the other side of the filtering alone.
	 */
	public void testSearchTermAlone() {
		_binding.dispose();
		ViewChannel searchTerm = new DefaultViewChannel("searchTerm");
		TableFilterBinding binding = new TableFilterBinding(_table, null, searchTerm);
		try {
			_table.applyNamedFilter(HOLDS_LI);

			assertEquals("li", searchTerm.get());
			assertNull("The channel the binding does not have stays untouched.", _activeFilter.get());

			searchTerm.set("bo");

			assertEquals(TextFilterState.contains("bo"), _table.getView().state().getSearch());
		} finally {
			binding.dispose();
		}
	}

	/**
	 * Tests that a disposed binding leaves table and channels alone.
	 */
	public void testDisposedBindingBindsNothing() {
		_binding.dispose();

		_activeFilter.set(HOLDS_A);
		assertNull("The table is not filtered any more.", _table.getView().activeNamedFilter());

		_table.applyNamedFilter(HOLDS_LI);
		assertEquals("The channel keeps what was written to it.", HOLDS_A, _activeFilter.get());
	}

	/** The number of rows the table displays. */
	private int rowCount() {
		return _table.getView().rowCount();
	}

	/**
	 * A table over the three rows, offering the two declared filters.
	 *
	 * @param initialFilter
	 *        The filter the table is filtered by while nothing is personalized about it,
	 *        {@code null} to start out unfiltered.
	 */
	private TableViewControl<String> table(String initialFilter) {
		List<Column<String, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, Set.of());
		ListRowSource<String> rows =
			new ListRowSource<>(new ArrayList<>(List.of(ALICE, BOB, CARLA)), columns);
		DefaultTableView<String> view = new DefaultTableView<>(columns, rows, state, null, null, Set.of(),
			declaredFilters(), null, initialFilter);
		return new TableViewControl<>(_context, view, false);
	}

	/** The single, filterable column showing the row object itself. */
	private static List<Column<String, ?>> columns() {
		return List.<Column<String, ?>> of(DefaultColumn.<String, String> builder(COLUMN_NAME, row -> row)
			.filter(TextColumnFilter.forStrings())
			.build());
	}

	/** The two filters the table offers under a name. */
	private static List<NamedFilter> declaredFilters() {
		return List.of(
			NamedFilter.declared(HOLDS_A, ResKey.text("Holds a"),
				Map.of(COLUMN_NAME, TextFilterState.contains("a")), null),
			NamedFilter.declared(HOLDS_LI, ResKey.text("Holds li"), Map.of(), TextFilterState.contains("li")));
	}

	@Override
	protected void tearDown() throws Exception {
		_binding.dispose();
		super.tearDown();
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableFilterBinding.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
