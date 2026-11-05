/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.TLUnit;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnDeclarations;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.layout.view.table.ColumnsConfig;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.util.Resources;

/**
 * Tests for the {@link TLUnit unit} a table cell writes after a number: shown by the display of the
 * cell as a view-mode form field shows it, and part of the text the cell is searched by - a
 * constant for every row, a function computed for the object owning the value of each row.
 */
public class TestColumnUnit extends AbstractModelAccessTest {

	/** Whole number attribute of a {@link #PROJECT} whose unit is a text in several languages. */
	private static final String DURATION = "duration";

	/** Fractional attribute of a {@link #PROJECT} whose unit is the name of the project. */
	private static final String DISTANCE = "distance";

	/** Fractional attribute of a {@link #PROJECT} without a unit. */
	private static final String HEIGHT = "height";

	/** Name of the embedded column showing the {@link #DISTANCE} of the project a task is created in. */
	private static final String CREATED_IN_DISTANCE = CREATED_IN + "." + DISTANCE;

	/** Root tag of the parsed column declarations. */
	private static final String COLUMNS_TAG = "columns";

	/** The columns of a table of projects. */
	private static final String PROJECT_COLUMNS = "<columns>"
		+ "<column attribute='" + DURATION + "'/>"
		+ "<column attribute='" + DISTANCE + "'/>"
		+ "<column attribute='" + HEIGHT + "'/>"
		+ "</columns>";

	/** The columns of a table of tasks, showing the distance of the project a task is created in. */
	private static final String TASK_COLUMNS = "<columns>"
		+ "<embedded-columns reference='" + CREATED_IN + "'><column attribute='" + DISTANCE + "'/></embedded-columns>"
		+ "</columns>";

	/** The columns of a table of projects filtering the {@link #DISTANCE} by a script. */
	private static final String SCRIPTED_COLUMNS = "<columns>"
		+ "<column attribute='" + DISTANCE + "'>"
		+ "<filter class='com.top_logic.layout.view.table.ScriptedFilter' model='" + MODULE + ":" + PROJECT
		+ "' match='true'/>"
		+ "</column>"
		+ "</columns>";

	private static final double PROJECT_DISTANCE = 1.5;

