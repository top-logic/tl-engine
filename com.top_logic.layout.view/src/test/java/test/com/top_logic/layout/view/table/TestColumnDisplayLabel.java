/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.controlprovider.ReactControlProvider;
import com.top_logic.layout.view.element.ResourceDisplayByExpression;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnExport;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.layout.view.table.ScriptedCell;
import com.top_logic.layout.view.table.ScriptedFilter;
import com.top_logic.layout.view.table.ScriptedFilterState;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.FilterState;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RegexpOptionsFilter;
import com.top_logic.table.filter.TextFilterState;

/**
 * Tests a column showing objects through a display that computes their labels: the column sorts,
 * text-filters and exports them by the label the display shows, while a declared comparator or
 * filter keeps its own logic, and a column of typed values keeps sorting and filtering by its type.
 */
public class TestColumnDisplayLabel extends AbstractModelAccessTest {

	/** The column showing a project by its {@link #CODE}. */
	private static final String CODE_COLUMN = "code";

	/** The column showing a project by its {@link #CODE}, ordered by its {@link #NAME}. */
	private static final String BY_NAME_COLUMN = "byName";

	/** The column showing a project by its {@link #CODE}, filtered by a script. */
	private static final String SCRIPTED_COLUMN = "scripted";

	/** The column showing a project by its {@link #CODE}, filtered by regular expressions. */
	private static final String REGEXP_COLUMN = "regexp";

	/** The column showing a project by its own label. */
	private static final String PLAIN_COLUMN = "plain";

	/** Fractional attribute of a {@link #PROJECT}, shown by a display computing a label. */
	private static final String HEIGHT = "height";

	/** The regular expression the {@link #REGEXP_COLUMN} offers. */
	private static final String CODE_PATTERN = "^X-002";

	/** The column showing the {@link #HEIGHT} of a project, filtered by a script. */
	private static final String SCRIPTED_HEIGHT_COLUMN = "scriptedHeight";

	/** Root tag of the parsed column declarations. */
	private static final String COLUMNS_TAG = "columns";

	/** The code of {@link #_project}, whose name sorts after the one of {@link #_otherProject}. */
	private static final String PROJECT_CODE = "X-001";

	/** The code of {@link #_otherProject}, which sorts after the one of {@link #_project}. */
	private static final String OTHER_PROJECT_CODE = "X-002";

	/** The display labelling a project by its code. */
	private static final String CODE_DISPLAY = "<display class='" + ResourceDisplayByExpression.class.getName()
		+ "' label='$value.get(`" + MODULE + ":" + PROJECT + "#" + CODE + "`)'/>";

