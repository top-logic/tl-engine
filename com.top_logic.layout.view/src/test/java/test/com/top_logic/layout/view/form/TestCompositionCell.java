/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCompactFieldControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.CompositionCellModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetEditSession;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLObject;

/**
 * Tests the cell of a composition of a row edited in a table: a one-line preview of the parts with
 * a button opening the table of the parts in a dialog, which edits the parts within the edit
 * session of the form.
 */
public class TestCompositionCell extends AbstractModelAccessTest {

	/** The command a button is pressed with. */
	private static final String CLICK = "click";

	private static final String OLD_ITEM = "old item";

	private static final String NEW_ITEM = "new item";

	private static final String RENAMED = "renamed";

	private RecordingDialogManager _dialogs;

	private ReactContext _context;

	private TLObject _step;

	private TLObject _oldItem;

	private FormControl _form;

	private StepsControl _steps;

	private TLObject _stepRow;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		SSEUpdateQueue queue = new SSEUpdateQueue();
		_dialogs = new RecordingDialogManager();
		queue.setDialogManager(_dialogs);
		_context = new DefaultReactContext("", "test", queue, new ReactWindowRegistry("test"));

		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			_oldItem = item(OLD_ITEM);
			_step.tUpdateByName(ITEMS, List.of(_oldItem));
			_step.tUpdateByName(LOCKED_ITEMS, List.of(item(OLD_ITEM)));
			_project.tUpdateByName(STEPS, List.of(_step));
			tx.commit();
		}

		becomeUser(_responsible);
		_form = new FormControl(_context, _project, "no model", NoTokenHandling.INSTANCE);
		_steps = new StepsControl(_form);
		_steps.init();
		assertTrue(_form.enterEditMode());
		_stepRow = single(_steps.session().currentRows());
	}

	private static TLObject item(String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(ITEM));
		result.tUpdateByName(NAME, name);
		return result;
	}

	/**
	 * The composition cell of an edited row shows the labels of the parts and a button opening
	 * their editor.
	 */
	public void testEditableCompositionCell() {
		ReactCompactFieldControl cell = cell(ITEMS);

		assertTrue(cell.getFieldModel() instanceof CompositionCellModel);
		assertTrue(cell.getFieldModel().isEditable());
		assertEquals(OLD_ITEM, cell.getPreviewText());

		open(cell);
		assertNotNull(_dialogs._open);
		assertEquals(1, descendants(_dialogs._open, RowSetTableControl.class).size());
		assertEquals(2, dialogActions().size());
	}

	/**
	 * Cancel returns the parts to the state they had when the dialog opened: an added part is
	 * dropped, a changed part holds its value again.
	 */
	public void testCancelRestores() {
		ReactCompactFieldControl cell = cell(ITEMS);
		RowSetEditSession parts = session(cell);
		TLObject oldItem = single(parts.currentRows());

		open(cell);
		assertNotNull(parts.addRow(row -> row.tUpdateByName(NAME, NEW_ITEM)));
		oldItem.tUpdateByName(NAME, RENAMED);
		assertTrue(_form.isDirty());

		press(dialogActions().get(0));

		assertNull(_dialogs._open);
		assertSame(oldItem, single(parts.currentRows()));
		assertEquals(OLD_ITEM, oldItem.tValueByName(NAME));
		assertEquals(List.of(oldItem), _stepRow.tValueByName(ITEMS));
		assertEquals(OLD_ITEM, cell.getPreviewText());
		assertFalse(_form.isDirty());
	}

	/**
	 * Cancel restores the values a part added in an earlier dialog had when the dialog opened.
	 */
	public void testCancelRestoresNewPartValues() {
		ReactCompactFieldControl cell = cell(ITEMS);
		RowSetEditSession parts = session(cell);

		open(cell);
		TLObject added = parts.addRow(row -> row.tUpdateByName(NAME, NEW_ITEM));
		press(dialogActions().get(1));
		assertNull(_dialogs._open);

		open(cell);
		added.tUpdateByName(NAME, RENAMED);
		parts.removeRow(parts.currentRows().get(0));
		press(dialogActions().get(0));

		assertEquals(2, parts.currentRows().size());
		assertEquals(NEW_ITEM, added.tValueByName(NAME));
		assertEquals(OLD_ITEM + ", " + NEW_ITEM, cell.getPreviewText());
	}

	/**
	 * OK keeps an added part in the edit session, and the form saves it with the row.
	 */
	public void testOkKeepsPartUntilSave() {
		ReactCompactFieldControl cell = cell(ITEMS);
		RowSetEditSession parts = session(cell);

		open(cell);
		assertNotNull(parts.addRow(row -> row.tUpdateByName(NAME, NEW_ITEM)));
		press(dialogActions().get(1));

		assertNull(_dialogs._open);
		assertEquals(2, ((List<?>) _stepRow.tValueByName(ITEMS)).size());
		assertEquals(OLD_ITEM + ", " + NEW_ITEM, cell.getPreviewText());
		assertEquals(List.of(_oldItem), _step.tValueByName(ITEMS));
		assertTrue(_form.isDirty());

		_form.executeSave();

		List<?> saved = (List<?>) _step.tValueByName(ITEMS);
		assertEquals(2, saved.size());
		assertSame(_oldItem, saved.get(0));
		TLObject created = (TLObject) saved.get(1);
		assertFalse(created.tTransient());
		assertEquals(NEW_ITEM, created.tValueByName(NAME));
	}

	/**
	 * OK keeps the dialog open while a part is not valid.
	 */
	public void testOkRefusedForInvalidPart() {
		ReactCompactFieldControl cell = cell(ITEMS);
		RowSetEditSession parts = session(cell);

		open(cell);
		TLObject added = parts.addRow(null);
		assertNotNull(added);
		press(dialogActions().get(1));
		assertNotNull("A part without its mandatory name keeps the dialog open.", _dialogs._open);

		added.tUpdateByName(NAME, NEW_ITEM);
		press(dialogActions().get(1));
		assertNull(_dialogs._open);
	}

	/**
	 * The parts of a composition the user may not write are displayed read-only, in a dialog that
	 * only offers to close it.
	 */
	public void testReadOnlyComposition() {
		ReactCompactFieldControl cell = cell(LOCKED_ITEMS);
		assertFalse(cell.getFieldModel().isEditable());
		RowSetEditSession parts = session(cell);

		open(cell);
		assertEquals(1, dialogActions().size());
		assertNull(parts.addRow(null));

		press(dialogActions().get(0));
		assertNull(_dialogs._open);
		assertEquals(1, parts.currentRows().size());
	}

	/**
	 * The dialog closes when the form leaves edit mode, which ends the edit of the parts.
	 */
	public void testLeavingEditModeClosesDialog() {
		ReactCompactFieldControl cell = cell(ITEMS);
		RowSetEditSession parts = session(cell);

		open(cell);
		assertNotNull(_dialogs._open);

		_form.executeCancel();

		assertNull(_dialogs._open);
		assertFalse(parts.isRunning());
	}

	/**
	 * Creates the cell of the given composition of the edited step, as the table of the steps
	 * creates it.
	 */
	private ReactCompactFieldControl cell(String composition) {
		// Whether the column is offered at all is decided for the type, which is not under test.
		becomeUser(_root);
		ColumnSetup column = single(AttributeColumn.derived(type(STEP).getPartOrFail(composition))
			.resolve(new ColumnResolution(type(STEP), null)));
		becomeUser(_responsible);
		BoundFieldModel model = _steps.session().cellModel(_stepRow, column);
		assertNotNull(model);
		ReactControl control = column.editing().createControl(_context, _stepRow, model);
		assertTrue(control instanceof ReactCompactFieldControl);
		return (ReactCompactFieldControl) control;
	}

	private static RowSetEditSession session(ReactCompactFieldControl cell) {
		return ((CompositionCellModel) cell.getFieldModel()).session();
	}

	private void open(ReactCompactFieldControl cell) {
		press(descendants(cell, ReactButtonControl.class).get(0));
	}

	private static void press(ReactButtonControl button) {
		button.executeCommand(CLICK, Map.of());
	}

	/**
	 * The buttons of the open dialog itself, without the ones of the table it displays.
	 */
	private List<ReactButtonControl> dialogActions() {
		assertNotNull("A dialog is open", _dialogs._open);
		List<ReactButtonControl> result = new ArrayList<>();
		collectActions(_dialogs._open, result);
		return result;
	}

	private static void collectActions(ReactControl control, List<ReactButtonControl> result) {
		if (control instanceof RowSetTableControl) {
			return;
		}
		if (control instanceof ReactButtonControl button) {
			result.add(button);
			return;
		}
		for (ReactControl child : control.displayedChildren()) {
			collectActions(child, result);
		}
	}

	private static <T> List<T> descendants(ReactControl root, Class<T> type) {
		List<T> result = new ArrayList<>();
		collect(root, type, result);
		return result;
	}

	private static <T> void collect(ReactControl control, Class<T> type, List<T> result) {
		if (type.isInstance(control)) {
			result.add(type.cast(control));
			return;
		}
		for (ReactControl child : control.displayedChildren()) {
			collect(child, type, result);
		}
	}

	private static <T> T single(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	/**
	 * {@link AbstractCompositionControl} over the steps of the form object, without a presentation.
	 */
	private static class StepsControl extends AbstractCompositionControl {

		StepsControl(FormControl form) {
			super(form.getReactContext(), form, STEPS, "TLPanel");
		}

		@Override
		protected void buildContent(List<? extends TLObject> rows, boolean editMode) {
			// No presentation.
		}

		@Override
		protected void refreshRows() {
			// No presentation.
		}
	}

	/**
	 * A {@link DialogManager} holding a single open dialog.
	 */
	private static final class RecordingDialogManager implements DialogManager {

		ReactControl _open;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_open = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_open = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			// Only one dialog.
		}
	}

	/**
	 * The test suite, starting the theme and resources the dialog needs for its buttons and the
	 * label providers the preview needs.
	 */
	public static Test suite() {
		return suiteWith(TestCompositionCell.class, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE);
	}

}
