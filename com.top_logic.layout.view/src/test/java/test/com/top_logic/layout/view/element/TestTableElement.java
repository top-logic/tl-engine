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
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.layout.view.table.DropTargetMode;
import com.top_logic.layout.view.table.DynamicColumns;
import com.top_logic.layout.view.table.EmbeddedColumns;
import com.top_logic.layout.view.table.FilterStateConfig;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
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

		// Verify the channels publishing the filtering.
		assertNotNull("Active preset channel should be set", tableConfig.getActivePreset());
		assertEquals("Active preset channel name", "activeFilter",
			tableConfig.getActivePreset().getChannelName());
		assertNotNull("Search term channel should be set", tableConfig.getSearchTerm());
		assertEquals("Search term channel name", "searchTerm", tableConfig.getSearchTerm().getChannelName());
		assertNull("A table publishes its filtering only where it says so.",
			TypedConfiguration.newConfigItem(TableElement.Config.class).getActivePreset());
		assertNull("A table publishes its filtering only where it says so.",
			TypedConfiguration.newConfigItem(TableElement.Config.class).getSearchTerm());
	}

	/**
	 * Tests that the preset the table starts out filtered by is parsed, reaches the table, and that
	 * a table declaring none starts out unfiltered.
	 */
	public void testInitialPreset() throws Exception {
		TableElement.Config tableConfig = readTableConfig();
		assertEquals("The presets declare which of them the table starts with.",
			"all-active", tableConfig.getPresets().getInitial());

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		TableElement element = (TableElement) context.getInstance(tableConfig);
		context.checkErrors();

		assertEquals("The declared preset is what the table is filtered by.",
			"all-active", element.initialFilter());

		TableElement.Config unfiltered = TypedConfiguration.copy(tableConfig);
		PresetsConfig presets = unfiltered.getPresets();
		presets.update(presets.descriptor().getProperty(PresetsConfig.INITIAL), null);
		TableElement withoutInitial = (TableElement) new DefaultInstantiationContext(
			TestTableElement.class).getInstance(unfiltered);

		assertNull("A table that names no initial preset starts out unfiltered.",
			withoutInitial.initialFilter());
	}

	/**
	 * Tests that an initial preset naming none of the declared ones is reported: it would leave the
	 * table unfiltered without anything saying why.
	 */
	public void testUnknownInitialPresetReported() throws Exception {
		TableElement.Config tableConfig = TypedConfiguration.copy(readTableConfig());
		PresetsConfig presets = tableConfig.getPresets();
		presets.update(presets.descriptor().getProperty(PresetsConfig.INITIAL), "nonesuch");

		assertContains("names no declared preset", errors(tableConfig));
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
	 * Tests that an {@code <embedded-columns>} is parsed with the path leading to the embedded
	 * object - or the function computing it and the type it is of - and the columns embedded from
	 * it.
	 */
	public void testParseEmbeddedColumns() throws Exception {
		List<PolymorphicConfiguration<? extends ColumnDeclaration>> columns =
			readTableConfig().getColumns().getColumns();

		EmbeddedColumns.Config owner = (EmbeddedColumns.Config) columns.get(4);
		assertEquals("owner.contact", owner.getReference());
		assertNull("An embedding over a reference takes the type from that reference.",
			owner.getType());
		assertNull("An embedding over a reference is named after it.", owner.getName());
		assertEquals("Declarations of any kind are embedded.", 2, owner.getColumns().size());
		assertEquals("name", ((AttributeColumn.Config) owner.getColumns().get(0)).getAttribute());
		assertEquals("mail", ((ComputedColumn.Config) owner.getColumns().get(1)).getName());

		EmbeddedColumns.Config accounts = (EmbeddedColumns.Config) columns.get(5);
		assertEquals("accounts", accounts.getName());
		assertEquals("demo.test:Account", accounts.getType().qualifiedName());
		assertNotNull("The embedded object is computed.", accounts.getObject());
		assertTrue("The function yields a collection of objects.", accounts.getMultiple());
		assertFalse("An embedding reaches one object unless it says otherwise.",
			TypedConfiguration.newConfigItem(EmbeddedColumns.Config.class).getMultiple());
		assertEquals("Account",
			((ResKey.LiteralKey) accounts.getLabel()).getTranslationWithoutFallbacks(Locale.ENGLISH));
	}

	/**
	 * Tests that a {@code <dynamic-columns>} is parsed with everything it says about the columns it
	 * computes: the objects standing for them, their names, labels, values, edits, aggregate and
	 * the display all of them share.
	 */
	public void testParseDynamicColumns() throws Exception {
		DynamicColumns.Config milestones = dynamicColumns(readTableConfig());

		assertEquals("milestones", milestones.getName());
		assertEquals("tl.core:Double", milestones.getType().qualifiedName());
		assertFalse("A cell holds a single value unless the columns say otherwise.",
			milestones.getMultiple());
		assertNull("The columns share the named type, so none of them computes one.",
			milestones.getColumnType());
		assertEquals("The configured width of every computed column, in pixels.", 90,
			milestones.getWidth());
		assertTrue("The columns stay read-only while the rows are edited.", milestones.getReadonly());
		assertNotNull("The objects standing for the columns are computed.", milestones.getColumns());
		assertNotNull("The columns are named after their objects.", milestones.getColumnName());
		assertNotNull("The columns are labelled after their objects.", milestones.getColumnLabel());
		assertNotNull("The cell value is computed.", milestones.getValue());
		assertNotNull("An edited value is written back.", milestones.getUpdate());
		assertNotNull("The columns say which of their rows are edited.", milestones.getCanUpdate());
		assertNotNull("The columns aggregate over a group.", milestones.getAggregate());
		assertEquals("Should declare one input", 1, milestones.getInputs().size());
		assertEquals("testInput", milestones.getInputs().get(0).getChannelName());

		DynamicColumns.Config plain = TypedConfiguration.newConfigItem(DynamicColumns.Config.class);
		assertNull("A declaration that names no type computes none either.", plain.getColumnType());
		assertEquals("The columns keep the width their type derives.", 0, plain.getWidth());
		assertFalse("The columns are edited like any other unless they say otherwise.",
			plain.getReadonly());
	}

	/**
	 * Tests that computed columns contribute no column name before the table has been built - what
	 * columns there are is decided by data - and that the rest of the table is named as before.
	 */
	public void testDynamicColumnsContributeNoNames() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		List<ColumnDeclaration> declarations =
			ColumnDeclarations.instantiate(context, readTableConfig().getColumns());
		context.checkErrors();

		ColumnDeclaration milestones = declarations.get(declarations.size() - 1);
		assertEquals("Only the data at hand says which columns there are.",
			List.of(), milestones.declaredNames());
		assertEquals("Columns nothing is known about yet do not sort the table.",
			List.of(), milestones.defaultSort());
	}

	/**
	 * Tests that computed columns say what their values are in exactly one way, and that columns
	 * that are edited say it at all.
	 */
	public void testDynamicColumnsDeclareTheirValueType() throws Exception {
		TableElement.Config both = TypedConfiguration.copy(readTableConfig());
		DynamicColumns.Config computedType = dynamicColumns(both);
		computedType.update(computedType.descriptor().getProperty(DynamicColumns.Config.COLUMN_TYPE),
			TypedConfiguration.newConfigItem(Expr.Null.class));

		assertContains("not both", errors(both));

		TableElement.Config untyped = TypedConfiguration.copy(readTableConfig());
		DynamicColumns.Config edited = dynamicColumns(untyped);
		edited.update(edited.descriptor().getProperty(DynamicColumns.Config.TYPE), null);

		assertContains("must declare the type", errors(untyped));
	}

	/**
	 * The {@code <dynamic-columns>} of the given table.
	 */
	private static DynamicColumns.Config dynamicColumns(TableElement.Config tableConfig) {
		for (PolymorphicConfiguration<? extends ColumnDeclaration> column : tableConfig.getColumns().getColumns()) {
			if (column instanceof DynamicColumns.Config dynamic) {
				return dynamic;
			}
		}
		throw new AssertionError("The table declares computed columns.");
	}

	/**
	 * Tests that the columns of an embedding are named after the path leading to them, so that a
	 * column of the embedded object and one of the row itself stay apart.
	 */
	public void testEmbeddedColumnsAreNamedAfterTheirPath() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableElement.class);
		List<ColumnDeclaration> declarations =
			ColumnDeclarations.instantiate(context, readTableConfig().getColumns());
		context.checkErrors();

		assertEquals(List.of("name", "active", "owner", "total", "owner.contact.name",
			"owner.contact.mail", "accounts.number"),
			ColumnDeclarations.declaredNames(declarations));
	}

	/**
	 * Tests that an embedding saying which object it shows in more than one way - or in no way at
	 * all - is reported.
	 */
	public void testEmbeddedTargetIsDeclaredOnce() throws Exception {
		TableElement.Config both = TypedConfiguration.copy(readTableConfig());
		EmbeddedColumns.Config referenced = (EmbeddedColumns.Config) both.getColumns().getColumns().get(4);
		referenced.update(referenced.descriptor().getProperty(EmbeddedColumns.Config.TYPE),
			TLModelPartRef.ref("demo.test:Contact"));

		assertContains("not both", errors(both));

		TableElement.Config neither = TypedConfiguration.copy(readTableConfig());
		EmbeddedColumns.Config computed = (EmbeddedColumns.Config) neither.getColumns().getColumns().get(5);
		computed.update(computed.descriptor().getProperty(EmbeddedColumns.Config.OBJECT), null);
		computed.update(computed.descriptor().getProperty(EmbeddedColumns.Config.TYPE), null);

		assertContains("must say which object", errors(neither));
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

		assertEquals("demo.test:Row,|name,active,owner,total,owner.contact.name,owner.contact.mail,"
			+ "accounts.number,", element.tableId().value());
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
