/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.layout.view.table.ExistsIfConfig;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.TableView;

/**
 * Tests the columns of a {@code <table>} that exist only under a condition:
 * {@value ExistsIfConfig#EXISTS_IF} on a column and an embedding, and
 * {@value DynamicColumns.Config#COLUMN_EXISTS_IF} on a set of dynamic columns.
 *
 * <p>
 * The table is built from its view configuration and its condition channels are set as a view
 * sets them; what is checked is what the control sends to the client and what the column
 * selection offers.
 * </p>
 */
public class TestTableColumnExistence extends AbstractModelAccessTest {

	/** Tag of the table configurations under test. */
	private static final String TABLE = "table";

	/** Name of the channel deciding about the existence of a column. */
	private static final String SHOW = "show";

	/** Name of the channel deciding about the existence of all embedded columns. */
	private static final String OUTER = "outer";

	/** Name of the channel deciding about the existence of one embedded column. */
	private static final String INNER = "inner";

	/** State key of the columns the client displays. */
	private static final String COLUMNS = "columns";

	/** Key of the name of a column in the state the client receives. */
	private static final String COLUMN_NAME = "name";

	/** State key of the rows the client displays. */
	private static final String ROWS = "rows";

	/** Row property holding the cell controls, keyed by column name. */
	private static final String CELLS = "cells";

	/** Property of a child control descriptor holding the state of that control. */
	private static final String STATE = "state";

	/** State property of a text control holding the text it displays. */
	private static final String TEXT = "text";

	/** Name of the channel holding the project the table shows. */
	private static final String PROJECT_CHANNEL = "project";

	/** Name of the embedding of the category of a project, and the prefix of its columns. */
	private static final String EMBEDDED_CATEGORY = CATEGORY_REF + ".";

	/** Name of the dynamic columns under test. */
	private static final String DYNAMIC = "letters";

	private ReactContext _reactContext;

	private DefaultViewContext _context;

	private CountingChannel _show;

	private CountingChannel _outer;

	private CountingChannel _inner;

	/**
	 * A channel counting the listeners registered on it, so that a test sees whether a disposed
	 * table left one behind.
	 */
	private static final class CountingChannel extends DefaultViewChannel {

		private int _listeners;

		CountingChannel(String name) {
			super(name);
		}

		@Override
		public Registration addListener(ChannelListener listener) {
			Registration registration = super.addListener(listener);
			_listeners++;
			return Registration.onDispose(() -> {
				_listeners--;
				registration.dispose();
			});
		}

		/** The number of listeners registered and not yet disposed. */
		int listeners() {
			return _listeners;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_context = new DefaultViewContext(_reactContext);
		_show = channel(SHOW);
		_outer = channel(OUTER);
		_inner = channel(INNER);
		channel(PROJECT_CHANNEL).set(_project);
	}

	@Override
	protected void tearDown() throws Exception {
		_inner = null;
		_outer = null;
		_show = null;
		_context = null;
		_reactContext = null;
		super.tearDown();
	}

	private CountingChannel channel(String name) {
		CountingChannel result = new CountingChannel(name);
		_context.registerChannel(name, result);
		return result;
	}

	/**
	 * A column whose condition does not hold is neither sent nor offered from the first state on,
	 * appears when its channel makes the condition hold and disappears again with it.
	 */
	public void testColumnFollowsItsCondition() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table inputs='show' types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "'/><column attribute='" + BUDGET + "' exists-if='$show'/>"
			+ "</columns></table>");

		assertEquals(List.of(NAME), sentColumns(control));
		assertFalse(offered(control.getView()).contains(BUDGET));

		_show.set(Boolean.TRUE);

		assertEquals(List.of(NAME, BUDGET), sentColumns(control));
		assertTrue(offered(control.getView()).contains(BUDGET));

		_show.set(Boolean.FALSE);

