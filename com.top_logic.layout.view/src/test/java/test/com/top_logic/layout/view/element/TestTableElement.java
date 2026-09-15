/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TableElement.CriterionConfig;
import com.top_logic.layout.view.element.TableElement.DropConfig;
import com.top_logic.layout.view.element.TableElement.PresetConfig;
import com.top_logic.layout.view.element.TableElement.PresetsConfig;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.FilterStateConfig;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.SelectionMode;
import com.top_logic.table.SortDirection;

/**
 * Tests parsing and instantiation of {@link TableElement}.
 */
public class TestTableElement extends TestCase {

	/**
	 * Tests that a view XML with a {@code <table>} element can be parsed into configuration.
	 */
	public void testParseTableConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		assertNotNull("Config should be parsed", config);

		// The content should be a TableElement config.
		assertTrue("Content should be TableElement config",
			config.getContent() instanceof TableElement.Config);

		TableElement.Config tableConfig = (TableElement.Config) config.getContent();

		// Verify inputs.
		assertEquals("Should have one input", 1, tableConfig.getInputs().size());
		assertEquals("Input channel name", "testInput", tableConfig.getInputs().get(0).getChannelName());

		// Verify rows expression is present (non-null).
		assertNotNull("Rows expression should be set", tableConfig.getRows());

		// Verify selection channel.
		assertNotNull("Selection should be set", tableConfig.getSelection());
		assertEquals("Selection channel name", "selectedRow", tableConfig.getSelection().getChannelName());

