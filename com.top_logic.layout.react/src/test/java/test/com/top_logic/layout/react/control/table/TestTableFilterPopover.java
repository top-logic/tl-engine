/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.RecordedCommand;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.table.ColumnFilterUI;
import com.top_logic.layout.react.control.table.FilterAllArguments;
import com.top_logic.layout.react.control.table.FilterToggleArguments;
import com.top_logic.layout.react.control.table.OpenFilterArguments;
import com.top_logic.layout.react.control.table.ReactFilterFormControl;
import com.top_logic.layout.react.control.table.ReactFilterListControl;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.TypingFieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.table.Column;
import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Option;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

/**
 * Tests the filter of a column as a popover at its funnel: the {@link TableViewControl} holds the
 * content of one popover at a time in its state, every change in it filters the table at once, and
 * the content is disposed when it is replaced or closed.
 */
public class TestTableFilterPopover extends TestCase {

	private static final String COLUMN_NAME = "name";

	private static final String COLUMN_STATUS = "status";

	private static final String OPEN = "open";

	private static final String CLOSED = "closed";

	private static final String WAITING = "waiting";

	private static final String STATE_FILTER_POPOVER = "filterPopover";

	private static final String STATE_COLUMNS = "columns";

	private static final String POPOVER_COLUMN = "column";

	private static final String POPOVER_CONTENT = "content";

	private static final String CMD_OPEN_FILTER = "openFilter";

	private static final String CMD_CLOSE_FILTER = "closeFilter";

	private static final String CMD_TOGGLE = "toggle";

	private static final String CMD_ALL = "all";

	private static final String CMD_INVERT = "invert";

	private static final String CMD_CLEAR = "clear";

	private static final String CMD_APPLY = "apply";

	private static final String LIST_ENTRIES = "entries";

	private static final String LIST_SEARCHABLE = "searchable";

	private static final String ACTIVE = "active";

	private static final String FORM_APPLIES = "applies";

	private static final String ENTRY_KEY = "key";

	private static final String ENTRY_LABEL = "label";

	private static final String ENTRY_COUNT = "count";

	private static final String ENTRY_CHECKED = "checked";

	/** The status values the status column offers, in their order. */
	private static final List<String> STATUS_VALUES = List.of(OPEN, CLOSED, WAITING);

	/**
	 * A row business object.
	 *
	 * @param name
	 *        The row label, filtered by text.
	 * @param status
	 *        The status, filtered by a value list.
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

	private TableView<Item> _view;

	private TestTable _table;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		List<Item> rows = List.of(new Item("alpha", OPEN), new Item("beta", CLOSED), new Item("gamma", OPEN),
			new Item("delta", WAITING));
		_view = view(rows, statusOptions());
		_table = new TestTable(context(), _view);
	}

	/**
	 * Tests that the funnel opens the content of the popover into the state of the table - without a
	 * dialog manager, which the test context does not have: nothing is shown as a dialog any more.
	 */
	public void testOpenFilterPutsPopover() {
		assertNull(_table.clientState(STATE_FILTER_POPOVER));

		openFilter(COLUMN_STATUS);

		Map<?, ?> popover = popover();
		assertEquals(COLUMN_STATUS, popover.get(POPOVER_COLUMN));
		assertTrue(popover.get(POPOVER_CONTENT) instanceof ReactFilterListControl);
	}

	/**
	 * Tests that opening the filter of another column replaces the popover and disposes the content
	 * of the first one.
	 */
	public void testSecondOpenReplacesAndDisposes() {
		openFilter(COLUMN_STATUS);
		ReactControl first = content();

		openFilter(COLUMN_NAME);

		assertEquals(COLUMN_NAME, popover().get(POPOVER_COLUMN));
		assertNotSame(first, content());
		assertTrue("The replaced content is disposed.", first.isDisposed());
	}

	/** Tests that closing removes the popover from the state and disposes its content. */
	public void testCloseFilterRemovesAndDisposes() {
		openFilter(COLUMN_STATUS);
		ReactControl content = content();

		_table.executeClientCommand(CMD_CLOSE_FILTER, Map.of());

		assertNull(_table.clientState(STATE_FILTER_POPOVER));
		assertTrue("The closed content is disposed.", content.isDisposed());
		assertNull("The filter stays as it is.", _view.state().getFilters().get(COLUMN_STATUS));
	}

	/** Tests that a filter of the column a popover is open for is not changed by closing it. */
	public void testCloseKeepsFilter() {
		openFilter(COLUMN_STATUS);
		toggle(CLOSED);

		_table.executeClientCommand(CMD_CLOSE_FILTER, Map.of());

		assertEquals(hidden(CLOSED), _view.state().getFilters().get(COLUMN_STATUS));
	}

