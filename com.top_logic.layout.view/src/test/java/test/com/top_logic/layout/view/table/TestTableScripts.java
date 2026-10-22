/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.DefaultValueModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnFunctions;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.layout.view.table.EmbeddedColumns;
import com.top_logic.layout.view.table.RowClassConfig;
import com.top_logic.layout.view.table.RowClassFunction;
import com.top_logic.layout.view.table.ScriptedFilter;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.ui.ScriptContextVariablesResolver;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests the scripts of tables and their columns, which see the inputs as variables and the values
 * the table supplies - the row, the object of a dynamic column, an entered value - under their
 * default names or the names the supplying element gives them.
 */
public class TestTableScripts extends BasicTestCase {

	private static final String TABLE = "table";

	private static final String COMPOSITION_TABLE = "composition-table";

	private static final String SCRIPTED_FILTER = ScriptedFilter.class.getName();

	private ViewContext _context;

	private ViewChannel _factor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_factor = new DefaultViewChannel("factor");
		_context.registerChannel("factor", _factor);
		_factor.set("f");
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_factor = null;
		super.tearDown();
	}

	/**
	 * Tests that the row class sees the inputs of the table and the row by the name the table gives
	 * it, in the editor and at runtime.
	 */
	public void testRowClass() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='factor as f' row-as='item' rows='[]' row-class='$item + $f'/>");
		assertScope(List.of("f", "item"), table, RowClassConfig.ROW_CLASS);
		assertEquals("rowf", RowClassFunction.compile(table).bind(List.of(_factor)).apply("row"));

		CompositionTableElement.Config composition = (CompositionTableElement.Config) parse(COMPOSITION_TABLE,
			"<composition-table attribute='parts' row-class='$row'/>");
		assertScope(List.of("row"), composition, RowClassConfig.ROW_CLASS);
		assertEquals("part", RowClassFunction.compile(composition).bind(List.of()).apply("part"));
	}

	/**
	 * Tests that the scripts of a computed column see the inputs of the column, the row by the name
	 * the table gives it, and the entered value by the name the column gives it.
	 */
	public void testComputedColumn() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='other' row-as='item' rows='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String' value-as='entered'>"
				+ "<inputs><input channel='factor' as='f'/></inputs>"
				+ "<value>$item + $f</value>"
				+ "<update>throw($item + ':' + $entered + ':' + $f)</update>"
				+ "<can-update>$item == 'yes'</can-update>"
				+ "<aggregate>$members.size() + $f</aggregate>"
				+ "<comparator>compare($right, $left)</comparator>"
				+ "</computed-column>"
				+ "<column attribute='x' aggregate='$members.size()'/>"
				+ "</columns></table>");
		ComputedColumn.Config column = (ComputedColumn.Config) table.getColumns().getColumns().get(0);

		assertScope(List.of("f", "item"), column, ComputedColumn.Config.VALUE);
		assertScope(List.of("f", "item", "entered"), column, ComputedColumn.Config.UPDATE);
		assertScope(List.of("f", "item"), column, ComputedColumn.Config.CAN_UPDATE);
		assertScope(List.of("f", AbstractColumnDeclaration.Config.MEMBERS_VARIABLE), column,
			AbstractColumnDeclaration.Config.AGGREGATE);
		assertScope(
			List.of("f", AbstractColumnDeclaration.Config.LEFT_VARIABLE,
				AbstractColumnDeclaration.Config.RIGHT_VARIABLE),
			column, AbstractColumnDeclaration.Config.COMPARATOR);

		// A column without inputs of its own sees the inputs of the table.
		AttributeColumn.Config attribute = (AttributeColumn.Config) table.getColumns().getColumns().get(1);
		assertScope(List.of("other", AbstractColumnDeclaration.Config.MEMBERS_VARIABLE), attribute,
			AbstractColumnDeclaration.Config.AGGREGATE);

		ColumnFunctions.Resolved functions =
			ColumnFunctions.compile(column, ComputedColumn.Config.VALUE, ComputedColumn.Config.UPDATE,
				ComputedColumn.Config.CAN_UPDATE).resolve(new ColumnResolution(null, _context));
		assertEquals("rowf", functions.value().apply("row"));
		assertTrue(functions.canUpdate().test("yes"));
		assertFalse(functions.canUpdate().test("no"));
		try {
			functions.update().accept("row", "new");
			fail("The update must be evaluated.");
		} catch (TopLogicException ex) {
			assertEquals(ResKey.text("row:new:f"), ex.getErrorKey());
		}

		ColumnSetup setup = single(instantiate(column).resolve(new ColumnResolution(null, _context)));
		assertEquals("2f", setup.aggregate().apply(List.of("a", "b")));
		assertTrue(setup.comparator().compare("a", "b") > 0);
	}

	/**
	 * Tests that the scripts of dynamic columns see the row by the name the table gives it, and the
	 * object of a column and an entered value by the names the declaration gives them.
	 */
	public void testDynamicColumns() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table row-as='item' rows='[]'><columns>"
				+ "<dynamic-columns name='d' column-as='m' value-as='v' type='tl.core:String'>"
				+ "<inputs><input channel='factor'/></inputs>"
				+ "<columns>[$factor + 'a', 'b']</columns>"
				+ "<column-name>$m</column-name>"
				+ "<column-label>'L' + $m</column-label>"
				+ "<column-visible>$m != 'b'</column-visible>"
				+ "<value>$item + $m</value>"
				+ "<update>throw($item + ':' + $m + ':' + $v)</update>"
				+ "<can-update>$item == $m</can-update>"
				+ "<aggregate>$members.size() + $m</aggregate>"
				+ "<comparator>compare($right, $left)</comparator>"
				+ "</dynamic-columns>"
				+ "</columns></table>");
		DynamicColumns.Config dynamic = (DynamicColumns.Config) table.getColumns().getColumns().get(0);

		assertScope(List.of("factor"), dynamic, DynamicColumns.Config.COLUMNS);
		assertScope(List.of("factor", "m"), dynamic, DynamicColumns.Config.COLUMN_NAME);
		assertScope(List.of("factor", "m"), dynamic, DynamicColumns.Config.COLUMN_LABEL);
		assertScope(List.of("factor", "m"), dynamic, DynamicColumns.Config.COLUMN_TYPE);
		assertScope(List.of("factor", "m"), dynamic, DynamicColumns.Config.COLUMN_VISIBLE);
		assertScope(List.of("factor", "item", "m"), dynamic, DynamicColumns.Config.VALUE);
		assertScope(List.of("factor", "item", "m", "v"), dynamic, DynamicColumns.Config.UPDATE);
		assertScope(List.of("factor", "item", "m"), dynamic, DynamicColumns.Config.CAN_UPDATE);
		assertScope(List.of("factor", AbstractColumnDeclaration.Config.MEMBERS_VARIABLE, "m"), dynamic,
			DynamicColumns.Config.AGGREGATE);
		assertScope(
			List.of("factor", AbstractColumnDeclaration.Config.LEFT_VARIABLE,
				AbstractColumnDeclaration.Config.RIGHT_VARIABLE),
			dynamic, DynamicColumns.Config.COMPARATOR);

		List<ColumnSetup> columns = instantiate(dynamic).resolve(new ColumnResolution(null, _context));
		assertEquals(2, columns.size());
		ColumnSetup first = columns.get(0);
		assertEquals("d.fa", first.name());
		assertEquals(ResKey.text("Lfa"), first.label());
		assertFalse(first.hiddenByDefault());
		assertTrue(columns.get(1).hiddenByDefault());
		assertEquals("rowfa", first.value().apply("row"));
		assertEquals("2fa", first.aggregate().apply(List.of("x", "y")));
		assertTrue(first.comparator().compare("a", "b") > 0);
	}

	/**
	 * Tests the variables of the computed object of an embedding and of the match expression of a
	 * scripted filter, which takes the name of the value from its column and the name of the row
	 * from its table.
	 */
	public void testEmbeddingAndFilter() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='other' row-as='item' rows='[]'><columns>"
				+ "<embedded-columns name='e' label='E' type='tl.core:String' object='$item'/>"
				+ "<column attribute='x' value-as='cell'>"
				+ "<filter class='" + SCRIPTED_FILTER + "' model='tl.core:String' match='true'/>"
				+ "</column>"
				+ "</columns></table>");
		EmbeddedColumns.Config embedded = (EmbeddedColumns.Config) table.getColumns().getColumns().get(0);
		assertScope(List.of("item"), embedded, EmbeddedColumns.Config.OBJECT);

		AttributeColumn.Config column = (AttributeColumn.Config) table.getColumns().getColumns().get(1);
		ScriptedFilter.Config filter = (ScriptedFilter.Config) column.getFilter();
		assertScope(List.of(ScriptedFilter.Config.PARAMS_VARIABLE, "cell", "item"), filter,
			ScriptedFilter.Config.MATCH);
	}

	/**
	 * Tests that an input named like a value the table supplies is a configuration error naming
	 * both and the attribute renaming the supplied value.
	 */
	public void testNameClashIsAnError() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='factor as row' rows='[]' row-class='$row'/>");
		try {
			RowClassFunction.compile(table);
			fail("An input named like the row must be rejected.");
		} catch (ConfigurationError ex) {
			String message = ex.getMessage() + " " + ex.getErrorKey();
			assertTrue(message, message.contains("factor as row"));
			assertTrue(message, message.contains("row-as"));
		}

		TableElement.Config clashingColumn = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String' value-as='row'>"
				+ "<value>$row</value>"
				+ "<update>$row</update>"
				+ "</computed-column>"
				+ "</columns></table>");
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(clashingColumn.getColumns().getColumns().get(0));
		assertTrue("Two supplied values of the same name must be rejected.", log.hasErrors());
		assertTrue(log.getError(), log.getError().contains("value-as"));

		TableElement.Config clashingMembers = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String'>"
				+ "<inputs><input channel='members'/></inputs>"
				+ "<value>$row</value>"
				+ "<aggregate>$members</aggregate>"
				+ "</computed-column>"
				+ "</columns></table>");
		log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(clashingMembers.getColumns().getColumns().get(0));
		assertTrue("An input named like the members of a group must be rejected.", log.hasErrors());
		assertTrue(log.getError(), log.getError().contains("members"));
	}

	private static <T> T instantiate(PolymorphicConfiguration<T> config) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableScripts.class);
		T result = context.getInstance(config);
		try {
			context.checkErrors();
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
		return result;
	}

	private static ColumnSetup single(List<ColumnSetup> setups) {
		assertEquals(1, setups.size());
		return setups.get(0);
	}

	private static void assertScope(List<String> expected, ConfigurationItem config, String property) {
		assertEquals("Editor scope of '" + property + "'.", expected, ScriptContextVariablesResolver
			.resolve(new DefaultValueModel(config, config.descriptor().getProperty(property))));
		assertEquals("Runtime scope of '" + property + "'.", expected,
			InputScript.variables(config, config.descriptor().getProperty(property)));
	}

	private static ConfigurationItem parse(String tag, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableScripts.class);
		Map<String, ConfigurationDescriptor> descriptors = Map.of(
			TABLE, TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class),
			COMPOSITION_TABLE, TypedConfiguration.getConfigurationDescriptor(CompositionTableElement.Config.class));
		ConfigurationReader reader =
			new ConfigurationReader(context, Collections.singletonMap(tag, descriptors.get(tag)));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts are compiled and evaluated against the application model and the
	 *           knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestTableScripts.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
