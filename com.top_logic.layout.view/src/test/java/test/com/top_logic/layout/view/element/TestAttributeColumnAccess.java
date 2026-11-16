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
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.layout.view.model.CommittedModelChanges;
import test.com.top_logic.layout.view.model.CommittedModelChanges.ScopedReactContext;
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
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnExistence;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.ColumnOption;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.TableView;

/**
 * Tests that the column of an attribute follows the access rights of the user while the table is
 * displayed: a column whose attribute the user may read on no row is absent - neither sent nor
 * offered -, appears as soon as the user gains the right to read it and disappears when the user
 * loses it, without the table being built again and keeping the user's arrangement of it.
 *
 * <p>
 * The {@link #_roleless} user gains the role {@value #ROLE_RESPONSIBLE}, the only one granted to
 * read {@value #BUDGET}, by becoming the {@value #RESPONSIBLE} of the {@link #_project}. The change
 * is committed and delivered to the table as a browser window delivers it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestAttributeColumnAccess extends AbstractModelAccessTest {

	/** Tag of the table configurations under test. */
	private static final String TABLE = "table";

	/** Name of the channel deciding about the existence of a column by its condition. */
	private static final String SHOW = "show";

	/** State key of the columns the client displays. */
	private static final String COLUMNS = "columns";

	/** Key of the name of a column in the state the client receives. */
	private static final String COLUMN_NAME = "name";

	/** Name of the preset filtering by {@link #BUDGET}. */
	private static final String BUDGETED = "budgeted";

	/** Width the user gives the {@link #BUDGET} column. */
	private static final int USER_WIDTH = 321;

	/** A table of all projects showing their name and budget, following changes of projects. */
	private static final String PROJECTS = "<table inputs='" + SHOW + "' types='" + qualified(PROJECT) + "'"
		+ " rows='all(`" + qualified(PROJECT) + "`)' observed-types='" + qualified(PROJECT) + "'%s>"
		+ "<columns><column attribute='" + NAME + "'/><column attribute='" + BUDGET + "'%s/></columns>"
		+ "<presets><preset name='" + BUDGETED + "'><criterion column='" + BUDGET + "' expr=\"'b'\"/></preset>"
		+ "</presets></table>";

	private GlobalModelEventForwarder _scope;

	private DefaultViewContext _context;

	private DefaultViewChannel _show;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scope = CommittedModelChanges.modelScope();
		_context = new DefaultViewContext(new ScopedReactContext(_scope));
		_show = new DefaultViewChannel(SHOW);
		_context.registerChannel(SHOW, _show);
	}

	@Override
	protected void tearDown() throws Exception {
		_show = null;
		_context = null;
		_scope = null;
		super.tearDown();
	}

	/**
	 * The column of an attribute the user may read on no row is neither sent nor offered, appears
	 * when the user gains the right and disappears again when the user loses it.
	 */
	public void testColumnFollowsReadRight() throws Exception {
		becomeUser(_roleless);
		TableViewControl<?> control = readOnly(projects("", ""));
		control.attach();

		assertAbsent(control, BUDGET);

		grant();
		assertPresent(control, BUDGET);

		revoke();
		assertAbsent(control, BUDGET);

		control.cleanupTree();
	}

	/**
	 * The width and position the user gave the column survive its absence.
	 */
	public void testArrangementKept() throws Exception {
		becomeUser(_roleless);
		TableViewControl<?> control = readOnly(projects("", ""));
		control.attach();
		grant();
		assertPresent(control, BUDGET);
		TableView<?> view = control.getView();
		view.resizeColumn(BUDGET, USER_WIDTH);
		view.moveColumn(BUDGET, 0);
		assertEquals(List.of(BUDGET, NAME), view.state().getColumnOrder());

		revoke();
		assertAbsent(control, BUDGET);
		assertEquals(Integer.valueOf(USER_WIDTH), view.state().getWidths().get(BUDGET));

		grant();
		assertEquals(List.of(BUDGET, NAME), sentColumns(control));
		assertEquals(Integer.valueOf(USER_WIDTH), view.state().getWidths().get(BUDGET));

		control.cleanupTree();
	}

	/**
	 * The absent columns are handed on only when they change, however often the conditions are
	 * asked again.
	 */
	public void testReportedOnChangeOnly() throws Exception {
		becomeUser(_roleless);
		ColumnResolution scope = new ColumnResolution(type(PROJECT), _context);
		ColumnExistence existence =
			ColumnExistence.of(AttributeColumn.derived(part(PROJECT, BUDGET)).resolve(scope));
		List<Set<String>> reported = new ArrayList<>();
		Registration observation = existence.observe(reported::add);
		assertEquals(Set.of(BUDGET), existence.absentColumns());
		assertEquals(Set.of(BUDGET), existence.withheldColumns());

		existence.recheck();
		existence.recheck();
		assertEquals("Nothing changed.", List.of(), reported);

		grant();
		existence.recheck();
		existence.recheck();
		assertEquals(List.of(Set.of()), reported);
		assertEquals(Set.of(), existence.withheldColumns());

		observation.dispose();
		revoke();
		existence.recheck();
		assertEquals("No report after the observation ended.", 1, reported.size());
	}

	/**
	 * A column with a declared existence condition and an attribute the user may not read exists
	 * only while both its condition holds and the user may read the attribute.
	 */
	public void testConditionAndReadRight() throws Exception {
		becomeUser(_roleless);
		TableViewControl<?> control = readOnly(projects("", " exists-if='$" + SHOW + "'"));
		control.attach();
		_show.set(Boolean.TRUE);
		assertAbsent(control, BUDGET);

		grant();
		assertPresent(control, BUDGET);

		_show.set(Boolean.FALSE);
		assertAbsent(control, BUDGET);

		_show.set(Boolean.TRUE);
		assertPresent(control, BUDGET);

		revoke();
		assertAbsent(control, BUDGET);

		control.cleanupTree();
	}

	/**
	 * A preset with a criterion on a column withheld from the user is not offered, and offered once
	 * the user gains the right to read the column.
	 */
	public void testPresetFollowsReadRight() throws Exception {
		becomeUser(_roleless);
		TableViewControl<?> control = readOnly(projects("", ""));
		control.attach();
		assertFalse(presets(control.getView()).contains(BUDGETED));

		grant();
		assertTrue(presets(control.getView()).contains(BUDGETED));

		revoke();
		assertFalse(presets(control.getView()).contains(BUDGETED));

		control.cleanupTree();
	}

	/**
	 * A table whose rows are edited in a form follows the access rights the same way, its column
	 * and its presets alike.
	 */
	public void testEditableTableFollowsReadRight() throws Exception {
		becomeUser(_roleless);
		FormControl form = new FormControl(_context, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		RowSetTableControl control =
			(RowSetTableControl) table(projects(" row-edit='ALL'", "")).createControl(_context);
		control.attach();
		assertAbsent(control.getTableControl(), BUDGET);
		assertFalse(presets(control.getTableControl().getView()).contains(BUDGETED));

		grant();
		assertPresent(control.getTableControl(), BUDGET);
		assertTrue(presets(control.getTableControl().getView()).contains(BUDGETED));

		revoke();
		assertAbsent(control.getTableControl(), BUDGET);
		assertFalse(presets(control.getTableControl().getView()).contains(BUDGETED));

		control.cleanupTree();
	}

	/** Gives the {@link #_roleless} user the role reading the budget, by a committed change. */
	private void grant() {
		setResponsible(_roleless);
	}

	/** Takes the role reading the budget from the {@link #_roleless} user again. */
	private void revoke() {
		setResponsible(_responsible);
	}

	private void setResponsible(Object person) {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(RESPONSIBLE, person);
			tx.commit();
		}
		_scope.synthesizeModelEvents();
	}

	private static void assertAbsent(TableViewControl<?> control, String column) {
		assertFalse("Not sent: " + column, sentColumns(control).contains(column));
		assertFalse("Not offered: " + column, offered(control.getView()).contains(column));
	}

	private static void assertPresent(TableViewControl<?> control, String column) {
		assertTrue("Sent: " + column, sentColumns(control).contains(column));
		assertTrue("Offered: " + column, offered(control.getView()).contains(column));
	}

	private static String projects(String tableAttributes, String budgetAttributes) {
		return String.format(PROJECTS, tableAttributes, budgetAttributes);
	}

	private TableViewControl<?> readOnly(String xml) throws ConfigurationException {
		return (TableViewControl<?>) table(xml).createControl(_context);
	}

	/** The names of the columns the given control sends to the client, in display order. */
	private static List<String> sentColumns(TableViewControl<?> control) {
		Map<?, ?> state;
		try {
			state = (Map<?, ?>) JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control.", ex);
		}
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

	/** The identifiers of the named filters the given table offers. */
	private static List<String> presets(TableView<?> view) {
		return view.namedFilters().stream().map(NamedFilter::id).toList();
	}

	private static TableElement table(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestAttributeColumnAccess.class);
		TableElement result = (TableElement) context.getInstance((PolymorphicConfiguration<?>) parseTable(xml));
		context.checkErrors();
		return result;
	}

	private static ConfigurationItem parseTable(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestAttributeColumnAccess.class);
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
		return suiteWith(TestAttributeColumnAccess.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