	/**
	 * Tests the entries of the value list: each value with its label, the number of rows it matches
	 * and checked, as long as nothing is filtered.
	 */
	public void testValueListEntries() {
		openFilter(COLUMN_STATUS);

		List<Map<?, ?>> entries = entries();
		assertEquals(3, entries.size());
		assertEquals(STATUS_VALUES.get(0).toUpperCase(), entries.get(0).get(ENTRY_LABEL));
		assertEquals(Integer.valueOf(2), entries.get(0).get(ENTRY_COUNT));
		assertEquals(Integer.valueOf(1), entries.get(1).get(ENTRY_COUNT));
		for (Map<?, ?> entry : entries) {
			assertEquals(Boolean.TRUE, entry.get(ENTRY_CHECKED));
		}
		assertEquals("Without a filter there is nothing to remove.", Boolean.FALSE, list().scriptingScalarState()
			.get(ACTIVE));
		assertEquals("The list is named after its column.", COLUMN_STATUS.toUpperCase(),
			list().scriptingScalarState().get("label"));
	}

	/**
	 * Tests that unchecking a value filters at once: the value is hidden, the filter is stored as the
	 * negation of the unchecked values, and the funnel of the column tells the filter in force.
	 */
	public void testToggleFiltersAtOnce() {
		openFilter(COLUMN_STATUS);

		toggle(CLOSED);

		assertEquals(hidden(CLOSED), _view.state().getFilters().get(COLUMN_STATUS));
		assertEquals(3, _view.rowCount());
		assertEquals(Boolean.TRUE, columnState(COLUMN_STATUS).get("filterActive"));
		assertEquals(Boolean.FALSE, entry(CLOSED).get(ENTRY_CHECKED));
		assertEquals(Boolean.TRUE, list().scriptingScalarState().get(ACTIVE));

		toggle(CLOSED);

		assertNull("Everything checked is no filter.", _view.state().getFilters().get(COLUMN_STATUS));
		assertEquals(Boolean.FALSE, columnState(COLUMN_STATUS).get("filterActive"));
		assertEquals(Boolean.FALSE, list().scriptingScalarState().get(ACTIVE));
	}

	/**
	 * Tests that checking all values removes the filter, and that unchecking all of them hides every
	 * row - a filter that is kept, not dropped as empty.
	 */
	public void testAll() {
		openFilter(COLUMN_STATUS);

		all(false);
		assertEquals(hidden(OPEN, CLOSED, WAITING), _view.state().getFilters().get(COLUMN_STATUS));
		assertEquals(0, _view.rowCount());
		for (Map<?, ?> entry : entries()) {
			assertEquals(Boolean.FALSE, entry.get(ENTRY_CHECKED));
		}

		all(true);
		assertNull(_view.state().getFilters().get(COLUMN_STATUS));
		assertEquals(4, _view.rowCount());
	}

	/** Tests that inverting the selection turns every check mark. */
	public void testInvert() {
		openFilter(COLUMN_STATUS);
		toggle(CLOSED);

		filterList().executeClientCommand(CMD_INVERT, Map.of());

		assertEquals(hidden(OPEN, WAITING), _view.state().getFilters().get(COLUMN_STATUS));
		assertEquals(1, _view.rowCount());
		assertEquals(Boolean.TRUE, entry(CLOSED).get(ENTRY_CHECKED));
		assertEquals(Boolean.FALSE, entry(OPEN).get(ENTRY_CHECKED));
	}

	/** Tests that removing the filter checks every value again. */
	public void testClearChecksAll() {
		openFilter(COLUMN_STATUS);
		toggle(CLOSED);
		toggle(OPEN);

		filterList().executeClientCommand(CMD_CLEAR, Map.of());

		assertNull(_view.state().getFilters().get(COLUMN_STATUS));
		for (Map<?, ?> entry : entries()) {
			assertEquals(Boolean.TRUE, entry.get(ENTRY_CHECKED));
		}
	}

