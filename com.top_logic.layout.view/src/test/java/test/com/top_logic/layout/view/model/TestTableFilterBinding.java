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
import com.top_logic.table.FilterCodec;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.NamedFilterStore;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableId;
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
	 * Tests that a text written to the channel is searched for within the named filter the table is
	 * filtered by, which goes on being the one the other channel names.
	 */
	public void testChannelTermSearchesTheTable() {
		_activeFilter.set(HOLDS_A);

		_searchTerm.set("car");

		assertEquals(TextFilterState.contains("car"), _table.getView().state().getSearch());
		assertEquals("The filter names no term of its own, so the search narrows it instead of ending it.",
			HOLDS_A, _activeFilter.get());
		assertEquals("Of the rows holding an a, only Carla holds a car.", 1, rowCount());

		_searchTerm.set(null);

		assertNull("The search is given up again.", _table.getView().state().getSearch());
		assertEquals("The filter the search narrowed is still in effect.", HOLDS_A, _activeFilter.get());
		assertEquals(2, rowCount());
	}

	/**
	 * Tests that both channels together describe the displayed rows, so that the address a preset
	 * and a search leave behind opens the same table again.
	 */
	public void testBothChannelsDescribeTheDisplayedRows() {
		_activeFilter.set(HOLDS_A);
		_searchTerm.set("car");
		assertEquals(1, rowCount());

		// What an address holding both would be opened with.
		TableViewControl<String> reopened = table(null);
		TableFilterBinding binding = new TableFilterBinding(reopened, channel("activeFilter", HOLDS_A),
			channel("searchTerm", "car"));
		try {
			assertEquals("The link names the rows it was taken from.", 1, reopened.getView().rowCount());
			assertEquals(HOLDS_A, reopened.getView().activeNamedFilter().id());
			assertEquals(TextFilterState.contains("car"), reopened.getView().state().getSearch());
		} finally {
			binding.dispose();
		}
	}

	/**
	 * Tests that applying a named filter through the channel leaves a term the other channel already
	 * holds searched for, although the filter names none of its own.
	 */
	public void testAppliedFilterKeepsTheTermOfTheOtherChannel() {
		_searchTerm.set("car");
		assertNull("A search alone matches none of the offered filters.", _activeFilter.get());

		_activeFilter.set(HOLDS_A);

		assertEquals("The term of the other channel is searched for again.",
			TextFilterState.contains("car"), _table.getView().state().getSearch());
		assertEquals(HOLDS_A, _activeFilter.get());
		assertEquals("car", _searchTerm.get());
		assertEquals("Of the rows holding an a, only Carla holds a car.", 1, rowCount());
	}

	/**
	 * Tests that a deep link naming a preset and a text ends in the same rows whichever of its two
	 * parameters is written to its channel first.
	 */
	public void testDeepLinkAppliesInEitherOrder() {
		ViewChannel filterOfOne = new DefaultViewChannel("activeFilter");
		ViewChannel termOfOne = new DefaultViewChannel("searchTerm");
		TableViewControl<String> filterFirst = table(null);
		TableFilterBinding bindingOfOne = new TableFilterBinding(filterFirst, filterOfOne, termOfOne);

		ViewChannel filterOfOther = new DefaultViewChannel("activeFilter");
		ViewChannel termOfOther = new DefaultViewChannel("searchTerm");
		TableViewControl<String> termFirst = table(null);
		TableFilterBinding bindingOfOther = new TableFilterBinding(termFirst, filterOfOther, termOfOther);
		try {
			// A query binding writes one bound parameter after the other, in the order they are
			// declared in.
			filterOfOne.set(HOLDS_A);
			termOfOne.set("car");

			termOfOther.set("car");
			filterOfOther.set(HOLDS_A);

			assertEquals("The link names one set of rows.", 1, filterFirst.getView().rowCount());
			assertEquals("The other order names the same set.", 1, termFirst.getView().rowCount());
			assertEquals(HOLDS_A, filterOfOne.get());
			assertEquals("car", termOfOne.get());
			assertEquals(HOLDS_A, filterOfOther.get());
			assertEquals("The term must not be wiped by the filter arriving after it.",
				"car", termOfOther.get());
		} finally {
			bindingOfOne.dispose();
			bindingOfOther.dispose();
		}
	}

	/**
	 * Tests that giving up the named filter through its channel leaves the searched text in effect,
	 * which is a channel of its own.
	 */
	public void testNoFilterOnTheChannelKeepsTheSearch() {
		_activeFilter.set(HOLDS_A);
		_searchTerm.set("car");

		_activeFilter.set(null);

		assertNull("The table is filtered by no named filter.", _activeFilter.get());
		assertEquals("car", _searchTerm.get());
		assertEquals(TextFilterState.contains("car"), _table.getView().state().getSearch());
		assertEquals("Every row holding a car, not only the ones holding an a.", 1, rowCount());
	}

	/**
	 * Tests that giving up the search through its channel leaves the named filter applied.
	 */
	public void testNoTermOnTheChannelKeepsTheFilter() {
		_activeFilter.set(HOLDS_A);
		_searchTerm.set("car");
		assertEquals(1, rowCount());

		_searchTerm.set(null);

		assertEquals(HOLDS_A, _activeFilter.get());
		assertNull(_searchTerm.get());
		assertNull("Nothing is searched for any more.", _table.getView().state().getSearch());
		assertEquals("Alice and Carla hold an a.", 2, rowCount());
	}

	/**
	 * Tests that a filter carrying a term of its own is compared by that term as well: searching for
	 * another text ends its match and empties the channel naming it.
	 */
	public void testTermOfAFilterCarryingOneIsCompared() {
		_table.applyNamedFilter(HOLDS_LI);
		assertEquals(HOLDS_LI, _activeFilter.get());
		assertEquals("Such a filter puts its own term on the other channel.", "li", _searchTerm.get());

		_searchTerm.set("bo");

		assertNull("The filter carries a term of its own, which is no longer the one searched for.",
			_activeFilter.get());
	}

	/** A channel of the given name, already holding the given value. */
	private static ViewChannel channel(String name, String value) {
		ViewChannel result = new DefaultViewChannel(name);
		result.set(value);
		return result;
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

	/**
	 * Tests that a filter the user saves while searching within a preset is the one that reaches the
	 * channel, although the preset matches the same columns and is offered first.
	 */
	public void testSavedFilterReachesTheChannelInsteadOfThePreset() {
		ViewChannel activeFilter = new DefaultViewChannel("activeFilter");
		ViewChannel searchTerm = new DefaultViewChannel("searchTerm");
		TableViewControl<String> table = savingTable();
		TableFilterBinding binding = new TableFilterBinding(table, activeFilter, searchTerm);
		try {
			table.applyNamedFilter(HOLDS_A);
			table.search("car");
			assertEquals(HOLDS_A, activeFilter.get());

			NamedFilter saved = table.getView().saveNamedFilter("Mine");

			assertEquals("The saved filter names the term as well, so it is the one displayed.",
				saved.id(), activeFilter.get());
			assertEquals("car", searchTerm.get());
		} finally {
			binding.dispose();
		}
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

	/** A table over the three rows that also keeps the filters the user saves. */
	private TableViewControl<String> savingTable() {
		List<Column<String, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, Set.of());
		ListRowSource<String> rows =
			new ListRowSource<>(new ArrayList<>(List.of(ALICE, BOB, CARLA)), columns);
		DefaultTableView<String> view = new DefaultTableView<>(columns, rows, state, null, new TableId("t-save"),
			Set.of(), declaredFilters(), new MemoryFilterStore(), null);
		return new TableViewControl<>(_context, view, false);
	}

	/** An in-memory {@link NamedFilterStore}, so that a filter can be saved at all. */
	private static final class MemoryFilterStore implements NamedFilterStore {

		private List<NamedFilter> _filters = List.of();

		@Override
		public List<NamedFilter> load(TableId id, FilterCodec codec) {
			return _filters;
		}

		@Override
		public void save(TableId id, List<NamedFilter> filters, FilterCodec codec) {
			_filters = List.copyOf(filters);
		}
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
