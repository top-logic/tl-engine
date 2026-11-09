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
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.DisplayedRowsBinding;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortSpec;
import com.top_logic.table.TableView;
import com.top_logic.table.TableViewState;
import com.top_logic.table.TreeStructure;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.table.impl.TreeRowSource;

/**
 * Tests for {@link DisplayedRowsBinding}.
 *
 * <p>
 * The channel holds the objects of exactly the rows the user sees, in the order the table displays
 * them, and follows every change of the table: filtering, searching, sorting, expanding,
 * collapsing and a refresh of the rows.
 * </p>
 */
public class TestDisplayedRowsBinding extends TestCase {

	/** The name of the column showing the name of a person. */
	private static final String COLUMN_NAME = "name";

	/** The name of the column showing the team of a person. */
	private static final String COLUMN_TEAM = "team";

	private static final Person ALICE = new Person("Alice", "red");

	private static final Person BOB = new Person("Bob", "blue");

	private static final Person CARLA = new Person("Carla", "red");

	/** A root node of the tree, with {@link #CHILD} below it. */
	private static final String PARENT = "parent";

	/** The single child of {@link #PARENT}. */
	private static final String CHILD = "child";

	/** A root node of the tree without children. */
	private static final String LEAF = "leaf";

	private ReactContext _context;

	private ViewChannel _channel;

	private ListRowSource<Person> _rows;

	private TableViewControl<Person> _table;

	private DisplayedRowsBinding _binding;

