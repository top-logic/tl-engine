/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
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
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.element.TreeTableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.ColumnView;
import com.top_logic.table.TableView;

/**
 * Tests the computed type properties of a table: {@link AbstractTableElement.Config#getTypesExpr()}
 * means what the same {@link AbstractTableElement.Config#getTypes() types} mean, and
 * {@link TableElement.Config#getCreateTypeExpr()} what the same
 * {@link TableElement.Config#getCreateType() create type} means.
 */
public class TestTableTypesExpr extends AbstractModelAccessTest {

	/** Tag of the table configurations under test. */
	private static final String TABLE = "table";

	/** Tag of the tree table configurations under test. */
	private static final String TREE_TABLE = "tree-table";

	/** Name of the input channel holding the type. */
	private static final String TYPE_CHANNEL = "type";

	/** Name of the abstract type with the concrete specializations {@link #SPEC} and {@link #PLAN}. */
	private static final String DELIVERABLE = "Deliverable";

	/** Name of a concrete {@link #DELIVERABLE}. */
	private static final String SPEC = "Spec";

	/** Name of a concrete {@link #DELIVERABLE}. */
	private static final String PLAN = "Plan";

	private ReactContext _reactContext;

	private DefaultViewContext _context;

	private DefaultViewChannel _type;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_context = new DefaultViewContext(_reactContext);
		_type = new DefaultViewChannel(TYPE_CHANNEL);
		_context.registerChannel(TYPE_CHANNEL, _type);
	}

	@Override
	protected void tearDown() throws Exception {
		_type = null;
		_context = null;
		_reactContext = null;
		super.tearDown();
	}

	/**
	 * Computed types derive the columns a table shows and offers exactly like the same types
	 * configured.
	 */
	public void testComputedTypesDeriveColumnsLikeFixedTypes() throws Exception {
		becomeUser(_root);
		TableElement fixed = table("<table types='" + qualified(TASK) + "' rows='[]'/>");
		TableElement computed = table("<table inputs='type' types-expr='$type' rows='[]'/>");

		_type.set(type(TASK));
		TableView<?> fixedView = readOnlyView(fixed);
		TableView<?> computedView = readOnlyView(computed);

		List<String> shown = shown(fixedView);
		assertFalse("The fixed type derives the shown columns.", shown.isEmpty());
		assertEquals(shown, shown(computedView));
		assertEquals(options(fixedView), options(computedView));

		String columns = "<columns><column attribute='" + NAME + "'/></columns>";
		TableElement fixedDeclared =
			table("<table types='" + qualified(TASK) + "' rows='[]'>" + columns + "</table>");
		TableElement computedDeclared =
			table("<table inputs='type' types-expr='$type' rows='[]'>" + columns + "</table>");
		TableView<?> fixedDeclaredView = readOnlyView(fixedDeclared);
		TableView<?> computedDeclaredView = readOnlyView(computedDeclared);

		assertEquals(List.of(NAME), shown(computedDeclaredView));
		List<String> options = options(fixedDeclaredView);
		assertTrue("The fixed type offers further columns: " + options, options.size() > 1);
		assertEquals(options, options(computedDeclaredView));
	}

	/**
	 * The identity of a table with computed types is built from their names, so that the
	 * personalization is kept per type - with a personalization key, too.
	 */
	public void testIdentityPerComputedType() throws Exception {
		TableElement fixed = table("<table types='" + qualified(TASK) + "' rows='[]'/>");
		TableElement computed = table("<table inputs='type' types-expr='$type' rows='[]'/>");

		String fixedId = fixed.tableId(fixed.types(new Object[0])).value();
		String taskId = computed.tableId(computed.types(new Object[] { type(TASK) })).value();
		String projectId = computed.tableId(computed.types(new Object[] { type(PROJECT) })).value();
		assertEquals("A computed type is named like the same fixed type.", fixedId, taskId);
		assertFalse("Another type, another identity.", taskId.equals(projectId));

		TableElement keyed =
			table("<table inputs='type' types-expr='$type' rows='[]' personalization-key='instances'/>");
		String keyedTask = keyed.tableId(keyed.types(new Object[] { type(TASK) })).value();
		String keyedProject = keyed.tableId(keyed.types(new Object[] { type(PROJECT) })).value();
		assertTrue(keyedTask, keyedTask.contains("instances"));
		assertFalse("A key is kept per computed type as well.", keyedTask.equals(keyedProject));
	}

	/**
	 * A types expression may compute several types, or none, which leaves the table without types
	 * as an unset {@link AbstractTableElement.Config#getTypes()} does.
	 */
	public void testComputedTypeCollectionAndNothing() throws Exception {
		TableElement computed = table("<table inputs='type' types-expr='$type' rows='[]'/>");

		AbstractTableElement.RowTypes several = computed.types(new Object[] { List.of(type(TASK), type(PROJECT)) });
		assertEquals(List.of(qualified(TASK), qualified(PROJECT)), several.qualifiedNames());
		assertSame(type(TASK), several.first());

		assertTrue(computed.types(new Object[] { null }).isEmpty());
	}

	/**
	 * A types expression computing something else than types is an error naming the property.
	 */
	public void testComputedTypesOfWrongKind() throws Exception {
		TableElement computed = table("<table inputs='type' types-expr='$type' rows='[]'/>");
		try {
			computed.types(new Object[] { "no type" });
			fail("A text is no type.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(AbstractTableElement.Config.TYPES_EXPR));
		}
	}

	/**
	 * A computed concrete create type is offered alone, and adding a row creates it.
	 */
	public void testComputedConcreteCreateType() throws Exception {
		becomeUser(_root);
		RowSetTableControl table = editedTable(type(TASK));

		assertEquals(List.of(type(TASK)), table.createTypes());
		assertTrue(table.getAddRowCommand().isVisible());
		assertTrue(table.getAddRowCommand().executeCommand(_reactContext).isSuccess());
		List<TLObject> rows = new ArrayList<>(table.session().currentRows());
		assertEquals(1, rows.size());
		assertSame(type(TASK), rows.get(0).tType());
	}

	/**
	 * A computed abstract create type stands for its concrete specializations.
	 */
	public void testComputedAbstractCreateType() throws Exception {
		becomeUser(_root);
		RowSetTableControl table = editedTable(type(DELIVERABLE));

		Set<TLClass> expected = Set.of(type(SPEC), type(PLAN));
		assertEquals(expected, new HashSet<>(table.createTypes()));
		assertTrue(table.getAddRowCommand().isVisible());
	}

	/**
	 * A create type expression computing nothing offers no row creation.
	 */
	public void testComputedCreateTypeNothing() throws Exception {
		becomeUser(_root);
		RowSetTableControl table = editedTable(null);

		assertTrue(table.createTypes().isEmpty());
		assertFalse(table.getAddRowCommand().isVisible());
	}

	/**
	 * A create type expression computing something else than a class is an error naming the
	 * property.
	 */
	public void testComputedCreateTypeOfWrongKind() throws Exception {
		TableElement table = table("<table inputs='type' create-type-expr='$type' rows='[]' row-edit='ALL'/>");
		try {
			table.createType(new Object[] { "no type" });
			fail("A text is no type.");
		} catch (IllegalStateException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains(TableElement.Config.CREATE_TYPE_EXPR));
		}
	}

	/**
	 * Stating a type both fixed and by an expression is a configuration error.
	 */
	public void testFixedAndComputedReported() throws Exception {
		assertContains(AbstractTableElement.Config.TYPES_EXPR, errors(TABLE,
			"<table inputs='type' types='" + qualified(TASK) + "' types-expr='$type' rows='[]'/>"));
		assertContains(AbstractTableElement.Config.TYPES_EXPR, errors(TREE_TABLE,
			"<tree-table inputs='type' types='" + qualified(TASK) + "' types-expr='$type' root='$type'"
				+ " children='[]'/>"));
		assertContains(TableElement.Config.CREATE_TYPE_EXPR, errors(TABLE,
			"<table inputs='type' create-type='" + qualified(TASK) + "' create-type-expr='$type' rows='[]'"
				+ " row-edit='ALL'/>"));
	}

	/**
	 * Builds a row-editing table whose create type is computed from the {@link #TYPE_CHANNEL}
	 * holding the given type, in a form over the project, and enters edit mode.
	 */
	private RowSetTableControl editedTable(TLClass createType) throws Exception {
		TableElement element =
			table("<table inputs='type' create-type-expr='$type' rows='[]' row-edit='ALL'>"
				+ "<columns><column attribute='" + NAME + "'/></columns></table>");
		_type.set(createType);
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		RowSetTableControl control = (RowSetTableControl) element.createControl(_context);
		assertTrue(form.enterEditMode());
		return control;
	}

	private TableView<?> readOnlyView(TableElement element) {
		return ((TableViewControl<?>) element.createControl(_context)).getView();
	}

	private static List<String> shown(TableView<?> view) {
		return view.columns().stream().map(ColumnView::name).toList();
	}

	private static List<String> options(TableView<?> view) {
		return view.columnOptions().stream().map(ColumnOption::name).toList();
	}

	private static TableElement table(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableTypesExpr.class);
		TableElement result = (TableElement) context.getInstance((PolymorphicConfiguration<?>) parse(TABLE, xml));
		context.checkErrors();
		return result;
	}

	private static List<String> errors(String tag, String xml) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance((PolymorphicConfiguration<?>) parse(tag, xml));
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

	private static ConfigurationItem parse(String tag, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableTypesExpr.class);
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(
			TABLE.equals(tag) ? TableElement.Config.class : TreeTableElement.Config.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(tag, descriptor));
		reader.setSource(CharacterContents.newContent(xml));
		ConfigurationItem result = reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its columns and their cells, and the labels of the offered types.
	 */
	public static Test suite() {
		return suiteWith(TestTableTypesExpr.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