	/**
	 * Tests that a filter saved before the value list - "only these" - shows these checked and the
	 * others unchecked, and that the next change stores the new form.
	 */
	public void testPreviousFilterShownAsChecks() {
		_view.filter(COLUMN_STATUS, new OptionsFilterState(Set.of(OPEN)));
		openFilter(COLUMN_STATUS);

		assertEquals(Boolean.TRUE, entry(OPEN).get(ENTRY_CHECKED));
		assertEquals(Boolean.FALSE, entry(CLOSED).get(ENTRY_CHECKED));
		assertEquals(Boolean.FALSE, entry(WAITING).get(ENTRY_CHECKED));
		assertEquals(Boolean.TRUE, list().scriptingScalarState().get(ACTIVE));

		toggle(WAITING);

		assertEquals(hidden(CLOSED), _view.state().getFilters().get(COLUMN_STATUS));
	}

	/** Tests that the value list offers its search from eight values on. */
	public void testSearchableFromEightValues() {
		openFilter(COLUMN_STATUS);
		assertEquals(Boolean.FALSE, list().scriptingScalarState().get(LIST_SEARCHABLE));

		List<String> many = List.of("a", "b", "c", "d", "e", "f", "g", "h");
		List<Item> rows = new ArrayList<>();
		for (String value : many) {
			rows.add(new Item(value, value));
		}
		TestTable table = new TestTable(context(), view(rows, options(many.subList(0, 7))));
		table.executeClientCommand(CMD_OPEN_FILTER, Map.of(OpenFilterArguments.COLUMN, COLUMN_STATUS));
		assertEquals(Boolean.FALSE,
			((ReactControl) ((Map<?, ?>) table.clientState(STATE_FILTER_POPOVER)).get(POPOVER_CONTENT))
				.scriptingScalarState().get(LIST_SEARCHABLE));

		table = new TestTable(context(), view(rows, options(many)));
		table.executeClientCommand(CMD_OPEN_FILTER, Map.of(OpenFilterArguments.COLUMN, COLUMN_STATUS));
		assertEquals(Boolean.TRUE,
			((ReactControl) ((Map<?, ?>) table.clientState(STATE_FILTER_POPOVER)).get(POPOVER_CONTENT))
				.scriptingScalarState().get(LIST_SEARCHABLE));
	}

	/**
	 * Tests that a text filter filters once the value of its field reaches the server, without a
	 * further command - and that its field holds a typed value back for the typing pause and
	 * commits on Enter.
	 */
	public void testTextFieldFiltersOnValue() {
		openFilter(COLUMN_NAME);
		assertTrue(content() instanceof ReactFilterFormControl);
		assertEquals(Boolean.FALSE, content().scriptingScalarState().get(FORM_APPLIES));
		assertEquals(Boolean.FALSE, content().scriptingScalarState().get(ACTIVE));
		ReactTextInputControl pattern = find(content(), ReactTextInputControl.class).get(0);
		// The pause is presentation, sent to the client but kept out of the scripting projection.
		assertTrue(pattern.stateAsJSON(),
			pattern.stateAsJSON().contains(
				"\"" + TypingFieldState.DEBOUNCE_MS__PROP + "\":" + TableViewControl.FILTER_TYPING_PAUSE_MS));
		assertEquals(Boolean.TRUE, pattern.scriptingScalarState().get(TypingFieldState.COMMIT_EDITS__PROP));

		valueChanged(pattern, "mm");

		FilterState filter = _view.state().getFilters().get(COLUMN_NAME);
		assertTrue(filter instanceof TextFilterState);
		assertEquals("mm", ((TextFilterState) filter).pattern());
		assertEquals(1, _view.rowCount());
		assertEquals(Boolean.TRUE, columnState(COLUMN_NAME).get("filterActive"));
		assertEquals(Boolean.TRUE, content().scriptingScalarState().get(ACTIVE));
	}

	/**
	 * Tests that Enter - the value, then the commit of the field - filters by the typed value.
	 */
	public void testCommitFilters() {
		openFilter(COLUMN_NAME);
		ReactTextInputControl pattern = find(content(), ReactTextInputControl.class).get(0);

		valueChanged(pattern, "et");
		pattern.executeClientCommand(ReactFormFieldControl.COMMIT_COMMAND, Map.of());

		assertEquals("et", ((TextFilterState) _view.state().getFilters().get(COLUMN_NAME)).pattern());
		assertEquals(1, _view.rowCount());
	}

	/**
	 * Tests that removing the filter of a form clears the filter and shows the form empty again.
	 */
	public void testClearForm() {
		openFilter(COLUMN_NAME);
		valueChanged(find(content(), ReactTextInputControl.class).get(0), "mm");

		content().executeClientCommand(CMD_CLEAR, Map.of());

		assertNull(_view.state().getFilters().get(COLUMN_NAME));
		assertEquals(4, _view.rowCount());
		ReactTextInputControl pattern = find(content(), ReactTextInputControl.class).get(0);
		assertEquals("", String.valueOf(pattern.getFieldModel().getValue()));
		assertEquals(Boolean.FALSE, content().scriptingScalarState().get(ACTIVE));
	}