	/** The columns of a table of projects. */
	private static final String COLUMNS = "<columns>"
		+ "<computed-column name='" + CODE_COLUMN + "' value='$row'>" + CODE_DISPLAY + "</computed-column>"
		+ "<computed-column name='" + BY_NAME_COLUMN + "' value='$row' comparator='compare($left.get(`"
		+ MODULE + ":" + PROJECT + "#" + NAME + "`), $right.get(`" + MODULE + ":" + PROJECT + "#" + NAME + "`))'>"
		+ CODE_DISPLAY + "</computed-column>"
		+ "<computed-column name='" + SCRIPTED_COLUMN + "' value='$row'>" + CODE_DISPLAY
		+ "<filter class='" + ScriptedFilter.class.getName() + "' model='" + MODULE + ":" + PROJECT
		+ "' match=\"$value.get(`" + MODULE + ":" + PROJECT + "#" + NAME + "`) == 'project'\"/>"
		+ "</computed-column>"
		+ "<computed-column name='" + REGEXP_COLUMN + "' value='$row'>" + CODE_DISPLAY
		+ "<filter class='" + RegexpOptionsFilter.class.getName() + "'><pattern regexp='" + CODE_PATTERN
		+ "'/></filter>"
		+ "</computed-column>"
		+ "<computed-column name='" + PLAIN_COLUMN + "' value='$row'/>"
		+ "<column attribute='" + HEIGHT + "'><display class='" + ResourceDisplayByExpression.class.getName()
		+ "' label=\"'#' + $value\"/></column>"
		+ "<computed-column name='" + SCRIPTED_HEIGHT_COLUMN + "' type='tl.core:Double' value='$row.get(`" + MODULE
		+ ":" + PROJECT + "#" + HEIGHT + "`)'>"
		+ "<display class='" + ResourceDisplayByExpression.class.getName() + "' label=\"'#' + $value\"/>"
		+ "<filter class='" + ScriptedFilter.class.getName() + "' model='" + MODULE + ":" + PROJECT
		+ "' match='true'/>"
		+ "</computed-column>"
		+ "</columns>";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(NAME, "project");
			_project.tUpdateByName(CODE, PROJECT_CODE);
			_project.tUpdateByName(HEIGHT, 10.0);
			_otherProject.tUpdateByName(NAME, "otherProject");
			_otherProject.tUpdateByName(CODE, OTHER_PROJECT_CODE);
			_otherProject.tUpdateByName(HEIGHT, 9.0);
			tx.commit();
		}
	}

	/** The cells are read by a user who may read every value. */
	@Override
	protected Person initialUser() {
		return _root;
	}

	/** The text filter of the column matches the label the display shows, not the object's own. */
	public void testTextFilterMatchesTheDisplayLabel() throws ConfigurationException {
		Column<Object, ?> column = column(CODE_COLUMN);

		assertEquals(List.of(_otherProject), matching(column, TextFilterState.contains(OTHER_PROJECT_CODE)));
		assertEquals("The name of a project is not what the column shows.", List.of(),
			matching(column, TextFilterState.contains("otherProject")));
	}

	/** Without a comparator, the column sorts by the label the display shows. */
	public void testSortFollowsTheDisplayLabel() throws ConfigurationException {
		assertEquals(List.of(_project, _otherProject), sorted(column(CODE_COLUMN)));
	}

	/** An export writes the label the display shows, as a text. */
	public void testExportWritesTheDisplayLabel() throws ConfigurationException {
		ColumnSetup setup = setup(CODE_COLUMN);
		Column<Object, ?> column = setup.buildColumn();

		assertEquals(OTHER_PROJECT_CODE, column.exportValue(_otherProject));
		assertNull("The label is written as a text by the default renderer.", setup.exportRenderer());
	}

	/** A declared comparator sorts the column, whatever its display shows. */
	public void testDeclaredComparatorDecides() throws ConfigurationException {
		assertEquals(List.of(_otherProject, _project), sorted(column(BY_NAME_COLUMN)));
	}

	/**
	 * A declared text filter matches the label the display shows.
	 */
	public void testDeclaredTextFilterMatchesTheDisplayLabel() throws ConfigurationException {
		Column<Object, ?> column = column(REGEXP_COLUMN);

		assertEquals(List.of(_otherProject), matching(column, new OptionsFilterState(Set.of(CODE_PATTERN))));
	}

	/**
	 * A scripted filter filters by its own logic, and its column hands the display the cell value,
	 * not the cell pairing it with the row.
	 */
	public void testScriptedFilter() throws ConfigurationException {
		ColumnSetup setup = setup(SCRIPTED_COLUMN);
		Column<Object, ?> column = setup.buildColumn();

		assertEquals("The script decides.", List.of(_project),
			matching(column, new ScriptedFilterState(null, new Object[0])));
		assertEquals(PROJECT_CODE, column.searchText(_project));
		assertEquals(PROJECT_CODE, column.exportValue(_project));
		assertEquals("The column sorts by the label the display shows.", List.of(_project, _otherProject),
			sorted(column));

		RecordingDisplay display = new RecordingDisplay();
		Column<Object, ?> recorded = withDisplay(setup, display).buildColumn();
		assertTrue("The column holds the pairs of value and row.",
			recorded.value(_project) instanceof ScriptedCell);
		assertSame("The display is given the value.", _project, shown(display, recorded.renderCell(_project)));
	}

	/**
	 * A column of numbers sorts and filters them as numbers, also through a display computing a
	 * label, and exports the numbers.
	 */
	public void testTypedColumnKeepsItsType() throws ConfigurationException {
		ColumnSetup setup = setup(HEIGHT);
		Column<Object, ?> column = setup.buildColumn();

		assertEquals("Numeric order, not the one of the labels.", List.of(_otherProject, _project), sorted(column));
		assertTrue(column.filter().get() instanceof ComparableColumnFilter);
		assertEquals(Double.valueOf(10.0), column.exportValue(_project));
	}

	/**
	 * A column of numbers filtered by a script sorts them as numbers, also through a display
	 * computing a label.
	 */
	public void testScriptedFilterOverTypedValuesKeepsTheType() throws ConfigurationException {
		assertEquals("Numeric order, not the one of the labels.", List.of(_otherProject, _project),
			sorted(column(SCRIPTED_HEIGHT_COLUMN)));
	}

	/** A column without a display sorts, filters and exports the objects by their own labels. */
	public void testColumnWithoutDisplay() throws ConfigurationException {
		ColumnSetup setup = setup(PLAIN_COLUMN);
		Column<Object, ?> column = setup.buildColumn();

		assertEquals(List.of(_otherProject), matching(column, TextFilterState.contains("otherProject")));
		assertEquals(List.of(), matching(column, TextFilterState.contains(OTHER_PROJECT_CODE)));
		assertEquals(List.of(_otherProject, _project), sorted(column));
		assertSame(_otherProject, column.exportValue(_otherProject));
		assertEquals(ColumnExport.DEFAULT.rendererFor(setup.type()), setup.exportRenderer());
	}

	/** The rows of the tested table, the project of the lower code first. */
	private List<Object> rows() {
		return List.of(_project, _otherProject);
	}

	/** The rows the given column's filter lets pass in the given state. */
	private <V> List<Object> matching(Column<Object, V> column, FilterState state) {
		Predicate<V> predicate = column.filter().get().predicate(state);
		List<Object> result = new ArrayList<>();
		for (Object row : rows()) {
			if (predicate.test(column.value(row))) {
				result.add(row);
			}
		}
		return result;
	}

	/** The rows in the ascending order of the given column. */
	private <V> List<Object> sorted(Column<Object, V> column) {
		Comparator<V> order = column.sort().get().comparator();
		List<Object> result = new ArrayList<>(rows());
		result.sort((row1, row2) -> order.compare(column.value(row1), column.value(row2)));
		return result;
	}

	/** The value the given display was given by the control factory of the given cell content. */
	private static Object shown(RecordingDisplay display, CellContent content) {
		assertTrue("Not shown through the display: " + content, content instanceof CellContent.Raw);
		((CellControlFactory) ((CellContent.Raw) content).payload()).create(null);
		assertEquals("The display created one control.", 1, display._shown.size());
		return display._shown.get(0);
	}

	/** The given descriptor with the given display. */
	private static ColumnSetup withDisplay(ColumnSetup setup, ReactControlProvider display) {
		return new ColumnSetup(setup.name(), setup.label(), setup.type(), setup.value(), setup.viewContext(),
			setup.binding(), setup.width(), setup.editing(), setup.aggregate(), setup.comparator(),
			setup.hiddenByDefault(), setup.export(), setup.tooltip(), setup.cellClass(), setup.cellRole(), display,
			setup.sortable(), setup.exists(), setup.cellExists());
	}

	/** {@link ReactControlProvider} recording the values it is given, creating no control. */
	private static class RecordingDisplay implements ReactControlProvider {

		final List<Object> _shown = new ArrayList<>();

		@Override
		public ReactControl createControl(ReactContext context, Object model) {
			_shown.add(model);
			return null;
		}
	}

	private static Column<Object, ?> column(String name) throws ConfigurationException {
		return setup(name).buildColumn();
	}

	private static ColumnSetup setup(String name) throws ConfigurationException {
		for (ColumnSetup column : columns()) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new AssertionError("No column '" + name + "'.");
	}

	private static List<ColumnSetup> columns() throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnDisplayLabel.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(COLUMNS_TAG, TypedConfiguration.getConfigurationDescriptor(ColumnsConfig.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(COLUMNS));
		ColumnsConfig config = (ColumnsConfig) reader.read();
		List<ColumnDeclaration> declarations = ColumnDeclarations.instantiate(context, config);
		context.checkErrors();
		return ColumnDeclarations.resolve(declarations, new ColumnResolution(type(PROJECT), null));
	}

	/**
	 * The test suite, additionally starting the services building the columns and the
	 * {@link SearchBuilder} compiling the scripts of the displays and filters.
	 */
	public static Test suite() {
		return suiteWith(TestColumnDisplayLabel.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE);
	}

}