		// Verify the table identity and the filter bar switch.
		assertEquals("Configured personalization key", "test-table", tableConfig.getPersonalizationKey());
		assertTrue("Filter bar should be switched on", tableConfig.getFilterBar());
		assertEquals("Configured initial grouping", "owner", tableConfig.getGroupBy());
	}

	/**
	 * Tests that a table offers its activation command on every row unless it opts out.
	 */
	public void testActivationButton() throws Exception {
		assertTrue("A table shows the button running its activation command.",
			TypedConfiguration.newConfigItem(TableElement.Config.class).getActivationButton());

		assertFalse("The table opts out of the button.", readTableConfig().getActivationButton());
	}

	/**
	 * Tests that a table selects one row at a time unless it configures the selection of any number
	 * of them, and that an editable table states its mode through the same property - both table
	 * variants are built from the one configured mode.
	 */
	public void testSelectionMode() throws Exception {
		assertEquals("A table selects one row at a time unless it says otherwise.",
			SelectionMode.SINGLE,
			TypedConfiguration.newConfigItem(TableElement.Config.class).getSelectionMode());

		assertEquals("The table configures the selection of any number of rows.",
			SelectionMode.MULTI, readTableConfig().getSelectionMode());

		TableElement.Config editable = TypedConfiguration.copy(readTableConfig());
		editable.update(editable.descriptor().getProperty(TableElement.Config.ROW_EDIT),
			RowEditPolicy.ALL);

		assertEquals("An editable table states its selection mode through the same property.",
			SelectionMode.MULTI, editable.getSelectionMode());
	}

	/**
	 * Tests that the configured {@code group-by} column becomes the grouping the table starts with,
	 * and that a table without one starts ungrouped.
	 */
	public void testInitialGrouping() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		TableElement element = (TableElement) context.getInstance(readTableConfig());
		context.checkErrors();

		assertEquals("The rows are grouped by the configured column, and by it alone.",
			List.of("owner"), element.initialGrouping().columns());

		TableElement.Config ungrouped = TypedConfiguration.copy(readTableConfig());
		ungrouped.update(ungrouped.descriptor().getProperty(TableElement.Config.GROUP_BY), null);
		TableElement withoutGrouping = (TableElement) new DefaultInstantiationContext(
			TestTableElement.class).getInstance(ungrouped);

		assertEquals("A table that configures no grouping starts ungrouped.",
			GroupSpec.NONE, withoutGrouping.initialGrouping());
	}

	/**
	 * Tests that the declared {@code <presets>} of a {@code <table>} are parsed with their labels
	 * and criteria.
	 */
	public void testParsePresets() throws Exception {
		TableElement.Config tableConfig = readTableConfig();

		PresetsConfig presetsConfig = tableConfig.getPresets();
		assertNotNull("Presets should be parsed", presetsConfig);
		List<PresetConfig> presets = presetsConfig.getPresets();
		assertEquals("Should have three presets", 3, presets.size());

		PresetConfig mine = presets.get(0);
		assertEquals("mine", mine.getName());
		assertEquals("Preset label", "My rows",
			((ResKey.LiteralKey) mine.getLabel()).getTranslationWithoutFallbacks(Locale.ENGLISH));

		List<CriterionConfig> criteria = mine.getCriteria();
		assertEquals("Should have two criteria", 2, criteria.size());
		assertEquals("owner", criteria.get(0).getColumn());
		assertNotNull("Criterion expression should be set", criteria.get(0).getExpr());
		assertEquals("active", criteria.get(1).getColumn());
		assertNotNull("Criterion expression should be set", criteria.get(1).getExpr());

		PresetConfig allActive = presets.get(1);
		assertEquals("all-active", allActive.getName());
		assertNull("A preset without a label declares none", allActive.getLabel());
		assertEquals("Should have one criterion", 1, allActive.getCriteria().size());
	}

	/**
	 * Tests that a criterion written in the form of the column's filter is parsed with its matching
	 * options, and that a criterion inverting its column is marked as such.
	 */
	public void testParseTypedCriteria() throws Exception {
		PresetConfig typed = readTableConfig().getPresets().getPresets().get(2);
		assertEquals("typed", typed.getName());

		CriterionConfig name = typed.getCriteria().get(0);
		assertEquals("name", name.getColumn());
		assertNull("A criterion declaring a form declares no value.", name.getExpr());
		assertFalse("A criterion is not inverted unless it says so.", name.getInverted());
		assertEquals("Should declare one criterion form", 1, name.getStates().size());
		FilterStateConfig.TextConfig text = (FilterStateConfig.TextConfig) name.getStates().get(0);
		assertNotNull("Pattern expression should be set", text.getPattern());
		assertTrue(text.getCaseSensitive());
		assertTrue(text.getWholeField());
		assertFalse(text.getRegexp());

		CriterionConfig active = typed.getCriteria().get(1);
		assertEquals("active", active.getColumn());
		assertTrue("The criterion inverts its column.", active.getInverted());
		FilterStateConfig.BooleanConfig accepted =
			(FilterStateConfig.BooleanConfig) active.getStates().get(0);
		assertNotNull("Accept expression should be set", accepted.getAccept());
	}

	/**
	 * Tests that the {@code <drag>} of a {@code <table>} is parsed, and that a table declaring no row
	 * type at all is reported: nothing would then say what its rows are.
	 */
	public void testParseDrag() throws Exception {
		TableElement.Config tableConfig = readTableConfig();

		assertNotNull("The table declares its rows draggable.", tableConfig.getDrag());
		assertNull("The drag takes the table's row type, so it declares none of its own.",
			tableConfig.getDrag().getType());

		TableElement.Config withoutType = TypedConfiguration.copy(tableConfig);
		withoutType.update(withoutType.descriptor().getProperty(TableElement.Config.TYPES), List.of());

		assertContains("must say what they are", errors(withoutType));
	}

	/**
	 * Tests that the {@code <drop>}s of a {@code <table>} are parsed with the types they accept, what
	 * they target, and the action chain applying them.
	 */
	public void testParseDrops() throws Exception {
		List<DropConfig> drops = readTableConfig().getDrops();
		assertEquals("Should have two drops", 2, drops.size());

		DropConfig onTable = drops.get(0);
		assertEquals(List.of("demo.test:Row"),
			onTable.getAccept().stream().map(ref -> ref.qualifiedName()).toList());
		assertEquals("A drop targets the table unless it says otherwise.",
			DropTargetMode.TABLE, onTable.getTarget());
		assertNull("A table drop has no target row to publish.", onTable.getTargetChannel());
		assertEquals("Should declare one action", 1, onTable.getActions().size());

		DropConfig onRow = drops.get(1);
		assertEquals(List.of("demo.test:Row", "demo.test:Other"),
			onRow.getAccept().stream().map(ref -> ref.qualifiedName()).toList());
		assertEquals(DropTargetMode.ROW, onRow.getTarget());
		assertNotNull("The row drop publishes the row dropped on.", onRow.getTargetChannel());
		assertEquals("dropTarget", onRow.getTargetChannel().getChannelName());
		assertEquals("Should declare one action", 1, onRow.getActions().size());
	}

	/**
	 * Tests that a criterion declaring both a value and the form of the column's filter is reported,
	 * and its preset is not offered.
	 */
	public void testCriterionWithValueAndFormReported() throws Exception {
		TableElement.Config tableConfig = TypedConfiguration.copy(readTableConfig());
		CriterionConfig criterion = tableConfig.getPresets().getPresets().get(2).getCriteria().get(0);
		criterion.update(criterion.descriptor().getProperty(CriterionConfig.EXPR),
			TypedConfiguration.newConfigItem(Expr.True.class));

		assertContains("declares both", errors(tableConfig));
	}

	/**
	 * Tests that a criterion declaring neither a value nor the form of the column's filter is
	 * reported, and its preset is not offered.
	 */
	public void testCriterionWithoutValueReported() throws Exception {
		TableElement.Config tableConfig = TypedConfiguration.copy(readTableConfig());
		CriterionConfig criterion = tableConfig.getPresets().getPresets().get(0).getCriteria().get(0);
		criterion.update(criterion.descriptor().getProperty(CriterionConfig.EXPR), null);

		assertContains("declares neither", errors(tableConfig));
	}

	/**
	 * Tests that the {@code <update>} and {@code <can-update>} of a {@code <computed-column>} are
	 * parsed, and that a column that is edited without saying what its values are is reported.
	 */
	public void testParseEditableComputedColumn() throws Exception {
		TableElement.Config tableConfig = readTableConfig();
		ComputedColumn.Config total = computedColumn(tableConfig);

		assertEquals("total", total.getName());
		assertNotNull("The column writes an edited value back.", total.getUpdate());
		assertNotNull("The column says which of its rows are edited.", total.getCanUpdate());
		assertNull("A column that is not edited declares neither.",
			TypedConfiguration.newConfigItem(ComputedColumn.Config.class).getUpdate());

		TableElement.Config untyped = TypedConfiguration.copy(tableConfig);
		ComputedColumn.Config edited = computedColumn(untyped);
		edited.update(edited.descriptor().getProperty(ComputedColumn.Config.TYPE), null);

		assertContains("must declare the type", errors(untyped));
	}

	/**
	 * The {@code <computed-column>} of the given table.
	 */
	private static ComputedColumn.Config computedColumn(TableElement.Config tableConfig) {
		for (PolymorphicConfiguration<? extends ColumnDeclaration> column : tableConfig.getColumns().getColumns()) {
			if (column instanceof ComputedColumn.Config computed) {
				return computed;
			}
		}
		throw new AssertionError("The table declares a computed column.");
	}

	/**
	 * The problems reported while instantiating the given configuration.
	 */
	private static List<String> errors(TableElement.Config tableConfig) {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(tableConfig);
		return log.getErrors();
	}

	private static void assertContains(String expected, List<String> errors) {
		for (String error : errors) {
			if (error.contains(expected)) {
				return;
			}
		}
		fail("Expected '" + expected + "' to be named in one of: " + errors);
	}

	/**
	 * Tests that the configured {@link UIElement.Config#getPersonalizationKey() personalization
	 * key} is the table's identity, so that a personalization survives adding or removing a column.
	 */
	public void testConfiguredTableId() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		TableElement element = (TableElement) context.getInstance(readTableConfig());
		context.checkErrors();

		assertEquals("key:test-table", element.tableId().value());
	}

	/**
	 * Tests that a table without a configured
	 * {@link UIElement.Config#getPersonalizationKey() personalization key} falls back to its
	 * structural signature: its row types and its column attributes.
	 */
	public void testStructuralTableId() throws Exception {
		TableElement.Config tableConfig = TypedConfiguration.copy(readTableConfig());
		tableConfig.update(
			tableConfig.descriptor().getProperty(UIElement.Config.PERSONALIZATION_KEY), null);

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		TableElement element = (TableElement) context.getInstance(tableConfig);
		context.checkErrors();

		assertEquals("demo.test:Row,|name,active,owner,total,", element.tableId().value());
	}

	/**
	 * The {@code <table>} configuration of the test fixture.
	 */
	private TableElement.Config readTableConfig() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		return (TableElement.Config) config.getContent();
	}

	/**
	 * Tests that the parsed configuration can be instantiated into a UIElement tree.
	 */
	public void testInstantiateTableElement() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestTableElement.class, "test-table.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Tests that a {@code <column>} carries what it declares about its column: the display width,
	 * the header label overriding the attribute's own, the sort direction and the read-only flag.
	 */
	public void testParseAttributeColumns() throws Exception {
		List<PolymorphicConfiguration<? extends ColumnDeclaration>> columns =
			readTableConfig().getColumns().getColumns();

		AttributeColumn.Config name = (AttributeColumn.Config) columns.get(0);
		assertEquals("name", name.getAttribute());
		assertEquals("The configured width in pixels.", 220, name.getWidth());
		assertEquals("The column overrides the attribute's label.", "Row name",
			((ResKey.LiteralKey) name.getLabel()).getTranslationWithoutFallbacks(Locale.ENGLISH));
		assertEquals(SortDirection.ASC, name.getSort());
		assertFalse("A column is editable unless it says otherwise.", name.getReadonly());

		AttributeColumn.Config active = (AttributeColumn.Config) columns.get(1);
		assertEquals("active", active.getAttribute());
		assertEquals("A column without a width keeps the one its type derives.", 0, active.getWidth());
		assertNull("A column without a label keeps the attribute's.", active.getLabel());
		assertNull("A column that declares no direction starts unsorted.", active.getSort());

		AttributeColumn.Config owner = (AttributeColumn.Config) columns.get(2);
		assertEquals("owner", owner.getAttribute());
		assertTrue("The column stays read-only while the rows are edited.", owner.getReadonly());
	}

	/**
	 * Tests that a {@code <computed-column>} is parsed with the type of its values, the inputs its
	 * value function receives and what it aggregates over a group.
	 */
	public void testParseComputedColumn() throws Exception {
		List<PolymorphicConfiguration<? extends ColumnDeclaration>> columns =
			readTableConfig().getColumns().getColumns();

		ComputedColumn.Config total = (ComputedColumn.Config) columns.get(3);
		assertEquals("total", total.getName());
		assertEquals("tl.core:Double", total.getType().qualifiedName());
		assertFalse("A cell holds a single value unless the column says otherwise.",
			total.getMultiple());
		assertNotNull("The value function is declared.", total.getValue());
		assertEquals("Should declare one input", 1, total.getInputs().size());
		assertEquals("testInput", total.getInputs().get(0).getChannelName());
		assertNotNull("The column aggregates over a group.", total.getAggregate());
		assertNull("A column without a label is named after itself.", total.getLabel());
	}

	/**
	 * Tests that two declarations naming the same column are reported: a table refers to its
	 * columns by name, so two of them sharing one could not be told apart.
	 */
	public void testDuplicateColumnNameReported() throws Exception {
		TableElement.Config tableConfig = TypedConfiguration.copy(readTableConfig());
		AttributeColumn.Config duplicate = TypedConfiguration.newConfigItem(AttributeColumn.Config.class);
		duplicate.update(duplicate.descriptor().getProperty(AttributeColumn.Config.ATTRIBUTE), "active");
		tableConfig.getColumns().getColumns().add(duplicate);

		assertContains("more than once", errors(tableConfig));
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTableElement.class, TypeIndex.Module.INSTANCE);
	}
}