	/** The number of notifications {@link #_channel} has sent. */
	private int _notifications;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_channel = new DefaultViewChannel("displayedRows");
		_rows = new ListRowSource<>(new ArrayList<>(List.of(BOB, ALICE, CARLA)), columns());
		_table = new TableViewControl<>(_context, view(_rows, GroupSpec.NONE), false);
		_binding = new DisplayedRowsBinding(_table.getView(), _channel);
		_channel.addListener((sender, oldValue, newValue) -> _notifications++);
	}

	@Override
	protected void tearDown() throws Exception {
		_binding.dispose();
		super.tearDown();
	}

	/**
	 * Tests that the binding publishes the rows when it is created, in the order of the source.
	 */
	public void testInitialRowsInSourceOrder() {
		assertEquals(List.of(BOB, ALICE, CARLA), _channel.get());
	}

	/**
	 * Tests that a column filter leaves only the matching rows on the channel.
	 */
	public void testColumnFilter() {
		_table.getView().filter(COLUMN_NAME, TextFilterState.contains("a"));

		assertEquals(List.of(ALICE, CARLA), _channel.get());
	}

	/**
	 * Tests that a search leaves only the matching rows on the channel.
	 */
	public void testSearch() {
		_table.search("bo");

		assertEquals(List.of(BOB), _channel.get());

		_table.search(null);

		assertEquals(List.of(BOB, ALICE, CARLA), _channel.get());
	}

	/**
	 * Tests that the channel holds the rows in the order the table is sorted in.
	 */
	public void testSort() {
		_table.getView().sort(new SortSpec(List.of(new SortColumn(COLUMN_NAME, true))));

		assertEquals(List.of(ALICE, BOB, CARLA), _channel.get());

		_table.getView().sort(new SortSpec(List.of(new SortColumn(COLUMN_NAME, false))));

		assertEquals(List.of(CARLA, BOB, ALICE), _channel.get());
	}

	/**
	 * Tests that a refresh of the rows reaches the channel.
	 */
	public void testRefreshedRows() {
		Person dora = new Person("Dora", "blue");

		_rows.setElements(List.of(ALICE, dora));
		_table.refreshData();

		assertEquals(List.of(ALICE, dora), _channel.get());
	}

	/**
	 * Tests that a change of the table leaving the displayed objects and their order as they were
	 * does not notify the listeners of the channel.
	 */
	public void testUnchangedRowsDoNotNotify() {
		_rows.setElements(List.of(BOB, ALICE, CARLA));
		_table.refreshData();

		assertEquals("A refresh yielding the same rows notifies nothing.", 0, _notifications);

		_table.getView().filter(COLUMN_NAME, TextFilterState.contains("l"));
		assertEquals(List.of(ALICE, CARLA), _channel.get());
		assertEquals(1, _notifications);

		// A second criterion letting the same rows pass.
		_table.getView().filter(COLUMN_TEAM, TextFilterState.contains("e"));

		assertEquals(List.of(ALICE, CARLA), _channel.get());
		assertEquals("The displayed rows are the same, so nothing is notified.", 1, _notifications);
	}

	/**
	 * Tests that a disposed binding writes nothing to the channel any more.
	 */
	public void testDisposedBindingWritesNothing() {
		_binding.dispose();

		_table.search("bo");

		assertEquals("The channel keeps the rows displayed last.", List.of(BOB, ALICE, CARLA), _channel.get());
		assertEquals(0, _notifications);
	}

	/**
	 * Tests that the members of a collapsed group are not on the channel, the members of an
	 * expanded one are, and the group headers never are.
	 */
	public void testGroupedTable() {
		ViewChannel channel = new DefaultViewChannel("grouped");
		ListRowSource<Person> rows = new ListRowSource<>(new ArrayList<>(List.of(BOB, ALICE, CARLA)), columns());
		TableViewControl<Person> table =
			new TableViewControl<>(_context, view(rows, new GroupSpec(List.of(COLUMN_TEAM))), true);
		DisplayedRowsBinding binding = new DisplayedRowsBinding(table.getView(), channel);
		try {
			assertEquals("Group headers are no row objects.", Set.of(ALICE, BOB, CARLA), Set.copyOf(list(channel)));
			assertEquals(3, list(channel).size());

			Object redGroup = groupHeaderKey(table.getView(), ALICE);
			table.getView().setExpanded(redGroup, false);

			assertEquals("The members of the collapsed group are not displayed.", List.of(BOB), channel.get());

			table.expandRows(List.of(redGroup));

			assertEquals(Set.of(ALICE, BOB, CARLA), Set.copyOf(list(channel)));
		} finally {
			binding.dispose();
		}
	}

	/**
	 * Tests that the children of a collapsed tree node are not on the channel, and that they are,
	 * in tree order, once the node is expanded.
	 */
	public void testTree() {
		ViewChannel channel = new DefaultViewChannel("tree");
		List<Column<String, ?>> columns = List.<Column<String, ?>> of(
			DefaultColumn.<String, String> builder(COLUMN_NAME, node -> node).build());
		TreeRowSource<String, String> source = new TreeRowSource<>(new Tree(), columns);
		TableViewControl<String> table =
			new TableViewControl<>(_context, DefaultTableView.create(columns, source), true);
		DisplayedRowsBinding binding = new DisplayedRowsBinding(table.getView(), channel);
		try {
			assertEquals("The child of the collapsed node is not displayed.", List.of(PARENT, LEAF), channel.get());

			table.expandRows(List.of(PARENT));

			assertEquals("The child is displayed below its parent.", List.of(PARENT, CHILD, LEAF), channel.get());

			table.getView().setExpanded(PARENT, false);

			assertEquals(List.of(PARENT, LEAF), channel.get());
		} finally {
			binding.dispose();
		}
	}

	/** The value of the given channel as list. */
	private static List<?> list(ViewChannel channel) {
		return (List<?>) channel.get();
	}

	/** The key of the header row of the group the given member is displayed in. */
	private static Object groupHeaderKey(TableView<Person> view, Person member) {
		Object header = null;
		for (Row<Person> row : view.rows(0, view.rowCount())) {
			if (row.kind() != RowKind.DATA) {
				header = row.key();
			} else if (row.data() == member) {
				return header;
			}
		}
		throw new AssertionError("Not displayed: " + member);
	}

	/** A table over the given rows, grouped as given. */
	private static DefaultTableView<Person> view(ListRowSource<Person> rows, GroupSpec grouping) {
		List<Column<Person, ?>> columns = columns();
		TableViewState state = DefaultTableView.initialState(columns, SortSpec.NONE, Set.of());
		state.setGrouping(grouping);
		return new DefaultTableView<>(columns, rows, state, null, null);
	}

	/** The sortable and filterable name column and the filterable team column. */
	private static List<Column<Person, ?>> columns() {
		return List.of(
			DefaultColumn.<Person, String> builder(COLUMN_NAME, Person::name)
				.sort(() -> String::compareTo)
				.filter(TextColumnFilter.forStrings())
				.build(),
			DefaultColumn.<Person, String> builder(COLUMN_TEAM, Person::team)
				.filter(TextColumnFilter.forStrings())
				.build());
	}

	/** A row object of the flat and the grouped table. */
	private record Person(String name, String team) {
		// Pure data.
	}

	/** A tree of two roots, the first of which has a single child. */
	private static final class Tree implements TreeStructure<String, String> {

		private static final Map<String, List<String>> CHILDREN = Map.of(PARENT, List.of(CHILD));

		@Override
		public List<String> roots() {
			return List.of(PARENT, LEAF);
		}

		@Override
		public List<String> children(String node) {
			return CHILDREN.getOrDefault(node, List.of());
		}

		@Override
		public boolean isLeaf(String node) {
			return !CHILDREN.containsKey(node);
		}

		@Override
		public String businessObject(String node) {
			return node;
		}
	}

	/** Suite requiring the resources the table builds its column headers from. */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDisplayedRowsBinding.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