		assertEquals(List.of(NAME), sentColumns(control));
		assertFalse(offered(control.getView()).contains(BUDGET));
		assertTrue("The arrangement of the absent column is kept.",
			control.getView().state().getColumnOrder().contains(BUDGET));
	}

	/**
	 * A condition reading a channel that only the column lists among its inputs, not the table,
	 * is followed as well.
	 */
	public void testConditionOnColumnInputOnly() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "'/>"
			+ "<computed-column name='computed' inputs='show' value='$row' exists-if='$show'/>"
			+ "</columns></table>");

		assertEquals(List.of(NAME), sentColumns(control));

		_show.set(Boolean.TRUE);

		assertEquals(List.of(NAME, "computed"), sentColumns(control));
	}

	/**
	 * The cells of a column whose value reads a channel that only the column lists among its
	 * inputs, not the table, show the new value after the channel changes.
	 */
	public void testCellsFollowColumnInputOnly() throws Exception {
		becomeUser(_root);
		_show.set("first");
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[$project]'"
			+ " inputs='project'>"
			+ "<columns><computed-column name='computed' inputs='show' value='$show'/></columns></table>");
		control.attach();
		assertEquals("first", cellText(control, "computed"));

		_show.set("second");

		assertEquals("second", cellText(control, "computed"));

		control.cleanupTree();
		assertEquals(0, _show.listeners());
	}

	/**
	 * The condition of a set of dynamic columns decides per column object.
	 */
	public void testDynamicColumnsDecidePerColumn() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "'/>"
			+ "<dynamic-columns name='" + DYNAMIC + "' inputs='show' columns=\"['a', 'b', 'c']\" value='$column'"
			+ " column-exists-if='$column != $show'/>"
			+ "</columns></table>");

		assertEquals(List.of(NAME, dynamic("a"), dynamic("b"), dynamic("c")), sentColumns(control));

		_show.set("b");

		assertEquals(List.of(NAME, dynamic("a"), dynamic("c")), sentColumns(control));
		assertFalse(offered(control.getView()).contains(dynamic("b")));

		_show.set("c");

		assertEquals(List.of(NAME, dynamic("a"), dynamic("b")), sentColumns(control));
	}

	/**
	 * The condition of an embedding applies to all its columns, the offered ones included, and a
	 * nested column's own condition has to hold in addition.
	 */
	public void testEmbeddedConditionsMustBothHold() throws Exception {
		becomeUser(_root);
		String embeddedName = EMBEDDED_CATEGORY + NAME;
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "'/>"
			+ "<embedded-columns reference='" + CATEGORY_REF + "' inputs='outer, inner' exists-if='$outer'>"
			+ "<columns><column attribute='" + NAME + "' exists-if='$inner'/></columns>"
			+ "</embedded-columns></columns></table>");

		assertEquals(List.of(NAME), sentColumns(control));
		assertTrue("Offered embedded columns follow the embedding.",
			embeddedOptions(control.getView()).isEmpty());

		_outer.set(Boolean.TRUE);

		assertEquals(List.of(NAME), sentColumns(control));
		List<String> offered = embeddedOptions(control.getView());
		assertFalse("The nested condition does not hold.", offered.contains(embeddedName));
		assertFalse("Offered embedded columns exist with the embedding.", offered.isEmpty());

		_inner.set(Boolean.TRUE);

		assertEquals(List.of(NAME, embeddedName), sentColumns(control));

		_outer.set(Boolean.FALSE);

		assertEquals(List.of(NAME), sentColumns(control));
		assertTrue(embeddedOptions(control.getView()).isEmpty());
	}

	/**
	 * A disposed table leaves no listener on the channels its conditions read.
	 */
	public void testDisposedTableLeavesNoListener() throws Exception {
		becomeUser(_root);
		TableViewControl<?> control = readOnly("<table types='" + qualified(PROJECT) + "' rows='[]'>"
			+ "<columns><column attribute='" + NAME + "'/>"
			+ "<computed-column name='computed' inputs='show' value='$row' exists-if='$show'/>"
			+ "</columns></table>");
		assertTrue(_show.listeners() > 0);

		control.cleanupTree();

		assertEquals(0, _show.listeners());
	}

	/**
	 * A table whose rows are edited in a form follows the conditions as well, in each of the tables
	 * it builds for the edit mode and the view mode, and leaves no listener behind when it is
	 * disposed.
	 */
	public void testEditableTableFollowsTheCondition() throws Exception {
		becomeUser(_root);
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		RowSetTableControl control = (RowSetTableControl) table("<table inputs='show' types='"
			+ qualified(PROJECT) + "' rows='[]' row-edit='ALL'>"
			+ "<columns><column attribute='" + NAME + "'/><column attribute='" + BUDGET + "' exists-if='$show'/>"
			+ "</columns></table>").createControl(_context);

		assertEquals(List.of(NAME), sentColumns(control.getTableControl()));

		_show.set(Boolean.TRUE);
		assertEquals(List.of(NAME, BUDGET), sentColumns(control.getTableControl()));

		assertTrue(form.enterEditMode());
		assertTrue("The table built for the edit mode starts with the existing columns.",
			sentColumns(control.getTableControl()).contains(BUDGET));

		_show.set(Boolean.FALSE);
		assertFalse(sentColumns(control.getTableControl()).contains(BUDGET));

		control.cleanupTree();
		assertEquals(0, _show.listeners());
	}

	private String dynamic(String column) {
		return DYNAMIC + "." + column;
	}

	private TableViewControl<?> readOnly(String xml) throws ConfigurationException {
		return (TableViewControl<?>) table(xml).createControl(_context);
	}

	/** The text the given control sends for the cell of its first row in the given column. */
	private static String cellText(TableViewControl<?> control, String column) {
		Map<?, ?> row = (Map<?, ?>) ((List<?>) sentState(control).get(ROWS)).get(0);
		Map<?, ?> cell = (Map<?, ?>) ((Map<?, ?>) row.get(CELLS)).get(column);
		return (String) ((Map<?, ?>) cell.get(STATE)).get(TEXT);
	}

	/** The state of the given control as the client receives it. */
	private static Map<?, ?> sentState(TableViewControl<?> control) {
		try {
			return (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control.", ex);
		}
	}

	/** The names of the columns the given control sends to the client, in display order. */
	private static List<String> sentColumns(TableViewControl<?> control) {
		Map<?, ?> state = sentState(control);
		List<String> result = new ArrayList<>();
		for (Object column : (List<?>) state.get(COLUMNS)) {
			result.add((String) ((Map<?, ?>) column).get(COLUMN_NAME));
		}
		return result;
	}

	/** The names of the columns the column selection of the given table offers. */
	private static List<String> offered(TableView<?> view) {
		return view.columnOptions().stream().map(ColumnOption::name).toList();
	}

	/** The offered columns of the embedded category. */
	private static List<String> embeddedOptions(TableView<?> view) {
		return offered(view).stream().filter(name -> name.startsWith(EMBEDDED_CATEGORY)).toList();
	}

	private static TableElement table(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableColumnExistence.class);
		TableElement result = (TableElement) context.getInstance((PolymorphicConfiguration<?>) parseTable(xml));
		context.checkErrors();
		return result;
	}

	private static ConfigurationItem parseTable(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableColumnExistence.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(TABLE, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its columns and their cells, and the labels of its values.
	 */
	public static Test suite() {
		return suiteWith(TestTableColumnExistence.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