	/**
	 * Tests that the filter UI of an application keeps its explicit apply: a change of its fields
	 * does not filter, the command of its apply button does.
	 */
	public void testColumnFilterUIAppliesOnApply() {
		_table.setFilterUI(COLUMN_NAME, new ColumnFilterUI() {
			private AbstractFieldModel _model;

			@Override
			public ReactControl buildForm(ReactContext context) {
				_model = new AbstractFieldModel("");
				return new ReactTextInputControl(context, _model);
			}

			@Override
			public FilterState read() {
				return TextFilterState.contains(String.valueOf(_model.getValue()));
			}
		});
		openFilter(COLUMN_NAME);
		assertTrue(content() instanceof ReactFilterFormControl);
		assertEquals(Boolean.TRUE, content().scriptingScalarState().get(FORM_APPLIES));

		valueChanged(find(content(), ReactTextInputControl.class).get(0), "mm");
		assertNull("A field of the application does not filter by itself.",
			_view.state().getFilters().get(COLUMN_NAME));

		content().executeClientCommand(CMD_APPLY, Map.of());
		assertEquals("mm", ((TextFilterState) _view.state().getFilters().get(COLUMN_NAME)).pattern());
		assertEquals(1, _view.rowCount());
	}

	/**
	 * Tests that a click on a value is recorded with the label of the value for the reader of the
	 * script, and replays by the key - the position of the value among the options, the identity the
	 * options filter itself persists a selection by.
	 */
	public void testToggleRecordedWithLabel() {
		openFilter(COLUMN_STATUS);
		Object key = entry(CLOSED).get(ENTRY_KEY);

		RecordedCommand recorded =
			filterList().recordCommand(CMD_TOGGLE, Map.of(FilterToggleArguments.KEY, key));

		FilterToggleArguments args = (FilterToggleArguments) recorded.command();
		assertEquals(CMD_TOGGLE, args.getName());
		assertEquals(key, args.getKey());
		assertEquals(CLOSED.toUpperCase(), args.getLabel());

		filterList().executeClientCommand(CMD_TOGGLE,
			Map.of(FilterToggleArguments.KEY, key, FilterToggleArguments.LABEL, CLOSED.toUpperCase()));
		assertEquals(hidden(CLOSED), _view.state().getFilters().get(COLUMN_STATUS));
	}

	/**
	 * Tests that an open value list follows a filter removed elsewhere - the chip of the filter bar
	 * clearing every filter: every value is checked again and there is nothing left to remove.
	 */
	public void testValueListFollowsClearFilter() {
		openFilter(COLUMN_STATUS);
		toggle(CLOSED);

		_table.executeClientCommand("clearFilter", Map.of());

		assertNull(_view.state().getFilters().get(COLUMN_STATUS));
		for (Map<?, ?> entry : entries()) {
			assertEquals(Boolean.TRUE, entry.get(ENTRY_CHECKED));
		}
		assertEquals(Boolean.FALSE, list().scriptingScalarState().get(ACTIVE));
	}

	/**
	 * Tests that the counts of an open value list follow the other criteria when the rows are
	 * rebuilt: a filter of another column set from outside, then the data refreshed.
	 */
	public void testValueListCountsFollowRefresh() {
		openFilter(COLUMN_STATUS);
		assertEquals(Integer.valueOf(2), entry(OPEN).get(ENTRY_COUNT));

		_view.filter(COLUMN_NAME, TextFilterState.contains("mm"));
		_table.refreshData();

		assertEquals(Integer.valueOf(1), entry(OPEN).get(ENTRY_COUNT));
		assertEquals(Integer.valueOf(0), entry(CLOSED).get(ENTRY_COUNT));
		assertEquals("The own filter is unchanged.", Boolean.FALSE, list().scriptingScalarState().get(ACTIVE));
	}

	/** Tests that an open filter form follows a filter removed elsewhere: nothing left to remove. */
	public void testFilterFormFollowsClearFilter() {
		openFilter(COLUMN_NAME);
		valueChanged(find(content(), ReactTextInputControl.class).get(0), "mm");
		assertEquals(Boolean.TRUE, content().scriptingScalarState().get(ACTIVE));

		_table.clearFilter();

		assertEquals(Boolean.FALSE, content().scriptingScalarState().get(ACTIVE));
	}

