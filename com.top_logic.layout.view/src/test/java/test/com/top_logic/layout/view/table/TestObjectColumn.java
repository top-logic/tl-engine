/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.List;
import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.Resources;

/**
 * Tests the columns of a table declaring none of its own over a row type that holds nothing to
 * show in a column, and the header labels of the attribute columns derived from the row type.
 *
 * <p>
 * A table over objects of a class without attributes shows the objects themselves in the
 * {@link ColumnDeclarations#objectColumn(com.top_logic.model.TLStructuredType) object column},
 * headed by the label of the class. An attribute column is headed by the label of its attribute,
 * and by its name where the attribute has no label.
 * </p>
 */
public class TestObjectColumn extends AbstractModelAccessTest {

	/** A class of the test model holding no attributes. */
	private static final String SPEC = "Spec";

	/** An attribute of a {@link #PROJECT} the test resources label. */
	private static final String RATING = "rating";

	/** The English label the test resources give the attribute {@link #RATING}. */
	private static final String RATING_LABEL = "Rating of the project";

	/** The label derived from the name of the attribute {@link #BUDGET}, which has no label. */
	private static final String BUDGET_LABEL = "Budget";

	/** The rows are shown to a user who may read every value. */
	@Override
	protected Person initialUser() {
		return _root;
	}

	/**
	 * A table declaring no columns over objects of a class without attributes shows the objects in
	 * a single column headed by the label of the class.
	 */
	public void testClassWithoutAttributesShowsTheObjects() {
		TLClass spec = type(SPEC);
		List<ColumnSetup> columns = resolve(table(SPEC), spec);

		assertEquals(List.of(ColumnDeclarations.OBJECT_COLUMN), names(columns));
		ColumnSetup objects = columns.get(0);
		assertEquals(TLModelNamingConvention.getTypeLabelKey(spec), objects.label());
		assertEquals(SPEC, label(objects.label()));
		assertFalse("The column is displayed from the start.", objects.hiddenByDefault());
		assertSame("A cell holds the row itself.", _project, objects.value().apply(_project));
	}

	/** A table declaring no columns over rows of an unknown type has nothing to show. */
	public void testNoRowTypeAndNoColumnsFails() {
		TableElement table = table(null);
		try {
			table.columns(table.types(new Object[0]), null);
			fail("A table without columns and without a row type cannot show anything.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

	/** A class with attributes gets its attribute columns, and no object column at all. */
	public void testClassWithAttributesHasNoObjectColumn() {
		List<ColumnSetup> columns = resolve(table(PROJECT), type(PROJECT));

		assertFalse(columns.isEmpty());
		assertFalse("Neither displayed nor offered.", names(columns).contains(ColumnDeclarations.OBJECT_COLUMN));
	}

	/**
	 * An attribute column is headed by the label of its attribute, and by the attribute's name
	 * where the attribute has no label.
	 */
	public void testAttributeColumnLabel() {
		List<ColumnSetup> columns = resolve(table(PROJECT), type(PROJECT));

		assertEquals(RATING_LABEL, label(column(columns, RATING).label()));
		assertEquals(BUDGET_LABEL, label(column(columns, BUDGET).label()));
	}

	/** The columns the given table builds for rows of the given type, in display order. */
	private static List<ColumnSetup> resolve(AbstractTableElement table, TLClass rowType) {
		List<ColumnDeclaration> declarations = table.columns(table.types(new Object[0]), rowType);
		return ColumnDeclarations.resolve(declarations, new ColumnResolution(rowType, null));
	}

	private static ColumnSetup column(List<ColumnSetup> columns, String name) {
		for (ColumnSetup column : columns) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new AssertionError("No column '" + name + "' in " + names(columns) + ".");
	}

	private static List<String> names(List<ColumnSetup> columns) {
		return columns.stream().map(ColumnSetup::name).toList();
	}

	private static String label(ResKey key) {
		return Resources.getInstance(Locale.ENGLISH).getString(key);
	}

	/**
	 * A {@code <table>} declaring no columns, over rows of the given type of the test model.
	 *
	 * @param typeName
	 *        The name of the row type, {@code null} for a table stating no row type.
	 */
	private static TableElement table(String typeName) {
		TableElement.Config config = TypedConfiguration.newConfigItem(TableElement.Config.class);
		if (typeName != null) {
			set(config, TableElement.Config.TYPES, List.of(TLModelPartRef.ref(MODULE + ":" + typeName)));
		}
		set(config, TableElement.Config.ROWS, TypedConfiguration.newConfigItem(Expr.Null.class));
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestObjectColumn.class);
		TableElement result = (TableElement) context.getInstance(config);
		assertNotNull(result);
		return result;
	}

	private static void set(ConfigurationItem config, String property, Object value) {
		config.update(config.descriptor().getProperty(property), value);
	}

	/**
	 * The test suite, additionally starting the services building the columns, the
	 * {@link SearchBuilder} compiling the value of the object column, and the resources labelling
	 * the columns.
	 */
	public static Test suite() {
		return suiteWith(TestObjectColumn.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
