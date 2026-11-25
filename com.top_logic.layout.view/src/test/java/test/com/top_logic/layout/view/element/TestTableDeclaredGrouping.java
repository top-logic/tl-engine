/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.model.CommittedModelChanges;
import test.com.top_logic.layout.view.model.CommittedModelChanges.ScopedReactContext;
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
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnView;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.SortColumn;
import com.top_logic.table.SortDirection;
import com.top_logic.table.TableView;
import com.top_logic.table.impl.DefaultColumn;

/**
 * Tests that the grouping and the sort order a table declares take effect only as far as the
 * current user may read the columns they name, see
 * {@link ColumnDeclarations#grouping(com.top_logic.basic.Log, String, GroupSpec, java.util.Collection, List)}
 * and {@link com.top_logic.table.impl.DefaultTableView}.
 *
 * <p>
 * Reading {@value #BUDGET} is granted to {@value #ROLE_RESPONSIBLE} only: the {@link #_responsible}
 * user reads it on the {@link #_project}, the {@link #_roleless} user - made viewer of the project -
 * reads the project, but not its budget.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTableDeclaredGrouping extends AbstractModelAccessTest {

	/** Tag of the table configurations under test. */
	private static final String TABLE = "table";

	/** Name of a column no table of this test declares. */
	private static final String UNDECLARED = "undeclared";

	private static final String BUDGET_VALUE = "budget-of-project";

	/**
	 * A table of all projects showing their name and budget, grouped by the given column and sorted
	 * by the budget.
	 */
	private static final String PROJECTS = "<table types='" + qualified(PROJECT) + "'"
		+ " rows='all(`" + qualified(PROJECT) + "`)' group-by='%s'%s>"
		+ "<columns><column attribute='" + NAME + "'/><column attribute='" + BUDGET + "' sort='ASC'/></columns>"
		+ "</table>";

	private GlobalModelEventForwarder _scope;

	private DefaultViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scope = CommittedModelChanges.modelScope();
		_context = new DefaultViewContext(new ScopedReactContext(_scope));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(VIEWER, _roleless);
			_project.tUpdateByName(BUDGET, BUDGET_VALUE);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_scope = null;
		super.tearDown();
	}

	/**
	 * A user who may not read the grouped and sorted column gets the table ungrouped and not sorted
	 * by it.
	 */
	public void testWithoutReadRightUngrouped() throws Exception {
		becomeUser(_roleless);
		BufferingProtocol log = new BufferingProtocol();
		TableViewControl<?> control = readOnly(log, projects(BUDGET, ""));
		control.attach();

		assertUngrouped(control.getView());
		assertEquals(List.of(), log.getErrors());

		control.cleanupTree();
	}

	/**
	 * A user who may read the grouped and sorted column gets the table grouped and sorted as
	 * declared.
	 */
	public void testWithReadRightGrouped() throws Exception {
		becomeUser(_responsible);
		BufferingProtocol log = new BufferingProtocol();
		TableViewControl<?> control = readOnly(log, projects(BUDGET, ""));
		control.attach();

		assertGroupedAndSorted(control.getView());
		assertEquals(List.of(), log.getErrors());

		control.cleanupTree();
	}

	/**
	 * The table edited in a form follows the read right the same way.
	 */
	public void testEditableTableFollowsReadRight() throws Exception {
		becomeUser(_roleless);
		BufferingProtocol log = new BufferingProtocol();
		RowSetTableControl control = editable(log, projects(BUDGET, " row-edit='ALL'"));
		control.attach();
		assertUngrouped(control.getTableControl().getView());
		control.cleanupTree();

		becomeUser(_responsible);
		control = editable(log, projects(BUDGET, " row-edit='ALL'"));
		control.attach();
		assertGroupedAndSorted(control.getTableControl().getView());
		control.cleanupTree();

		assertEquals(List.of(), log.getErrors());
	}

	/**
	 * A grouping by a column the table does not declare is reported, and the table is displayed
	 * ungrouped nevertheless.
	 */
	public void testUndeclaredGroupingReported() throws Exception {
		becomeUser(_responsible);
		BufferingProtocol log = new BufferingProtocol();
		TableViewControl<?> control = readOnly(log, projects(UNDECLARED, ""));
		control.attach();

		assertEquals(GroupSpec.NONE, control.getView().state().getGrouping());
		assertEquals(0, groupHeaders(control.getView()));
		assertEquals(1, control.getView().rowCount());
		assertContains(UNDECLARED, log.getErrors());

		control.cleanupTree();
	}

	/**
	 * The table edited in a form reports a grouping by a column it does not declare the same way.
	 */
	public void testEditableUndeclaredGroupingReported() throws Exception {
		becomeUser(_responsible);
		BufferingProtocol log = new BufferingProtocol();
		RowSetTableControl control = editable(log, projects(UNDECLARED, " row-edit='ALL'"));
		control.attach();

		TableView<?> view = control.getTableControl().getView();
		assertEquals(GroupSpec.NONE, view.state().getGrouping());
		assertEquals(0, groupHeaders(view));
		assertContains(UNDECLARED, log.getErrors());

		control.cleanupTree();
	}

	/**
	 * The effective declared grouping keeps the built columns, leaves out a withheld column silently
	 * and reports an undeclared one.
	 */
	public void testEffectiveGrouping() {
		List<Column<Object, ?>> built = List.of(DefaultColumn.<Object, Object> builder(NAME, row -> row).build());
		List<String> declared = List.of(NAME, BUDGET);

		BufferingProtocol log = new BufferingProtocol();
		assertEquals(new GroupSpec(List.of(NAME)),
			ColumnDeclarations.grouping(log, TABLE, new GroupSpec(List.of(BUDGET, NAME)), declared, built));
		assertEquals("A withheld column is no configuration error.", List.of(), log.getErrors());

		assertEquals(new GroupSpec(List.of(NAME)),
			ColumnDeclarations.grouping(log, TABLE, new GroupSpec(List.of(UNDECLARED, NAME)), declared, built));
		assertContains(UNDECLARED, log.getErrors());
	}

	private static void assertUngrouped(TableView<?> view) {
		assertEquals("No row is grouped by a column the user may not read.", 0, groupHeaders(view));
		assertEquals("The project is displayed.", 1, view.rowCount());
		for (ColumnView column : view.columns()) {
			assertFalse("The column the user may not read is not displayed.", column.name().equals(BUDGET));
		}
	}

	private static void assertGroupedAndSorted(TableView<?> view) {
		assertEquals(new GroupSpec(List.of(BUDGET)), view.state().getGrouping());
		assertEquals(List.of(new SortColumn(BUDGET, true)), view.state().getSort());
		assertEquals("One group for the project's budget.", 1, groupHeaders(view));
		assertEquals(SortDirection.ASC, column(view, BUDGET).sortDirection());
	}

	private static ColumnView column(TableView<?> view, String name) {
		for (ColumnView column : view.columns()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new AssertionError("No column '" + name + "'.");
	}

	private static int groupHeaders(TableView<?> view) {
		int result = 0;
		for (Row<?> row : view.rows(0, view.rowCount())) {
			if (row.kind() == RowKind.GROUP_HEADER) {
				result++;
			}
		}
		return result;
	}

	private static void assertContains(String expected, List<String> errors) {
		for (String error : errors) {
			if (error.contains(expected)) {
				return;
			}
		}
		fail("No error mentions '" + expected + "': " + errors);
	}

	private static String projects(String groupBy, String tableAttributes) {
		return String.format(PROJECTS, groupBy, tableAttributes);
	}

	private TableViewControl<?> readOnly(BufferingProtocol log, String xml) throws ConfigurationException {
		return (TableViewControl<?>) table(log, xml).createControl(_context);
	}

	private RowSetTableControl editable(BufferingProtocol log, String xml) throws ConfigurationException {
		FormControl form = new FormControl(_context, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		return (RowSetTableControl) table(log, xml).createControl(_context);
	}

	/**
	 * The table of the given configuration, reporting the problems it finds while it is displayed
	 * to the given log.
	 */
	private static TableElement table(BufferingProtocol log, String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		TableElement result = (TableElement) context.getInstance((PolymorphicConfiguration<?>) parseTable(xml));
		assertEquals(List.of(), log.getErrors());
		return result;
	}

	private static ConfigurationItem parseTable(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableDeclaredGrouping.class);
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
		return suiteWith(TestTableDeclaredGrouping.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
