/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.kbbased.storage.mappings.IntMapping;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.layout.view.table.ComputedColumn;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.table.Column;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.SortSpec;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.util.model.CompatibilityService;

/**
 * Test for the columns a {@link ColumnDeclaration} contributes: what an
 * {@link AttributeColumn} takes from the attribute it shows, what a {@link ComputedColumn} takes
 * from the type it declares, and what a table derives from them before it has any rows.
 */
public class TestColumnDeclarations extends TestCase {

	/** The module holding the test model. */
	private static final String MODULE = "test.columnDeclarations";

	/** The type the declared columns show attributes of. */
	private TLClass _rowType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_rowType = TLModelUtil.addClass(module, "Row");
		TLModelUtil.addProperty(_rowType, "count",
			TLModelUtil.addDatatype(module, module, "Integer", Kind.INT, IntMapping.INSTANCE));
		TLModelUtil.addProperty(_rowType, "name", TLModelUtil.addClass(module, "Value"));
	}

	/**
	 * A {@code <column>} shows the attribute it names, with the label and the affordances the
	 * attribute's type gives it.
	 */
	public void testAttributeColumnTakesTheAttribute() {
		ColumnSetup setup = single(attributeColumn("count", null));

		assertEquals("count", setup.name());
		assertEquals("The column is labelled as the attribute is.",
			TLModelNamingConvention.resourceKey(_rowType.getPart("count")), setup.label());
		assertEquals("The column shows the attribute's values.", _rowType.getPart("count"),
			setup.type().part());
		assertNotNull("A column over an attribute is edited by writing that attribute.", setup.editing());
		assertNull("A column aggregates nothing unless it says so.", setup.aggregate());

		Column<Object, ?> column = setup.buildColumn();
		assertEquals("A whole number is filtered by a numeric range.", ComparableColumnFilter.class,
			column.filter().get().getClass());
		assertEquals("A whole number is shown in the number width.",
			ColumnProviderService.getInstance().getConfig().getNumberWidth(), column.defaultWidth());
		assertTrue("A column that aggregates nothing leaves its group cell empty.",
			column.aggregate().isEmpty());
	}

	/**
	 * What a {@code <column>} declares about its column reaches the resolved column: its label, its
	 * width, and that it is not edited.
	 */
	public void testAttributeColumnOverridesWhatItDeclares() {
		AttributeColumn.Config config = attributeColumn("count", null);
		set(config, AttributeColumn.Config.LABEL, ResKey.text("Amount"));
		set(config, AttributeColumn.Config.WIDTH, Integer.valueOf(220));
		set(config, AttributeColumn.Config.READONLY, Boolean.TRUE);

		ColumnSetup setup = single(config);

		assertEquals(ResKey.text("Amount"), setup.label());
		assertEquals(220, setup.width());
		assertNull("A column declared read-only is not edited.", setup.editing());
		assertEquals("The column is displayed in the width it declares.", 220,
			setup.buildColumn().defaultWidth());
	}

	/**
	 * A {@code <column>} naming an attribute the row type does not hold - and one resolved without
	 * a row type at all - shows its values by their display label.
	 */
	public void testUnknownAttributeFallsBackToTheLabelColumn() {
		ColumnSetup setup = single(attributeColumn("missing", null));

		assertEquals(ResKey.text("missing"), setup.label());
		assertFalse("Nothing is known about the values.", setup.type().resolved());
		assertEquals("Such a column text-filters by the display label.", TextColumnFilter.class,
			setup.buildColumn().filter().get().getClass());
	}

	/**
	 * A {@code <computed-column>} is named and labelled after itself, and the type it declares
	 * gives it the display, sort and filter of that kind of value.
	 */
	public void testComputedColumnTakesItsDeclaredType() {
		ColumnSetup setup = single(computedColumn("total", MODULE + ":Integer"));

		assertEquals("total", setup.name());
		assertEquals("A column without a label is named after itself.", ResKey.text("total"),
			setup.label());
		assertNull("No attribute holds a computed value.", setup.type().part());
		assertEquals("The declared type says what the values are.", MODULE + ":Integer",
			TLModelUtil.qualifiedName(setup.type().type()));

		Column<Object, ?> column = setup.buildColumn();
		assertEquals("A computed whole number is filtered by a numeric range, too.",
			ComparableColumnFilter.class, column.filter().get().getClass());
		assertEquals("A computed whole number is shown in the number width, too.",
			ColumnProviderService.getInstance().getConfig().getNumberWidth(), column.defaultWidth());
	}

	/**
	 * A {@code <computed-column>} declaring no type shows its values by their display label.
	 */
	public void testComputedColumnWithoutATypeShowsLabels() {
		ColumnSetup setup = single(computedColumn("total", null));

		assertFalse("Nothing is known about the values.", setup.type().resolved());
		assertEquals("Such a column text-filters by the display label.", TextColumnFilter.class,
			setup.buildColumn().filter().get().getClass());
	}

	/**
	 * A {@code <computed-column>} is edited exactly when it declares how an edited value is written
	 * back.
	 */
	public void testComputedColumnIsEditedWhenItUpdates() {
		assertNull("A computed column that cannot write its value back is not edited.",
			single(computedColumn("total", MODULE + ":Integer")).editing());

		ComputedColumn.Config updating = computedColumn("total", MODULE + ":Integer");
		set(updating, ComputedColumn.Config.UPDATE, TypedConfiguration.newConfigItem(Expr.Null.class));

		assertNotNull("The column writes an edited value through its update function.",
			single(updating).editing());

		ComputedColumn.Config readonly = TypedConfiguration.copy(updating);
		set(readonly, ComputedColumn.Config.READONLY, Boolean.TRUE);

		assertNull("A column declared read-only is not edited, whatever it could write.",
			single(readonly).editing());
	}

	/**
	 * A {@code <computed-column>} that is edited must say what its values are - nothing else could
	 * say which control enters them.
	 */
	public void testEditedComputedColumnWithoutATypeIsReported() {
		ComputedColumn.Config config = computedColumn("total", null);
		set(config, ComputedColumn.Config.UPDATE, TypedConfiguration.newConfigItem(Expr.Null.class));

		ColumnsConfig columns = TypedConfiguration.newConfigItem(ColumnsConfig.class);
		columns.getColumns().add(config);

		BufferingProtocol log = new BufferingProtocol();
		ColumnDeclarations.instantiate(new DefaultInstantiationContext(log), columns);

		assertFalse("The column is reported.", log.getErrors().isEmpty());
		assertTrue("The column is reported by name: " + log.getErrors(),
			log.getErrors().get(0).contains("total"));
	}

	/**
	 * A column declaring a sort direction sorts the table it is in, and the declaration order
	 * decides which column sorts first.
	 */
	public void testDefaultSortFollowsTheDeclarationOrder() {
		AttributeColumn.Config count = attributeColumn("count", "DESC");
		ComputedColumn.Config total = computedColumn("total", null);
		set(total, ComputedColumn.Config.SORT, SortDirection.ASC);

		SortSpec sort = ColumnDeclarations.defaultSort(instantiate(count, total));

		assertEquals(List.of(new SortColumn("count", false), new SortColumn("total", true)),
			sort.columns());
		assertEquals("A table whose columns declare no direction starts unsorted.", SortSpec.NONE,
			ColumnDeclarations.defaultSort(instantiate(attributeColumn("count", null))));
	}

	/**
	 * A table refers to its columns by name, so two declarations naming the same column are
	 * reported and the second is not added.
	 */
	public void testDuplicateColumnNameIsReported() {
		ColumnsConfig columns = TypedConfiguration.newConfigItem(ColumnsConfig.class);
		columns.getColumns().add(attributeColumn("count", null));
		columns.getColumns().add(computedColumn("count", null));

		BufferingProtocol log = new BufferingProtocol();
		List<ColumnDeclaration> declarations =
			ColumnDeclarations.instantiate(new DefaultInstantiationContext(log), columns);

		assertEquals("Only the first declaration of the name contributes its column.", 1,
			declarations.size());
		assertEquals(List.of("count"), ColumnDeclarations.declaredNames(declarations));
		assertFalse("The duplicate is reported.", log.getErrors().isEmpty());
	}

	/**
	 * A column that is only offered says so itself, so a table starts out hiding exactly the
	 * columns nobody chose to show.
	 */
	public void testOfferedColumnsStartOutHidden() {
		List<ColumnDeclaration> declarations = new ArrayList<>(instantiate(attributeColumn("count", null)));
		declarations.add(AttributeColumn.offered(_rowType.getPart("name")));

		List<ColumnSetup> columns = ColumnDeclarations.resolve(declarations, scope());

		assertFalse("A declared column is shown from the start.", columns.get(0).hiddenByDefault());
		assertTrue("An offered column is shown once the user selects it.",
			columns.get(1).hiddenByDefault());
		assertEquals("Only the offered column is hidden.", List.of("name"),
			List.copyOf(ColumnDeclarations.hiddenByDefault(columns)));
	}

	/** The single column the given declaration contributes. */
	private ColumnSetup single(PolymorphicConfiguration<? extends ColumnDeclaration> config) {
		List<ColumnSetup> setups = ColumnDeclarations.resolve(instantiate(config), scope());
		assertEquals("A declaration naming one column contributes one.", 1, setups.size());
		return setups.get(0);
	}

	/** What the declarations under test are resolved against. */
	private ColumnResolution scope() {
		return new ColumnResolution(_rowType, null);
	}

	/** The instantiated declarations, failing the test when one of them is reported. */
	@SafeVarargs
	private static List<ColumnDeclaration> instantiate(
			PolymorphicConfiguration<? extends ColumnDeclaration>... configs) {
		ColumnsConfig columns = TypedConfiguration.newConfigItem(ColumnsConfig.class);
		for (PolymorphicConfiguration<? extends ColumnDeclaration> config : configs) {
			columns.getColumns().add(config);
		}
		BufferingProtocol log = new BufferingProtocol();
		List<ColumnDeclaration> result =
			ColumnDeclarations.instantiate(new DefaultInstantiationContext(log), columns);
		assertEquals("The declarations are accepted.", List.of(), log.getErrors());
		return result;
	}

	/** A {@code <column>} over the given attribute, sorted in the given direction. */
	private static AttributeColumn.Config attributeColumn(String attribute, String sort) {
		AttributeColumn.Config config = TypedConfiguration.newConfigItem(AttributeColumn.Config.class);
		set(config, AttributeColumn.Config.ATTRIBUTE, attribute);
		if (sort != null) {
			set(config, AttributeColumn.Config.SORT, SortDirection.valueOf(sort));
		}
		return config;
	}

	/** A {@code <computed-column>} over values of the given type, or of an undeclared one. */
	private static ComputedColumn.Config computedColumn(String name, String type) {
		ComputedColumn.Config config = TypedConfiguration.newConfigItem(ComputedColumn.Config.class);
		set(config, ComputedColumn.Config.NAME, name);
		set(config, ComputedColumn.Config.VALUE, TypedConfiguration.newConfigItem(Expr.Null.class));
		if (type != null) {
			set(config, ComputedColumn.Config.TYPE, TLModelPartRef.ref(type));
		}
		return config;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService}, which every declared column is built
	 * through, the {@link AttributeSettings} its display of an attribute value consults, and the
	 * {@link CompatibilityService} an attribute is asked about its storage through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnDeclarations.class,
				ColumnProviderService.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE));
	}

}
