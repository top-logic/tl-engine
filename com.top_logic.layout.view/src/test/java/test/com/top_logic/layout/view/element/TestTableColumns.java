/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.config.annotation.MainProperties;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Test for the columns a {@code <table>} shows when it declares none of its own: the main
 * properties the model names for its row type, and everything else that type holds as an offer.
 */
public class TestTableColumns extends TestCase {

	/** The module holding the test model. */
	private static final String MODULE = "test.tableColumns";

	/** The qualified name of the row type. */
	private static final String ROW_TYPE = MODULE + ":Row";

	/** The type whose instances the tables under test show. */
	private TLClass _rowType;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, MODULE);
		_rowType = TLModelUtil.addClass(module, "Row");
		TLClass valueType = TLModelUtil.addClass(module, "Value");
		TLModelUtil.addProperty(_rowType, "name", valueType);
		TLModelUtil.addProperty(_rowType, "description", valueType);
		DisplayAnnotations.setHidden(TLModelUtil.addProperty(_rowType, "technicalId", valueType), true);
	}

	/**
	 * A table declaring no columns shows the main properties of its row type, and offers the rest
	 * of what that type holds.
	 */
	public void testMainPropertiesAreDisplayed() {
		MainProperties mainProperties = TypedConfiguration.newConfigItem(MainProperties.class);
		set(mainProperties, MainProperties.PROPERTIES, List.of("name"));
		_rowType.setAnnotation(mainProperties);

		List<ColumnSetup> columns = resolve(tableWithoutColumns());

		assertEquals("The table shows the main properties and offers the rest.",
			List.of("name", "description"), names(columns));
		assertEquals("Everything the type holds beyond its main properties starts out hidden.",
			List.of("description"), List.copyOf(ColumnDeclarations.hiddenByDefault(columns)));
	}

	/**
	 * A table whose row type names no main properties shows all of its non-hidden attributes.
	 */
	public void testWithoutMainPropertiesEverythingIsDisplayed() {
		List<ColumnSetup> columns = resolve(tableWithoutColumns());

		assertEquals("All non-hidden attributes are shown.", List.of("name", "description"),
			names(columns));
		assertEquals("No column starts out hidden.", List.of(),
			List.copyOf(ColumnDeclarations.hiddenByDefault(columns)));
	}

	/**
	 * A table that declares its columns shows exactly those, and offers the rest of what its row
	 * type holds.
	 */
	public void testDeclaredColumnsWin() {
		MainProperties mainProperties = TypedConfiguration.newConfigItem(MainProperties.class);
		set(mainProperties, MainProperties.PROPERTIES, List.of("name"));
		_rowType.setAnnotation(mainProperties);

		List<ColumnSetup> columns = resolve(tableWithColumns("description"));

		assertEquals("The table shows what it declares and offers the rest of the type.",
			List.of("description", "name"), names(columns));
		assertEquals("Only what the table does not declare starts out hidden.", List.of("name"),
			List.copyOf(ColumnDeclarations.hiddenByDefault(columns)));
	}

	/** The columns of the given table over the row type, in display order. */
	private List<ColumnSetup> resolve(TableElement table) {
		List<ColumnDeclaration> declarations = table.columns(_rowType);
		return ColumnDeclarations.resolve(declarations, new ColumnResolution(_rowType, null));
	}

	/** The names of the given columns, in display order. */
	private static List<String> names(List<ColumnSetup> columns) {
		return columns.stream().map(ColumnSetup::name).toList();
	}

	/** A {@code <table>} over the row type that declares no columns of its own. */
	private static TableElement tableWithoutColumns() {
		return table(TypedConfiguration.newConfigItem(TableElement.Config.class));
	}

	/** A {@code <table>} over the row type showing the given attributes. */
	private static TableElement tableWithColumns(String... attributes) {
		TableElement.Config config = TypedConfiguration.newConfigItem(TableElement.Config.class);
		com.top_logic.layout.view.table.ColumnsConfig columns =
			TypedConfiguration.newConfigItem(com.top_logic.layout.view.table.ColumnsConfig.class);
		for (String attribute : attributes) {
			com.top_logic.layout.view.table.AttributeColumn.Config column = TypedConfiguration
				.newConfigItem(com.top_logic.layout.view.table.AttributeColumn.Config.class);
			set(column, com.top_logic.layout.view.table.AttributeColumn.Config.ATTRIBUTE, attribute);
			columns.getColumns().add(column);
		}
		set(config, TableElement.Config.COLUMNS, columns);
		return table(config);
	}

	/** The instantiated table over the row type, with the given columns. */
	private static TableElement table(TableElement.Config config) {
		set(config, TableElement.Config.TYPES, List.of(TLModelPartRef.ref(ROW_TYPE)));
		set(config, TableElement.Config.ROWS, TypedConfiguration.newConfigItem(Expr.Null.class));
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableColumns.class);
		TableElement element = (TableElement) context.getInstance(config);
		return element;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/**
	 * Test suite requiring the {@link TypeIndex}, the {@link AttributeSettings} the test model is
	 * built through, and the {@link CompatibilityService} the model consults about its types.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestTableColumns.class,
				TypeIndex.Module.INSTANCE,
				AttributeSettings.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE));
	}

}