	/**
	 * Tests that a table leaving the display closes its filter popover and releases the content, so
	 * that the popover does not open again when the table is displayed again.
	 */
	public void testDetachClosesPopover() {
		_table.attach();
		openFilter(COLUMN_STATUS);
		ReactControl content = content();

		_table.detach();

		assertNull(_table.clientState(STATE_FILTER_POPOVER));
		assertTrue(content.isDisposed());
	}

	/** Tests that disposing the table disposes the content of its open filter popover. */
	public void testDisposeReleasesPopover() {
		_table.attach();
		openFilter(COLUMN_NAME);
		ReactControl content = content();
		ReactTextInputControl field = find(content, ReactTextInputControl.class).get(0);

		_table.cleanupTree();

		assertTrue(content.isDisposed());
		assertTrue(field.isDisposed());
	}

	/** Tests that a column without a filter opens nothing. */
	public void testNoPopoverWithoutFilter() {
		openFilter("unknown");
		assertNull(_table.clientState(STATE_FILTER_POPOVER));
	}

	private void openFilter(String column) {
		_table.executeClientCommand(CMD_OPEN_FILTER, Map.of(OpenFilterArguments.COLUMN, column));
	}

	private void toggle(String value) {
		filterList().executeClientCommand(CMD_TOGGLE, Map.of(FilterToggleArguments.KEY, entry(value).get(ENTRY_KEY)));
	}

	private void all(boolean checked) {
		filterList().executeClientCommand(CMD_ALL, Map.of(FilterAllArguments.CHECKED, Boolean.valueOf(checked)));
	}

	private static void valueChanged(ReactTextInputControl field, String value) {
		field.executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldValueArguments.VALUE, value));
	}

	private Map<?, ?> popover() {
		return (Map<?, ?>) _table.clientState(STATE_FILTER_POPOVER);
	}

	private ReactControl content() {
		return (ReactControl) popover().get(POPOVER_CONTENT);
	}

	private ReactFilterListControl filterList() {
		return (ReactFilterListControl) content();
	}

	private ReactControl list() {
		return content();
	}

	@SuppressWarnings("unchecked")
	private List<Map<?, ?>> entries() {
		return (List<Map<?, ?>>) list().scriptingScalarState().get(LIST_ENTRIES);
	}

	private Map<?, ?> entry(String value) {
		String label = value.toUpperCase();
		for (Map<?, ?> entry : entries()) {
			if (label.equals(entry.get(ENTRY_LABEL))) {
				return entry;
			}
		}
		throw new AssertionError("No entry " + label);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> columnState(String column) {
		for (Map<String, Object> state : (List<Map<String, Object>>) _table.clientState(STATE_COLUMNS)) {
			if (column.equals(state.get("name"))) {
				return state;
			}
		}
		throw new AssertionError("No column " + column);
	}

	private static NegatedFilterState hidden(String... values) {
		return new NegatedFilterState(new OptionsFilterState(new LinkedHashSet<>(List.of(values))));
	}

	/** The controls of the given type in the tree of the given control, in the order they are displayed. */
	private static <T> List<T> find(ReactControl root, Class<T> type) {
		List<T> result = new ArrayList<>();
		collect(root, type, result);
		return result;
	}

	private static <T> void collect(ReactControl control, Class<T> type, List<T> result) {
		if (type.isInstance(control)) {
			result.add(type.cast(control));
		}
		for (ReactControl child : control.displayedChildren()) {
			collect(child, type, result);
		}
	}

	private static List<Option> statusOptions() {
		return options(STATUS_VALUES);
	}

	private static List<Option> options(List<String> values) {
		List<Option> result = new ArrayList<>();
		for (String value : values) {
			result.add(new Option(value, ResKey.text(value.toUpperCase())));
		}
		return result;
	}

	private static TableView<Item> view(List<Item> rows, List<Option> statusOptions) {
		List<Column<Item, ?>> columns = List.of(
			DefaultColumn.<Item, String> builder(COLUMN_NAME, Item::name).filter(TextColumnFilter.forStrings())
				.build(),
			DefaultColumn.<Item, String> builder(COLUMN_STATUS, Item::status)
				.label(ResKey.text(COLUMN_STATUS.toUpperCase()))
				.filter(new OptionsColumnFilter<String>(statusOptions))
				.build());
		return DefaultTableView.create(columns, new ListRowSource<>(new ArrayList<>(rows), columns));
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * Suite requiring the resources the table builds its texts from, and the theme the fields of a
	 * filter form take their icons from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableFilterPopover.class,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE, ThemeFactory.Module.INSTANCE));
	}

}
