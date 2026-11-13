/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
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
import com.top_logic.layout.view.I18NConstants;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.InputScript;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.CompositionTableElement;
import com.top_logic.layout.view.element.ResourceDisplayByExpression;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.table.AbstractColumnDeclaration;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnFunctions;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.layout.view.table.EmbeddedColumns;
import com.top_logic.layout.view.table.RowClassConfig;
import com.top_logic.layout.view.table.RowFunction;
import com.top_logic.layout.view.table.RowRoleConfig;
import com.top_logic.layout.view.table.ScriptedFilter;
import com.top_logic.model.annotate.ui.AnnotationValueColorProvider;
import com.top_logic.model.annotate.ui.ValueColor;
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

	private static final String TREE_TABLE = "tree-table";

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
		assertEquals("rowf", RowFunction.rowClass(table).bind(List.of(_factor)).apply("row"));

		CompositionTableElement.Config composition = (CompositionTableElement.Config) parse(COMPOSITION_TABLE,
			"<composition-table attribute='parts' row-class='$row'/>");
		assertScope(List.of("row"), composition, RowClassConfig.ROW_CLASS);
		assertEquals("part", RowFunction.rowClass(composition).bind(List.of()).apply("part"));

		TreeTableElement.Config treeTable = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table inputs='factor as f' row-as='item' root='1' children='[]' row-class='[$item, $f]'/>");
		assertScope(List.of("f", "item"), treeTable, RowClassConfig.ROW_CLASS);
		assertEquals("row f", RowFunction.rowClass(treeTable).bind(List.of(_factor)).apply("row"));

		TreeTableElement.Config plainTree = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table root='1' children='[]'/>");
		assertNull("A tree table without a row class marks no row.",
			RowFunction.rowClass(plainTree).bind(List.of()));
	}

	/**
	 * Tests that the row role sees the same variables as the row class, and that its result names
	 * the color role of the row.
	 */
	public void testRowRole() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='factor as f' row-as='item' rows='[]' row-role=\"$item == $f ? 'warning' : null\"/>");
		assertScope(List.of("f", "item"), table, RowRoleConfig.ROW_ROLE);
		assertEquals(ValueColor.WARNING, RowFunction.rowRole(table).bind(List.of(_factor)).apply("f"));
		assertNull(RowFunction.rowRole(table).bind(List.of(_factor)).apply("other"));

		CompositionTableElement.Config composition = (CompositionTableElement.Config) parse(COMPOSITION_TABLE,
			"<composition-table attribute='parts' row-role='$row'/>");
		assertScope(List.of("row"), composition, RowRoleConfig.ROW_ROLE);
		assertEquals(ValueColor.CATEGORY_3, RowFunction.rowRole(composition).bind(List.of()).apply("category-3"));
		assertNull("An empty text names no role.", RowFunction.rowRole(composition).bind(List.of()).apply(""));

		TreeTableElement.Config treeTable = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table inputs='factor as f' row-as='item' root='1' children='[]' row-role=\"'error'\"/>");
		assertScope(List.of("f", "item"), treeTable, RowRoleConfig.ROW_ROLE);
		assertEquals(ValueColor.ERROR, RowFunction.rowRole(treeTable).bind(List.of(_factor)).apply("row"));

		TreeTableElement.Config plainTree = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table root='1' children='[]'/>");
		assertNull("A tree table without a row role tints no row.",
			RowFunction.rowRole(plainTree).bind(List.of()));
	}

	/**
	 * Tests that a row role naming no role is an error naming the misspelled role.
	 */
	public void testUnknownRowRoleIsAnError() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table rows='[]' row-role=\"'warn'\"/>");
		try {
			RowFunction.rowRole(table).bind(List.of()).apply("row");
			fail("A misspelled role must be rejected.");
		} catch (TopLogicException ex) {
			assertUnknownRole("warn", ex);
		}
	}

	/**
	 * Tests the conversion of a script result to a color role.
	 */
	public void testValueColor() {
		assertNull(AbstractColumnDeclaration.valueColor(null));
		assertNull(AbstractColumnDeclaration.valueColor(""));
		assertEquals(ValueColor.SUCCESS, AbstractColumnDeclaration.valueColor("success"));
		assertEquals(ValueColor.CATEGORY_8, AbstractColumnDeclaration.valueColor("category-8"));
		assertEquals(ValueColor.INFO, AbstractColumnDeclaration.valueColor(ValueColor.INFO));
		try {
			AbstractColumnDeclaration.valueColor("WARNING");
			fail("A role is named by its external name.");
		} catch (TopLogicException ex) {
			assertUnknownRole("WARNING", ex);
		}
		try {
			AbstractColumnDeclaration.valueColor(42L);
			fail("A number names no role.");
		} catch (TopLogicException ex) {
			assertUnknownRole(42L, ex);
		}
	}

	private static void assertUnknownRole(Object value, TopLogicException ex) {
		assertEquals(I18NConstants.ERROR_UNKNOWN_COLOR_ROLE__VALUE_ROLES.fill(value, "").plain(),
			ex.getErrorKey().plain());
		Object[] arguments = ex.getErrorKey().arguments();
		assertEquals(value, arguments[0]);
		String roles = (String) arguments[1];
		for (ValueColor color : ValueColor.values()) {
			assertTrue(roles, roles.contains(color.getExternalName()));
		}
	}

	/**
	 * Tests that the cell role of a column sees the variables of its cell class, and that its result
	 * names the color role of the cell - also for an embedded column, which sees the embedded
	 * object as its row.
	 */
	public void testCellRole() throws Exception {
		TreeTableElement.Config treeTable = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table inputs='other' row-as='item' root='1' children='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String' value-as='v'>"
				+ "<inputs><input channel='factor' as='f'/></inputs>"
				+ "<value>$item</value>"
				+ "<cell-role>$v == 'none' ? null : ($v == $f ? 'success' : $item)</cell-role>"
				+ "</computed-column>"
				+ "<column attribute='x' cell-role='$item'/>"
				+ "</columns></tree-table>");
		ComputedColumn.Config column = (ComputedColumn.Config) treeTable.getColumns().getColumns().get(0);
		assertScope(List.of("f", "item", "v"), column, AbstractColumnDeclaration.Config.CELL_ROLE);

		AttributeColumn.Config attribute = (AttributeColumn.Config) treeTable.getColumns().getColumns().get(1);
		assertScope(List.of("other", "item", "value"), attribute, AbstractColumnDeclaration.Config.CELL_ROLE);
		assertEquals("A column computing its role follows the inputs of the table.",
			List.of("other"), InputRef.names(instantiate(attribute).inputs()));

		ComputedColumn computed = instantiate(column);
		assertEquals("A column computing its role follows its inputs.",
			List.of("f"), InputRef.names(computed.inputs()));
		ColumnSetup setup = single(computed.resolve(new ColumnResolution(List.of(), _context)));
		assertEquals(ValueColor.SUCCESS, setup.cellRole().apply("r", "f"));
		assertEquals(ValueColor.BRAND, setup.cellRole().apply("brand", "a"));
		assertNull(setup.cellRole().apply("r", "none"));
		try {
			setup.cellRole().apply("errr", "a");
			fail("A misspelled role must be rejected.");
		} catch (TopLogicException ex) {
			assertUnknownRole("errr", ex);
		}

		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String'><value>$row</value></computed-column>"
				+ "</columns></table>");
		ColumnSetup plain = single(instantiate(table.getColumns().getColumns().get(0))
			.resolve(new ColumnResolution(List.of(), _context)));
		assertNull("A column without a cell role tints no cell.", plain.cellRole());

		// An embedded column computes its cell role with the embedded object as its row.
		TableElement.Config embedding = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<embedded-columns name='e' label='E' type='tl.model:TLClass' object='$row + \"-1\"'>"
				+ "<computed-column name='c' type='tl.core:String' value='$row' cell-role='$row'/>"
				+ "</embedded-columns>"
				+ "</columns></table>");
		List<ColumnSetup> embeddedSetups = instantiate(embedding.getColumns().getColumns().get(0))
			.resolve(new ColumnResolution(List.of(), _context));
		assertEquals(ValueColor.CATEGORY_1, embeddedSetups.get(0).cellRole().apply("category", "v"));
		assertNull(embeddedSetups.get(1).cellRole());
	}

	/**
	 * Tests that the cell class of a column sees the inputs of the column, the row by the name the
	 * table gives it and the value by the name the column gives it, and that its result becomes the
	 * class of the cell: nothing and an empty list none, a list all of its members.
	 */
	public void testCellClass() throws Exception {
		TreeTableElement.Config treeTable = (TreeTableElement.Config) parse(TREE_TABLE,
			"<tree-table inputs='other' row-as='item' root='1' children='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String' value-as='v'>"
				+ "<inputs><input channel='factor' as='f'/></inputs>"
				+ "<value>$item</value>"
				+ "<cell-class>$v == 'none' ? null : ($v == 'empty' ? [] : [$item, $v + $f])</cell-class>"
				+ "</computed-column>"
				+ "<column attribute='x' cell-class='$row'/>"
				+ "</columns></tree-table>");
		ComputedColumn.Config column = (ComputedColumn.Config) treeTable.getColumns().getColumns().get(0);
		assertScope(List.of("f", "item", "v"), column, AbstractColumnDeclaration.Config.CELL_CLASS);

		// A column without inputs of its own sees the inputs of the table.
		AttributeColumn.Config attribute = (AttributeColumn.Config) treeTable.getColumns().getColumns().get(1);
		assertScope(List.of("other", "item", "value"), attribute, AbstractColumnDeclaration.Config.CELL_CLASS);

		ColumnSetup setup = single(instantiate(column).resolve(new ColumnResolution(List.of(), _context)));
		assertEquals("r af", setup.cellClass().apply("r", "a"));
		assertNull(setup.cellClass().apply("r", "none"));
		assertNull(setup.cellClass().apply("r", "empty"));

		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String'><value>$row</value></computed-column>"
				+ "</columns></table>");
		ColumnSetup plain = single(instantiate(table.getColumns().getColumns().get(0))
			.resolve(new ColumnResolution(List.of(), _context)));
		assertNull("A column without a cell class marks no cell.", plain.cellClass());

		// An embedded column computes its cell class with the embedded object as its row.
		TableElement.Config embedding = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<embedded-columns name='e' label='E' type='tl.model:TLClass' object='$row + \"/e\"'>"
				+ "<computed-column name='c' type='tl.core:String' value='$row' cell-class='$row + \":\" + $value'/>"
				+ "</embedded-columns>"
				+ "</columns></table>");
		List<ColumnSetup> embeddedSetups = instantiate(embedding.getColumns().getColumns().get(0))
			.resolve(new ColumnResolution(List.of(), _context));
		ColumnSetup embedded = embeddedSetups.get(0);
		assertEquals("e.c", embedded.name());
		assertEquals("r/e:v", embedded.cellClass().apply("r", "v"));
		// The offered attribute columns of the embedded type declare no cell class.
		assertNull(embeddedSetups.get(1).cellClass());
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
				ComputedColumn.Config.CAN_UPDATE).resolve(new ColumnResolution(List.of(), _context));
		assertEquals("rowf", functions.value().apply("row"));
		assertTrue(functions.canUpdate().test("yes"));
		assertFalse(functions.canUpdate().test("no"));
		try {
			functions.update().accept("row", "new");
			fail("The update must be evaluated.");
		} catch (TopLogicException ex) {
			assertEquals(ResKey.text("row:new:f"), ex.getErrorKey());
		}

		ColumnSetup setup = single(instantiate(column).resolve(new ColumnResolution(List.of(), _context)));
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
				+ "<cell-class>$v == 'none' ? null : [$item, $m + $v]</cell-class>"
				+ "<cell-role>$v == 'none' ? null : $v</cell-role>"
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
		assertScope(List.of("factor", "item", "m", "v"), dynamic, DynamicColumns.Config.CELL_CLASS);
		assertScope(List.of("factor", "item", "m", "v"), dynamic, DynamicColumns.Config.CELL_ROLE);

		List<ColumnSetup> columns = instantiate(dynamic).resolve(new ColumnResolution(List.of(), _context));
		assertEquals(2, columns.size());
		ColumnSetup first = columns.get(0);
		assertEquals("d.fa", first.name());
		assertEquals(ResKey.text("Lfa"), first.label());
		assertFalse(first.hiddenByDefault());
		assertTrue(columns.get(1).hiddenByDefault());
		assertEquals("rowfa", first.value().apply("row"));
		assertEquals("2fa", first.aggregate().apply(List.of("x", "y")));
		assertTrue(first.comparator().compare("a", "b") > 0);
		assertEquals("row fax", first.cellClass().apply("row", "x"));
		assertNull(first.cellClass().apply("row", "none"));
		assertEquals(ValueColor.INFO, first.cellRole().apply("row", "info"));
		assertNull(first.cellRole().apply("row", "none"));
	}

	/**
	 * Tests that the width of dynamic columns is computed per column with the variables of the
	 * column label: a constant number is the width of every column, an expression gives each
	 * column its own width, and nothing, {@code 0} or no width at all keep the width the type of
	 * the values derives.
	 */
	public void testDynamicColumnsWidth() throws Exception {
		String columns = "<inputs><input channel='factor'/></inputs>"
			+ "<columns>['name', 'start', 'end']</columns>"
			+ "<column-type>$m == 'name' ? `tl.core:String` : `tl.core:Date`</column-type>"
			+ "<value>$row</value>";

		DynamicColumns.Config constant = dynamicColumns("width='130'", columns);
		assertEquals("A constant width applies to every column.", List.of(130, 130, 130),
			widths(constant));

		DynamicColumns.Config computed =
			dynamicColumns("", columns + "<width>$m == 'name' ? 260 : 130</width>");
		assertScope(List.of("factor", "m"), computed, DynamicColumns.Config.WIDTH);
		List<ColumnSetup> computedColumns = resolve(computed);
		assertEquals("The columns follow the list of column objects.", List.of("d.name", "d.start", "d.end"),
			computedColumns.stream().map(ColumnSetup::name).toList());
		assertEquals("Every column gets its own width.", List.of(260, 130, 130), widths(computed));

		DynamicColumns.Config typeDerived =
			dynamicColumns("", columns + "<width>$m == 'name' ? null : ($m == 'start' ? 0 : 130)</width>");
		assertEquals("Nothing and 0 keep the width the type of the values derives.", List.of(0, 0, 130),
			widths(typeDerived));

		DynamicColumns.Config unset = dynamicColumns("", columns);
		assertNull(unset.getWidth());
		assertEquals("Without a width, every column keeps the width its type derives.", List.of(0, 0, 0),
			widths(unset));

		DynamicColumns.Config negative = dynamicColumns("width='-1'", columns);
		try {
			resolve(negative);
			fail("A negative width must be rejected.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("-1"));
		}
	}

	private DynamicColumns.Config dynamicColumns(String attributes, String content) throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns>"
				+ "<dynamic-columns name='d' column-as='m' " + attributes + ">" + content + "</dynamic-columns>"
				+ "</columns></table>");
		return (DynamicColumns.Config) table.getColumns().getColumns().get(0);
	}

	private List<ColumnSetup> resolve(DynamicColumns.Config config) {
		return instantiate(config).resolve(new ColumnResolution(List.of(), _context));
	}

	private List<Integer> widths(DynamicColumns.Config config) {
		return resolve(config).stream().map(ColumnSetup::width).toList();
	}

	/**
	 * Tests that a column and a set of dynamic columns take a display of their values, whose scripts
	 * see the value by the name the column gives it and none of the inputs of the table, so that a
	 * display adds no input to the column.
	 */
	public void testDisplay() throws Exception {
		String display = "<display class='" + ResourceDisplayByExpression.class.getName() + "'"
			+ " label=\"'L:' + $v\" css-class=\"['c', $v]\" role=\"$v == 'a' ? 'warning' : null\"/>";
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='factor' rows='[]'><columns>"
				+ "<computed-column name='c' type='tl.core:String' value-as='v' value='$row'>" + display
				+ "</computed-column>"
				+ "<dynamic-columns name='d' value-as='v' type='tl.core:String' columns=\"['m']\" value='$row'>"
				+ display + "</dynamic-columns>"
				+ "<column attribute='x'><display class='" + ResourceDisplayByExpression.class.getName()
				+ "' label='$value'/></column>"
				+ "</columns></table>");
		List<PolymorphicConfiguration<? extends ColumnDeclaration>> declarations = table.getColumns().getColumns();

		ComputedColumn.Config computed = (ComputedColumn.Config) declarations.get(0);
		ConfigurationItem computedDisplay = computed.getDisplay();
		assertTrue(computedDisplay instanceof ResourceDisplayByExpression.Config);
		assertScope(List.of("v"), computedDisplay, ResourceDisplayByExpression.Config.LABEL);
		assertScope(List.of("v"), computedDisplay, ResourceDisplayByExpression.Config.CSS_CLASS);
		assertScope(List.of("v"), computedDisplay, ResourceDisplayByExpression.Config.ROLE);
		ColumnSetup setup = single(instantiate(computed).resolve(new ColumnResolution(List.of(), _context)));
		ResourceDisplayByExpression provider = (ResourceDisplayByExpression) setup.display();
		assertEquals("L:a", provider.getLabel("a"));
		assertEquals("c a", provider.getCssClass("a"));
		assertEquals(ValueColor.WARNING, provider.colorOf("a"));
		assertEquals("Where the role expression yields nothing, the value keeps the color of its model.",
			AnnotationValueColorProvider.INSTANCE.colorOf("b"), provider.colorOf("b"));

		DynamicColumns.Config dynamic = (DynamicColumns.Config) declarations.get(1);
		assertScope(List.of("v"), dynamic.getDisplay(), ResourceDisplayByExpression.Config.LABEL);
		ColumnSetup dynamicSetup = single(instantiate(dynamic).resolve(new ColumnResolution(List.of(), _context)));
		assertEquals("L:b", ((ResourceDisplayByExpression) dynamicSetup.display()).getLabel("b"));

		AttributeColumn.Config attribute = (AttributeColumn.Config) declarations.get(2);
		assertScope(List.of("value"), attribute.getDisplay(), ResourceDisplayByExpression.Config.LABEL);
		ResourceDisplayByExpression withoutRole = (ResourceDisplayByExpression) instantiate(attribute.getDisplay());
		assertEquals("Without a role expression, the value keeps the color of its model.",
			AnnotationValueColorProvider.INSTANCE.colorOf("b"), withoutRole.colorOf("b"));
		assertEquals("A display reads no input of the table.", List.of(), instantiate(attribute).inputs());

		ColumnSetup plain = single(instantiate(table.getColumns().getColumns().get(0))
			.resolve(new ColumnResolution(List.of(), _context)));
		assertNotNull(plain.display());
		TableElement.Config withoutDisplay = (TableElement.Config) parse(TABLE,
			"<table rows='[]'><columns><computed-column name='c' value='$row'/></columns></table>");
		assertNull("A column without a display shows its values as their type derives.",
			single(instantiate(withoutDisplay.getColumns().getColumns().get(0))
				.resolve(new ColumnResolution(List.of(), _context))).display());
	}

	/**
	 * Tests that a column reports the inputs its scripts see as the channels its cells follow: a
	 * column without inputs of its own those of the table as soon as it computes anything with a
	 * script, and nothing while it computes nothing.
	 */
	public void testScriptInputsAreCellInputs() throws Exception {
		TableElement.Config table = (TableElement.Config) parse(TABLE,
			"<table inputs='factor as f, other' rows='[]'><columns>"
				+ "<column attribute='plain'/>"
				+ "<column attribute='marked' cell-class='$value'/>"
				+ "<column attribute='explained' tooltip='$value'/>"
				+ "<column attribute='summed' aggregate='$members.size()'/>"
				+ "<column attribute='ordered' comparator='compare($left, $right)'/>"
				+ "<computed-column name='c' type='tl.core:String' cell-class='$f'>"
				+ "<inputs><input channel='factor' as='f'/></inputs>"
				+ "<value>$f</value>"
				+ "</computed-column>"
				+ "</columns></table>");
		List<InputRef> tableInputs = table.getInputs();
		List<List<InputRef>> inputs = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ColumnDeclaration> column : table.getColumns().getColumns()) {
			inputs.add(instantiate(column).inputs());
		}

		assertEquals("A column computing nothing with a script reads no input.", List.of(), inputs.get(0));
		assertEquals("cell-class", tableInputs, inputs.get(1));
		assertEquals("tooltip", tableInputs, inputs.get(2));
		assertEquals("aggregate", tableInputs, inputs.get(3));
		assertEquals("comparator", tableInputs, inputs.get(4));
		ComputedColumn.Config computed = (ComputedColumn.Config) table.getColumns().getColumns().get(5);
		assertEquals("A column with inputs of its own reports each of them once.", computed.getInputs(),
			inputs.get(5));
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
			RowFunction.rowClass(table);
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
			COMPOSITION_TABLE, TypedConfiguration.getConfigurationDescriptor(CompositionTableElement.Config.class),
			TREE_TABLE, TypedConfiguration.getConfigurationDescriptor(TreeTableElement.Config.class));
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