	private static final double OTHER_DISTANCE = 2.5;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(DURATION, 120L);
			_project.tUpdateByName(DISTANCE, PROJECT_DISTANCE);
			_project.tUpdateByName(HEIGHT, 3.5);
			_otherProject.tUpdateByName(DISTANCE, OTHER_DISTANCE);
			_task.tUpdateByName(CREATED_IN, _project);
			tx.commit();
		}
	}

	/** The cells are read by a user who may read every value. */
	@Override
	protected Person initialUser() {
		return _root;
	}

	/** A constant unit is shown after the number of every cell and is part of its search text. */
	public void testConstantUnit() throws ConfigurationException {
		Column<Object, ?> column = projectColumn(DURATION);
		String unit = Resources.getInstance()
			.getString(ResKey.builder().add(Locale.ENGLISH, "min").add(Locale.GERMAN, "Min.").build());

		assertEquals(unit, cellUnit(column.renderCell(_project)));
		assertEquals(number(DURATION, 120L) + " " + unit, column.searchText(_project));
	}

	/** A function computes the unit of each row's cell from the object of the row. */
	public void testFunctionUnitPerRow() throws ConfigurationException {
		Column<Object, ?> column = projectColumn(DISTANCE);

		assertEquals("project", cellUnit(column.renderCell(_project)));
		assertEquals("otherProject", cellUnit(column.renderCell(_otherProject)));
		assertEquals(number(DISTANCE, PROJECT_DISTANCE) + " project", column.searchText(_project));
		assertEquals(number(DISTANCE, OTHER_DISTANCE) + " otherProject", column.searchText(_otherProject));
	}

	/** A column filtered by a script shows and searches the unit of each row as any other column. */
	public void testScriptedFilterColumnComputesTheUnitPerRow() throws ConfigurationException {
		Column<Object, ?> column = find(columns(SCRIPTED_COLUMNS, PROJECT), DISTANCE).buildColumn();

		assertEquals("otherProject", cellUnit(column.renderCell(_otherProject)));
		assertEquals(number(DISTANCE, OTHER_DISTANCE) + " otherProject", column.searchText(_otherProject));
	}

	/** A number without a unit is shown and searched by its number only. */
	public void testNoUnit() throws ConfigurationException {
		Column<Object, ?> column = projectColumn(HEIGHT);

		assertNull(cellUnit(column.renderCell(_project)));
		assertEquals(number(HEIGHT, 3.5), column.searchText(_project));
	}

	/**
	 * A column showing what an object referenced by the row holds computes the unit for that
	 * object.
	 */
	public void testEmbeddedColumnComputesTheUnitForTheReferencedObject() throws ConfigurationException {
		Column<Object, ?> column = find(columns(TASK_COLUMNS, TASK), CREATED_IN_DISTANCE).buildColumn();

		assertEquals("project", cellUnit(column.renderCell(_task)));
		assertEquals(number(DISTANCE, PROJECT_DISTANCE) + " project", column.searchText(_task));
	}

	/**
	 * A value shown without a row - the value a group of rows is formed by - has no object owning
	 * it: a function is not called, a constant is still written.
	 */
	public void testValueWithoutRowWritesAConstantUnitOnly() throws ConfigurationException {
		assertNull(cellUnit(render(projectColumn(DISTANCE), PROJECT_DISTANCE)));
		assertNotNull(cellUnit(render(projectColumn(DURATION), 120L)));
	}

	/** Cells collecting the values of several objects have no single object owning them. */
	public void testCollectedValuesHaveNoOwner() {
		ColumnType type = ColumnType.of(part(PROJECT, DISTANCE));

		assertSame(_project, type.ownerOf(_project));
		assertNull(type.collected().ownerOf(_project));
		assertNull(type.ownerOf(null));
	}

	/** The unit written in a cell: the one of the number input displaying it. */
	private static String cellUnit(CellContent content) {
		assertTrue("Not displayed by a control: " + content, content instanceof CellContent.Raw);
		CellControlFactory factory = (CellControlFactory) ((CellContent.Raw) content).payload();
		ReactControl control = factory.create(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		assertTrue("A number input displays the value, but is: " + control,
			control instanceof ReactNumberInputControl);
		return ((ReactNumberInputControl) control).getUnit();
	}

	@SuppressWarnings("unchecked")
	private static <V> CellContent render(Column<Object, V> column, Object value) {
		return column.renderer().render((V) value);
	}

	/** The given number written in the format of the given attribute of a project. */
	private static String number(String attribute, Object value) {
		TLStructuredTypePart part = part(PROJECT, attribute);
		return FieldControlService.numberFormat(part, part.getType()).format(value);
	}

	private static Column<Object, ?> projectColumn(String name) throws ConfigurationException {
		return find(columns(PROJECT_COLUMNS, PROJECT), name).buildColumn();
	}

	private static ColumnSetup find(List<ColumnSetup> columns, String name) {
		for (ColumnSetup column : columns) {
			if (column.name().equals(name)) {
				return column;
			}
		}
		throw new AssertionError("No column '" + name + "' in " + columns.stream().map(ColumnSetup::name).toList());
	}

	private static List<ColumnSetup> columns(String columns, String rowType) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestColumnUnit.class);
		Map<String, ConfigurationDescriptor> descriptors = new HashMap<>();
		descriptors.put(COLUMNS_TAG, TypedConfiguration.getConfigurationDescriptor(ColumnsConfig.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(columns));
		ColumnsConfig config = (ColumnsConfig) reader.read();
		List<ColumnDeclaration> declarations = ColumnDeclarations.instantiate(context, config);
		context.checkErrors();
		return ColumnDeclarations.resolve(declarations, new ColumnResolution(type(rowType), null));
	}

	/**
	 * The test suite, additionally starting the services building the columns and the
	 * {@link SearchBuilder} compiling the scripts computing the units.
	 */
	public static Test suite() {
		return suiteWith(TestColumnUnit.class, TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE);
	}

}
