/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.servlet.DeliveryScope;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.ChildControl;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.AbstractTableElement;
import com.top_logic.layout.view.element.TableElement;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.CompositionRowModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the {@link AbstractTableElement.Config#getRowEditable() row condition} of a row-editing
 * table: a refused row shows every cell read-only, also the cell of a computed column, and cannot
 * be removed; an accepted row is edited and removed as without the condition.
 */
public class TestTableRowEditable extends AbstractModelAccessTest {

	private static final String TABLE = "table";

	/** The channel holding the rows of the table. */
	private static final String STEPS_CHANNEL = "steps";

	/** The name of the computed column. */
	private static final String COMPUTED = "c";

	/** The name of the step the {@link #ROW_EDITABLE} condition refuses. */
	private static final String FIXED = "fixed";

	/** The name of a step. */
	private static final String STEP_NAME = qualified(STEP) + "#" + NAME;

	/** Name of the attribute of a step whose cell {@link #CAN_UPDATE_NOTE} restricts. */
	private static final String NOTE_ATTRIBUTE = "note";

	/** Name of the attribute of a step {@link #CAN_UPDATE_NOTE} reads. */
	private static final String STATE = "state";

	/**
	 * The {@link #STATE} of a step whose note is not edited; a state of the test model names the
	 * mode of another attribute of the step.
	 */
	private static final String LOCKED = "read-only";

	/** The note column, edited unless the step is {@link #LOCKED}. */
	private static final String CAN_UPDATE_NOTE = "<column attribute='" + NOTE_ATTRIBUTE + "'>"
		+ "<can-update>$row.get(`" + qualified(STEP) + "#" + STATE + "`) != '" + LOCKED + "'</can-update>"
		+ "</column>";

	/** The state of the table control: its displayed rows. */
	private static final String ROWS_STATE = "rows";

	/** The state of a displayed row: its cells by column name. */
	private static final String CELLS_STATE = "cells";

	/** The row condition refusing the step named {@link #FIXED}. */
	private static final String ROW_EDITABLE =
		"<row-editable>$row.get(`" + STEP_NAME + "`) != '" + FIXED + "'</row-editable>";

	private ReactContext _reactContext;

	private DefaultViewContext _context;

	private TLObject _open;

	private TLObject _fixed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_context = new DefaultViewContext(_reactContext);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_open = newStep("open");
			_fixed = newStep(FIXED);
			_project.tUpdateByName(STEPS, List.of(_open, _fixed));
			tx.commit();
		}
		DefaultViewChannel steps = new DefaultViewChannel(STEPS_CHANNEL);
		steps.set(List.of(_open, _fixed));
		_context.registerChannel(STEPS_CHANNEL, steps);
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_context = null;
		_open = null;
		_fixed = null;
		super.tearDown();
	}

	/**
	 * The tests start as the super-user, so that only the row condition and the model decide.
	 */
	@Override
	protected Person initialUser() {
		return _root;
	}

	/**
	 * A refused row shows the cells of an attribute column and of a computed column read-only, an
	 * accepted row edits them.
	 */
	public void testRefusedRowNotEdited() throws Exception {
		RowSetTableControl table = editedTable(ROW_EDITABLE);

		assertNotNull(cellModel(table, _open, NAME));
		assertNotNull(cellModel(table, _open, COMPUTED));
		assertNull(cellModel(table, _fixed, NAME));
		assertNull(cellModel(table, _fixed, COMPUTED));
	}

	/**
	 * A refused row has no delete button, an accepted one has.
	 */
	public void testRefusedRowHasNoDeleteButton() throws Exception {
		RowSetTableControl table = editedTable(ROW_EDITABLE);

		assertTrue(cell(table, _open, RowSetTableControl.COLUMN_DELETE) instanceof CellContent.Raw);
		assertEquals(CellContent.empty(), cell(table, _fixed, RowSetTableControl.COLUMN_DELETE));
		assertTrue(table.isRemovable(row(table, _open).data()));
		assertFalse(table.isRemovable(row(table, _fixed).data()));
	}

	/**
	 * The removal of the selected rows cannot be executed while a refused row is selected.
	 */
	public void testSelectionWithRefusedRowNotRemoved() throws Exception {
		RowSetTableControl table = editedTable(ROW_EDITABLE);
		CommandModel command = table.getRemoveSelectedRowsCommand();
		assertTrue(command.isVisible());

		table.getTableControl().selectRows(List.of(row(table, _open).key(), row(table, _fixed).key()));
		assertFalse("A refused row is selected.", command.isExecutable());
		try {
			command.executeCommand(_reactContext);
			fail("The removal must be refused while a refused row is selected.");
		} catch (TopLogicException ex) {
			// Expected.
		}
		assertEquals("Nothing removed.", 2, table.session().currentRows().size());

		table.getTableControl().selectRows(List.of(row(table, _open).key()));
		assertTrue(command.isExecutable());
		command.executeCommand(_reactContext);
		assertEquals(1, table.session().currentRows().size());
	}

	/**
	 * Without a row condition, every row is edited and removed as its columns and the table offer.
	 */
	public void testWithoutCondition() throws Exception {
		RowSetTableControl table = editedTable("");

		assertNotNull(cellModel(table, _fixed, NAME));
		assertNotNull(cellModel(table, _fixed, COMPUTED));
		assertTrue(cell(table, _fixed, RowSetTableControl.COLUMN_DELETE) instanceof CellContent.Raw);

		table.getTableControl().selectRows(List.of(row(table, _open).key(), row(table, _fixed).key()));
		assertTrue(table.getRemoveSelectedRowsCommand().isExecutable());
	}

	/**
	 * The decisions about the edit of a row follow the edited values of the row they read: a cell
	 * whose <code>can-update</code> reads a value turns read-only and editable again as the value
	 * changes, a row the row condition refuses after a change of its name loses its delete button
	 * and the edit of its cells. A change of a value no decision reads keeps the cells of the row.
	 */
	public void testDecisionsFollowEditedValues() throws Exception {
		RowSetTableControl table = editedTable(ROW_EDITABLE, CAN_UPDATE_NOTE);
		TLObject open = (TLObject) row(table, _open).data();
		String editor = module(cells(table, _open), NOTE_ATTRIBUTE);
		String display = module(cells(table, _fixed), NOTE_ATTRIBUTE);
		String deleteButton = module(cells(table, _open), RowSetTableControl.COLUMN_DELETE);
		assertFalse("An edited cell is shown by its input.", editor.equals(display));

		// A value no decision reads: the cells of the row are kept.
		Map<?, ?> before = cells(table, _open);
		interaction(() -> cellModel(table, _open, NOTE_ATTRIBUTE).setValue("noted"));
		Map<?, ?> after = cells(table, _open);
		assertEquals(controlId(before, NOTE_ATTRIBUTE), controlId(after, NOTE_ATTRIBUTE));
		assertEquals(controlId(before, NAME), controlId(after, NAME));

		// The value the condition of the note column reads.
		interaction(() -> open.tUpdateByName(STATE, LOCKED));
		assertEquals(display, module(cells(table, _open), NOTE_ATTRIBUTE));
		assertEquals("The other cells stay edited.", editor, module(cells(table, _open), NAME));
		interaction(() -> open.tUpdateByName(STATE, null));
		assertEquals(editor, module(cells(table, _open), NOTE_ATTRIBUTE));

		// The value the row condition reads, entered in the cell of the row.
		interaction(() -> cellModel(table, _open, NAME).setValue(FIXED));
		Map<?, ?> refused = cells(table, _open);
		assertEquals(display, module(refused, NAME));
		assertEquals(display, module(refused, NOTE_ATTRIBUTE));
		assertFalse("No delete button.", deleteButton.equals(module(refused, RowSetTableControl.COLUMN_DELETE)));
		interaction(() -> open.tUpdateByName(NAME, "reopened"));
		Map<?, ?> accepted = cells(table, _open);
		assertEquals(editor, module(accepted, NAME));
		assertEquals(deleteButton, module(accepted, RowSetTableControl.COLUMN_DELETE));
	}

	/**
	 * Runs the given change as an interaction of the client does: the work it schedules for the
	 * completed interaction runs when it has completed.
	 */
	private static void interaction(Runnable change) {
		DeliveryScope.enter();
		try {
			change.run();
		} finally {
			DeliveryScope.exit();
		}
	}

	/**
	 * The cells of the given step as the client receives them, by column name.
	 */
	private Map<?, ?> cells(RowSetTableControl table, TLObject step) {
		TableView<?> view = table.getTableControl().getView();
		int index = view.rows(0, view.rowCount()).indexOf(row(view, step));
		Map<?, ?> state = parseJSON(table.getTableControl().stateAsJSON());
		Map<?, ?> row = (Map<?, ?>) ((List<?>) state.get(ROWS_STATE)).get(index);
		return (Map<?, ?>) row.get(CELLS_STATE);
	}

	private static String module(Map<?, ?> cells, String column) {
		return (String) ((Map<?, ?>) cells.get(column)).get(ChildControl.MODULE__PROP);
	}

	private static String controlId(Map<?, ?> cells, String column) {
		return (String) ((Map<?, ?>) cells.get(column)).get(ChildControl.CONTROL_ID__PROP);
	}

	private static Map<?, ?> parseJSON(String json) {
		try {
			return (Map<?, ?>) JSON.fromString(json);
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + json, ex);
		}
	}

	/**
	 * Builds a row-editing table over the steps with the given row condition, in a form over the
	 * project, and enters edit mode.
	 */
	private RowSetTableControl editedTable(String rowEditable) throws Exception {
		return editedTable(rowEditable, "<computed-column name='" + COMPUTED + "' type='tl.core:String'"
			+ " value=\"$row.get(`" + STEP_NAME + "`)\" update=\"$row.set(`" + STEP_NAME + "`, $value)\"/>");
	}

	/**
	 * Builds a row-editing table over the steps with the given row condition and a name column
	 * followed by the given columns, in a form over the project, and enters edit mode.
	 */
	private RowSetTableControl editedTable(String rowEditable, String columns) throws Exception {
		TableElement element = table("<table inputs='" + STEPS_CHANNEL + "' rows='$" + STEPS_CHANNEL + "'"
			+ " row-edit='ALL' on-remove='DELETE' selection-mode='MULTI'>"
			+ rowEditable
			+ "<columns>"
			+ "<column attribute='" + NAME + "'/>"
			+ columns
			+ "</columns></table>");
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		_context.setFormModel(form);
		RowSetTableControl control = (RowSetTableControl) element.createControl(_context);
		assertTrue(form.enterEditMode());
		return control;
	}

	/**
	 * The field model the given cell is edited with, once the cell is displayed, {@code null} if it
	 * is not edited.
	 */
	private BoundFieldModel cellModel(RowSetTableControl table, TLObject step, String column) {
		CellContent cell = cell(table, step, column);
		if (cell instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory factory) {
			factory.create(_reactContext);
		}
		CompositionRowModel rowModel = table.session().findRowModel((TLObject) row(table, step).data());
		assertNotNull(rowModel);
		return rowModel.getColumnModel(column);
	}

	private CellContent cell(RowSetTableControl table, TLObject step, String column) {
		return cell(table.getTableControl().getView(), step, column);
	}

	private static <R> CellContent cell(TableView<R> view, TLObject step, String column) {
		return view.cell(row(view, step), column);
	}

	private static Row<?> row(RowSetTableControl table, TLObject step) {
		return row(table.getTableControl().getView(), step);
	}

	private static <R> Row<R> row(TableView<R> view, TLObject step) {
		for (Row<R> row : view.rows(0, view.rowCount())) {
			if (row.data() == step
				|| (row.data() instanceof TLObjectOverlay overlay && overlay.getBase() == step)) {
				return row;
			}
		}
		throw new AssertionError("No row for " + step.tValueByName(NAME));
	}

	private static TLObject newStep(String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
		result.tUpdateByName(NAME, name);
		return result;
	}

	private static TableElement table(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestTableRowEditable.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap(TABLE, TypedConfiguration.getConfigurationDescriptor(TableElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		TableElement result = (TableElement) context.getInstance((PolymorphicConfiguration<?>) reader.read());
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its columns and their cells, and the scripting the conditions are computed with.
	 */
	public static Test suite() {
		return suiteWith(TestTableRowEditable.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			FieldControlService.Module.INSTANCE, ColumnProviderService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
