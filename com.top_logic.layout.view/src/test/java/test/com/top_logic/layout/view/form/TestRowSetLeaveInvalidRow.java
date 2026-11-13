/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactCommandAnswer;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.control.table.SelectRowArguments;
import com.top_logic.layout.react.control.table.TableViewControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.ColumnView;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests that a row with errors of a {@link RowSetTableControl} editing the
 * {@link RowEditPolicy#SELECTED selected row} cannot be left: selecting another row is refused with
 * the errors of the row, which stays selected; a row without errors, removing the row and
 * cancelling the edit are not refused, nor are the other policies affected.
 */
public class TestRowSetLeaveInvalidRow extends AbstractModelAccessTest {

	/** The tag a column declaration is read from. */
	private static final String COLUMN = "column";

	/** The state key telling whether a field offers an input. */
	private static final String EDITABLE = "editable";

	private ReactContext _reactContext;

	private TLObject _step;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			_step.tUpdateByName(ITEMS, List.of(newItem("first"), newItem("second")));
			_project.tUpdateByName(STEPS, List.of(_step));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_step = null;
		super.tearDown();
	}

	/**
	 * A row whose mandatory cell was emptied cannot be left: the click on another row is refused
	 * with the error of the cell, named by its column, and the row stays selected and editable.
	 */
	public void testRowWithErrorIsNotLeft() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.SELECTED);
		TLObject first = row(table, 0);
		TLObject second = row(table, 1);

		click(table, first);
		nameCell(table, first).setValue(null);

		TopLogicException refusal = refusedClick(table, second);
		assertEquals("The refusal moves the focus to the field with the error.",
			ReactCommandAnswer.ERROR_CODE_INVALID_INPUT, CommandErrors.errorCode(refusal));
		String messages = messages(refusal);
		assertTrue("The refusal names the column: " + messages, messages.contains(columnLabel(table)));

		assertEquals(Set.of(first), table.getTableControl().getSelectedKeys());
		assertTrue("The row stays editable.", isEdited(table, first));
		assertFalse(isEdited(table, second));

		// Corrected, the row can be left.
		nameCell(table, first).setValue("corrected");
		click(table, second);
		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
		assertTrue(isEdited(table, second));
	}

	/**
	 * A new row whose mandatory cell was never touched shows no error yet; leaving it is refused,
	 * and the error is shown afterwards. Adding a further row, which selects it, is refused as well
	 * and adds nothing.
	 */
	public void testNewRowRevealsItsErrors() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.SELECTED);
		TLObject first = row(table, 0);

		TLObject added = table.addRow();
		assertNotNull(added);
		assertEquals(Set.of(added), table.getTableControl().getSelectedKeys());
		BoundFieldModel name = nameCell(table, added);
		assertFalse("Not touched yet.", name.isRevealed());

		refusedClick(table, first);
		assertEquals(Set.of(added), table.getTableControl().getSelectedKeys());
		assertTrue("The error is shown now.", name.isRevealed());
		assertNotNull(name.getError());

		int rowCount = table.session().currentRows().size();
		try {
			table.addRow();
			fail("Adding a row must not leave a row with errors.");
		} catch (TopLogicException ex) {
			assertTrue(messages(ex).contains(columnLabel(table)));
		}
		assertEquals("No row is added.", rowCount, table.session().currentRows().size());
		assertEquals(Set.of(added), table.getTableControl().getSelectedKeys());
	}

	/**
	 * A row without errors is left as before.
	 */
	public void testValidRowIsLeft() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.SELECTED);
		TLObject first = row(table, 0);
		TLObject second = row(table, 1);

		click(table, first);
		nameCell(table, first).setValue("renamed");
		click(table, second);

		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
		assertFalse(isEdited(table, first));
		assertTrue(isEdited(table, second));
	}

	/**
	 * A row with errors can be removed, after which another row can be selected.
	 */
	public void testRowWithErrorIsRemoved() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.SELECTED);
		TLObject first = row(table, 0);
		TLObject second = row(table, 1);

		click(table, first);
		nameCell(table, first).setValue(null);
		table.removeRow(first);
		assertEquals(List.of(second), table.session().currentRows());

		click(table, second);
		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
	}

	/**
	 * Cancelling the edit of a form whose selected row has errors is not refused, and afterwards
	 * the rows are selected freely.
	 */
	public void testCancelWithRowWithError() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.SELECTED);
		TLObject first = row(table, 0);

		click(table, first);
		nameCell(table, first).setValue(null);
		form.executeCancel();
		assertFalse(form.isEditMode());

		Object second = rowKey(table, 1);
		clickKey(table, second);
		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
	}

	/**
	 * A selection the server sets is not refused, only the user's changes are.
	 */
	public void testServerSelectionIsNotRefused() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.SELECTED);
		TLObject first = row(table, 0);
		TLObject second = row(table, 1);

		click(table, first);
		nameCell(table, first).setValue(null);
		table.getTableControl().selectRow(second);

		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
	}

	/**
	 * Under {@link RowEditPolicy#ALL}, every row is edited and shows its errors: another row is
	 * selected regardless of errors.
	 */
	public void testAllRowsEditedIsUnchanged() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.ALL);
		TLObject first = row(table, 0);
		TLObject second = row(table, 1);

		click(table, first);
		nameCell(table, first).setValue(null);
		click(table, second);

		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
		assertTrue(isEdited(table, first));
	}

	/**
	 * Under {@link RowEditPolicy#NONE}, no row is edited: the rows are selected freely.
	 */
	public void testNoRowEditedIsUnchanged() throws ConfigurationException {
		FormControl form = newForm();
		RowSetTableControl table = editedTable(form, RowEditPolicy.NONE);
		Object first = rowKey(table, 0);
		Object second = rowKey(table, 1);

		clickKey(table, first);
		clickKey(table, second);

		assertEquals(Set.of(second), table.getTableControl().getSelectedKeys());
	}

	private RowSetTableControl editedTable(FormControl form, RowEditPolicy policy) throws ConfigurationException {
		becomeUser(_root);
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new AttributeRowSetBinding(ITEMS), List.of(column("<column attribute='" + NAME + "'/>")), policy);
		table.init();
		assertTrue(form.enterEditMode());
		return table;
	}

	private FormControl newForm() {
		return new FormControl(_reactContext, _step, "no model", NoTokenHandling.INSTANCE);
	}

	private static TLObject newItem(String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(ITEM));
		result.tUpdateByName(NAME, name);
		return result;
	}

	/** The row object at the given position. */
	private static TLObject row(RowSetTableControl table, int index) {
		return table.session().currentRows().get(index);
	}

	/** The key of the displayed row at the given position. */
	private static Object rowKey(RowSetTableControl table, int index) {
		TableView<TLObject> view = table.getTableControl().getView();
		return view.rows(0, view.rowCount()).get(index).key();
	}

	/** Selects the row of the given row object as the user's click does. */
	private static void click(RowSetTableControl table, TLObject row) {
		clickKey(table, row);
	}

	/** Selects the row with the given key as the user's click does. */
	private static void clickKey(RowSetTableControl table, Object key) {
		TableViewControl<TLObject> control = table.getTableControl();
		TableView<TLObject> view = control.getView();
		List<Row<TLObject>> rows = view.rows(0, view.rowCount());
		for (int n = 0; n < rows.size(); n++) {
			if (rows.get(n).key() == key) {
				control.executeClientCommand(TableViewControl.CMD_SELECT, Map.of(
					SelectRowArguments.ROW_ID, control.rowIdAt(n),
					SelectRowArguments.CTRL_KEY, Boolean.FALSE,
					SelectRowArguments.SHIFT_KEY, Boolean.FALSE));
				return;
			}
		}
		throw new AssertionError("No row " + key);
	}

	/** Clicks the row of the given row object, expecting the click to be refused. */
	private static TopLogicException refusedClick(RowSetTableControl table, TLObject row) {
		try {
			click(table, row);
		} catch (TopLogicException ex) {
			return ex;
		}
		throw new AssertionError("Selecting " + row + " must be refused.");
	}

	/** The messages of the refusal as the user reads them, one per line. */
	private static String messages(TopLogicException refusal) {
		StringBuilder result = new StringBuilder();
		for (ResKey message : CommandErrors.messages(refusal)) {
			result.append(Resources.getInstance().getString(message)).append('\n');
		}
		return result.toString();
	}

	/** The label of the name column, as its header shows it. */
	private static String columnLabel(RowSetTableControl table) {
		for (ColumnView column : table.getTableControl().getView().columns()) {
			if (column.name().equals(NAME)) {
				return Resources.getInstance().getString(column.label());
			}
		}
		throw new AssertionError("No column " + NAME);
	}

	/** The model of the name cell of the given row, built as displaying the cell builds it. */
	private BoundFieldModel nameCell(RowSetTableControl table, TLObject row) {
		cellControl(table, row);
		Map<String, BoundFieldModel> cells = table.session().findRowModel(row).getColumnModels();
		assertEquals(1, cells.size());
		return cells.values().iterator().next();
	}

	/** Whether the row displays its name cell for editing: the cell offers an input. */
	private boolean isEdited(RowSetTableControl table, TLObject row) {
		ReactControl control = cellControl(table, row);
		if (control instanceof ReactFormFieldChromeControl chrome) {
			control = chrome.getField();
		}
		if (control == null) {
			return false;
		}
		try {
			Map<?, ?> state = (Map<?, ?>) JSON.fromString(control.stateAsJSON());
			return Boolean.TRUE.equals(state.get(EDITABLE));
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control.", ex);
		}
	}

	/**
	 * The control displaying the name cell of the given row, built as displaying the cell builds
	 * it; {@code null} for a cell displayed without a control.
	 */
	private ReactControl cellControl(RowSetTableControl table, TLObject rowObject) {
		TableView<TLObject> view = table.getTableControl().getView();
		for (Row<TLObject> row : view.rows(0, view.rowCount())) {
			if (row.key() == rowObject) {
				CellContent cell = view.cell(row, NAME);
				if (cell instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory factory) {
					return factory.create(_reactContext);
				}
				return null;
			}
		}
		throw new AssertionError("No row " + rowObject);
	}

	/** The column declaration read from the given {@value #COLUMN} tag. */
	private static ColumnDeclaration column(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestRowSetLeaveInvalidRow.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(COLUMN,
			TypedConfiguration.getConfigurationDescriptor(AttributeColumn.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ColumnDeclaration result =
			(ColumnDeclaration) context.getInstance((PolymorphicConfiguration<?>) reader.read());
		context.checkErrors();
		return result;
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its data columns and their cells, and the labels of their values.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetLeaveInvalidRow.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
