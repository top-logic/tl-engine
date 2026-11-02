/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl.EditorSession;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.CompositionCellModel;
import com.top_logic.layout.view.form.CompositionEditing;
import com.top_logic.layout.view.form.CompositionRowModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding.RemoveMode;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnDeclaration;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;

/**
 * Tests that a read-only cell of a row-editing {@link RowSetTableControl} shows another row as it
 * is currently edited: the cell of a step listing its substeps shows the pending name of a substep
 * that is a row of the same table, and is built again when that name is changed through its cell.
 * An edited step reads its substeps through their row buffers, also in the dialog of its substeps.
 */
public class TestRowSetTableEditedValues extends AbstractModelAccessTest {

	/** The tag a column declaration is read from. */
	private static final String COLUMN = "column";

	private static final String OLD_NAME = "old substep";

	private static final String NEW_NAME = "renamed substep";

	private static final String DIALOG_NAME = "substep renamed in the dialog";

	private ReactContext _reactContext;

	private TLObject _step;

	private TLObject _substep;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			_substep = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_substep.tUpdateByName(NAME, OLD_NAME);
			_step.tUpdateByName(SUBSTEPS, List.of(_substep));
			_project.tUpdateByName(STEPS, List.of(_step));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_step = null;
		_substep = null;
		super.tearDown();
	}

	/**
	 * Renaming a substep in its cell shows the new name in the cell of its step, before the form
	 * is saved.
	 */
	public void testCellShowsEditedRow() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new QueryRowSetBinding(() -> List.of(_step, _substep), type(STEP), List.of(), RemoveMode.NONE),
			List.of(column("<column attribute='" + NAME + "'/>"),
				column("<column attribute='" + SUBSTEPS + "' readonly='true'/>")),
			RowEditPolicy.SELECTED);
		table.init();
		assertTrue(form.enterEditMode());

		List<TLObject> rows = table.session().currentRows();
		TLObject stepRow = rows.get(0);
		TLObject substepRow = rows.get(1);
		TableView<TLObject> view = table.getTableControl().getView();
		assertTrue(cellText(view, stepRow, SUBSTEPS).contains(OLD_NAME));

		// Edit the name of the substep in its cell.
		table.getTableControl().selectRow(substepRow);
		cellText(view, substepRow, NAME);
		BoundFieldModel nameField =
			(BoundFieldModel) table.session().findRowModel(substepRow).getColumnModels().values().iterator().next();
		nameField.setValue(NEW_NAME);
		assertEquals(NEW_NAME, substepRow.tValueByName(NAME));
		assertEquals("Not saved yet.", OLD_NAME, _substep.tValueByName(NAME));

		String stepCell = cellText(view, stepRow, SUBSTEPS);
		assertTrue(stepCell, stepCell.contains(NEW_NAME));
		assertFalse(stepCell, stepCell.contains(OLD_NAME));
	}

	/**
	 * A container selected after one of its parts was renamed in its own row shows the part as
	 * edited: its composition cell lists the new name, the dialog of the parts edits the buffer of
	 * the part row, and OK writes the dialog's change into that buffer, which the row of the part
	 * shows and the form stores.
	 */
	public void testEditedContainerShowsEditedParts() throws ConfigurationException {
		becomeUser(_root);
		FormControl form = new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new QueryRowSetBinding(() -> List.of(_step, _substep), type(STEP), List.of(), RemoveMode.NONE),
			List.of(column("<column attribute='" + NAME + "'/>"),
				column("<column attribute='" + SUBSTEPS + "'/>")),
			RowEditPolicy.SELECTED);
		table.init();
		assertTrue(form.enterEditMode());

		List<TLObject> rows = table.session().currentRows();
		TLObject stepRow = rows.get(0);
		TLObject substepRow = rows.get(1);
		TableView<TLObject> view = table.getTableControl().getView();

		// Rename the substep in its own row.
		table.getTableControl().selectRow(substepRow);
		cellText(view, substepRow, NAME);
		BoundFieldModel nameField =
			(BoundFieldModel) table.session().findRowModel(substepRow).getColumnModels().values().iterator().next();
		nameField.setValue(NEW_NAME);

		// Select the step: its composition cell lists the substep as edited.
		table.getTableControl().selectRow(stepRow);
		String stepCell = cellText(view, stepRow, SUBSTEPS);
		assertTrue(stepCell, stepCell.contains(NEW_NAME));
		assertFalse(stepCell, stepCell.contains(OLD_NAME));
		CompositionCellModel substeps = compositionCell(table.session().findRowModel(stepRow));
		assertEquals(List.of(substepRow), substeps.getValue());

		// The dialog of the substeps edits on top of the buffer of the substep row.
		EditorSession dialog = new CompositionEditing(substeps).open(substeps, true);
		RowSetTableControl dialogTable = (RowSetTableControl) dialog.createEditor(_reactContext, () -> {
			// No dialog to close.
		});
		TLObject dialogPart = single(dialogTable.session().currentRows());
		assertEquals(NEW_NAME, dialogPart.tValueByName(NAME));
		dialogPart.tUpdateByName(NAME, DIALOG_NAME);
		assertTrue(dialog.apply());

		// OK writes into the buffer of the substep row, which is listed by the step.
		assertEquals(DIALOG_NAME, substepRow.tValueByName(NAME));
		assertEquals(List.of(substepRow), stepRow.tValueByName(SUBSTEPS));
		assertEquals(DIALOG_NAME, nameField.getValue());
		String substepCell = cellText(view, substepRow, NAME);
		assertTrue(substepCell, substepCell.contains(DIALOG_NAME));
		assertEquals("Not saved yet.", OLD_NAME, _substep.tValueByName(NAME));

		form.executeSave();
		assertEquals(DIALOG_NAME, _substep.tValueByName(NAME));
		assertEquals(List.of(_substep), _step.tValueByName(SUBSTEPS));
	}

	private static CompositionCellModel compositionCell(CompositionRowModel row) {
		for (BoundFieldModel model : row.getColumnModels().values()) {
			if (model instanceof CompositionCellModel composition) {
				return composition;
			}
		}
		throw new AssertionError("No composition cell in " + row.getColumnModels());
	}

	private static <T> T single(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	/**
	 * The text of the given cell as the client receives it: the state of its control, or its text.
	 */
	private String cellText(TableView<TLObject> view, TLObject rowObject, String column) {
		for (Row<TLObject> row : view.rows(0, view.rowCount())) {
			if (row.key() == rowObject) {
				CellContent cell = view.cell(row, column);
				if (cell instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory factory) {
					return factory.create(_reactContext).stateAsJSON();
				}
				return String.valueOf(cell);
			}
		}
		throw new AssertionError("No row " + rowObject);
	}

	/** The column declaration read from the given {@value #COLUMN} tag. */
	private static ColumnDeclaration column(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestRowSetTableEditedValues.class);
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
		return suiteWith(TestRowSetTableEditedValues.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
