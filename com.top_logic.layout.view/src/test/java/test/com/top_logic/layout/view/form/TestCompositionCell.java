/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
 * a button opening the table of the parts in a dialog, which edits the parts on a level of its own
 * - nested as deep as the compositions are.
 */
public class TestCompositionCell extends AbstractModelAccessTest {

	/** The command a button is pressed with. */
	private static final String CLICK = "click";

	private static final String OLD_ITEM = "old item";

	private static final String NEW_ITEM = "new item";

	private static final String RENAMED = "renamed";

	private static final String SUBSTEP = "substep";

	private static final String NEW_SUBSTEP = "new substep";

	private RecordingDialogManager _dialogs;

	private ReactContext _context;

	private TLObject _step;

	private TLObject _oldItem;

	private TLObject _substep;

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
			_step = named(STEP, "step");
			_oldItem = named(ITEM, OLD_ITEM);
			_step.tUpdateByName(ITEMS, List.of(_oldItem));
			_step.tUpdateByName(LOCKED_ITEMS, List.of(named(ITEM, OLD_ITEM)));
			_substep = named(STEP, SUBSTEP);
			_step.tUpdateByName(SUBSTEPS, List.of(_substep));
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

	private static TLObject named(String type, String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(type));
		result.tUpdateByName(NAME, name);
		return result;
	}

	/**
	 * The composition cell of an edited row shows the labels of the parts and a button opening
	 * their table in a dialog.
	 */
	public void testEditableCompositionCell() {
		ReactCompactFieldControl cell = cell(ITEMS);

		assertTrue(cell.getFieldModel() instanceof CompositionCellModel);
		assertTrue(cell.getFieldModel().isEditable());
		assertEquals(OLD_ITEM, cell.getPreviewText());

		open(cell);
		assertEquals(1, _dialogs._open.size());
		assertEquals(OLD_ITEM, single(rowsOfTopDialog().currentRows()).tValueByName(NAME));
		assertEquals(2, dialogActions().size());
	}

	/**
	 * Cancel drops the dialog's changes: an added part and a changed part leave the row as it was.
	 */
	public void testCancelDropsChanges() {
		ReactCompactFieldControl cell = cell(ITEMS);

		open(cell);
		RowSetEditSession parts = rowsOfTopDialog();
		assertNotNull(parts.addRow(row -> row.tUpdateByName(NAME, NEW_ITEM)));
		parts.currentRows().get(0).tUpdateByName(NAME, RENAMED);

		press(dialogActions().get(0));

		assertTrue(_dialogs._open.isEmpty());
		assertEquals(List.of(_oldItem), _stepRow.tValueByName(ITEMS));
		assertEquals(OLD_ITEM, _oldItem.tValueByName(NAME));
		assertEquals(OLD_ITEM, cell.getPreviewText());
		assertFalse(_form.isDirty());
	}

	/**
	 * OK writes the parts into the row; the form stores them with the row.
	 */
	public void testOkKeepsPartsUntilSave() {
		ReactCompactFieldControl cell = cell(ITEMS);

		open(cell);
		RowSetEditSession parts = rowsOfTopDialog();
		assertNotNull(parts.addRow(row -> row.tUpdateByName(NAME, NEW_ITEM)));
		parts.currentRows().get(0).tUpdateByName(NAME, RENAMED);
		press(dialogActions().get(1));

		assertTrue(_dialogs._open.isEmpty());
		assertEquals(2, ((List<?>) _stepRow.tValueByName(ITEMS)).size());
		assertEquals(RENAMED + ", " + NEW_ITEM, cell.getPreviewText());
		assertEquals("Nothing is stored before the form is saved.", List.of(_oldItem), _step.tValueByName(ITEMS));
		assertEquals(OLD_ITEM, _oldItem.tValueByName(NAME));
		assertTrue(_form.isDirty());

		_form.executeSave();

		List<?> saved = (List<?>) _step.tValueByName(ITEMS);
		assertEquals(2, saved.size());
		assertSame(_oldItem, saved.get(0));
		assertEquals(RENAMED, _oldItem.tValueByName(NAME));
		TLObject created = (TLObject) saved.get(1);
		assertFalse(created.tTransient());
		assertEquals(NEW_ITEM, created.tValueByName(NAME));
	}

	/**
	 * A change made only within a part makes the form dirty and is stored with it.
	 */
	public void testChangeWithinPartIsSaved() {
		ReactCompactFieldControl cell = cell(ITEMS);

		open(cell);
		rowsOfTopDialog().currentRows().get(0).tUpdateByName(NAME, RENAMED);
		press(dialogActions().get(1));
		assertTrue(_form.isDirty());

		_form.executeSave();
		assertEquals(RENAMED, _oldItem.tValueByName(NAME));
	}

	/**
	 * OK keeps the dialog open while a part is not valid.
	 */
	public void testOkRefusedForInvalidPart() {
		ReactCompactFieldControl cell = cell(ITEMS);

		open(cell);
		TLObject added = rowsOfTopDialog().addRow(null);
		assertNotNull(added);
		press(dialogActions().get(1));
		assertEquals("A part without its mandatory name keeps the dialog open.", 1, _dialogs._open.size());

		added.tUpdateByName(NAME, NEW_ITEM);
		press(dialogActions().get(1));
		assertTrue(_dialogs._open.isEmpty());
	}

	/**
	 * The parts of a composition the user may not write are displayed read-only, in a dialog that
	 * only offers to close it.
	 */
	public void testReadOnlyComposition() {
		ReactCompactFieldControl cell = cell(LOCKED_ITEMS);
		assertFalse(cell.getFieldModel().isEditable());

		open(cell);
		assertEquals(1, dialogActions().size());
		assertNull(rowsOfTopDialog().addRow(null));

		press(dialogActions().get(0));
		assertTrue(_dialogs._open.isEmpty());
		assertFalse(_form.isDirty());
	}

	/**
	 * The dialogs close when the form leaves edit mode.
	 */
	public void testLeavingEditModeClosesDialogs() {
		open(cell(SUBSTEPS));
		open(nestedCell());
		assertEquals(2, _dialogs._open.size());

		_form.executeCancel();

		assertTrue(_dialogs._open.isEmpty());
	}

	/**
	 * A composition of a part is edited in a nested dialog: its changes reach the outer dialog on
	 * OK, the row only when the outer dialog is confirmed as well, and the database only on save.
	 */
	public void testNestedDialogsConfirmed() {
		open(cell(SUBSTEPS));
		RowSetEditSession substeps = rowsOfTopDialog();
		TLObject substepBuffer = single(substeps.currentRows());

		open(nestedCell());
		assertNotNull(rowsOfTopDialog().addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		press(dialogActions().get(1));

		assertEquals(1, _dialogs._open.size());
		assertEquals(1, ((List<?>) substepBuffer.tValueByName(SUBSTEPS)).size());
		assertEquals(List.of(), _substep.tValueByName(SUBSTEPS));

		press(dialogActions().get(1));
		assertTrue(_dialogs._open.isEmpty());
		assertTrue(_form.isDirty());
		assertEquals(List.of(), _substep.tValueByName(SUBSTEPS));

		_form.executeSave();

		assertEquals(List.of(_substep), _step.tValueByName(SUBSTEPS));
		TLObject created = singleObject(_substep.tValueByName(SUBSTEPS));
		assertFalse(created.tTransient());
		assertEquals(NEW_SUBSTEP, created.tValueByName(NAME));
	}

	/**
	 * Cancelling the outer dialog drops what a nested dialog confirmed.
	 */
	public void testOuterCancelDropsNestedOk() {
		open(cell(SUBSTEPS));
		open(nestedCell());
		assertNotNull(rowsOfTopDialog().addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		press(dialogActions().get(1));

		press(dialogActions().get(0));

		assertTrue(_dialogs._open.isEmpty());
		assertEquals(List.of(_substep), _stepRow.tValueByName(SUBSTEPS));
		assertFalse(_form.isDirty());
		_form.executeSave();
		assertEquals(List.of(), _substep.tValueByName(SUBSTEPS));
	}

	/**
	 * Cancelling a nested dialog leaves the outer dialog's part as it was; confirming the outer
	 * dialog then changes nothing.
	 */
	public void testNestedCancelThenOuterOk() {
		ReactCompactFieldControl cell = cell(SUBSTEPS);
		open(cell);
		open(nestedCell());
		assertNotNull(rowsOfTopDialog().addRow(row -> row.tUpdateByName(NAME, NEW_SUBSTEP)));
		press(dialogActions().get(0));

		press(dialogActions().get(1));

		assertTrue(_dialogs._open.isEmpty());
		assertEquals("An unchanged part stands for itself.", List.of(_substep), _stepRow.tValueByName(SUBSTEPS));
		assertFalse(_form.isDirty());
	}

	/**
	 * A part confirmed in a first dialog and removed in a second, cancelled one is still there.
	 */
	public void testRemoveThenCancelKeepsPart() {
		ReactCompactFieldControl cell = cell(ITEMS);

		open(cell);
		rowsOfTopDialog().currentRows().get(0).tUpdateByName(NAME, RENAMED);
		press(dialogActions().get(1));

		open(cell);
		RowSetEditSession parts = rowsOfTopDialog();
		parts.removeRow(parts.currentRows().get(0));
		assertTrue(parts.currentRows().isEmpty());
		press(dialogActions().get(0));

		TLObject item = singleObject(_stepRow.tValueByName(ITEMS));
		assertEquals(RENAMED, item.tValueByName(NAME));

		_form.executeSave();
		assertTrue(_oldItem.tValid());
		assertEquals(RENAMED, _oldItem.tValueByName(NAME));
	}

	/**
	 * Creates the cell of the given composition of the edited step, as the table of the steps
	 * creates it.
	 */
	private ReactCompactFieldControl cell(String composition) {
		return cell(_steps.session(), _stepRow, composition);
	}

	/**
	 * Creates the cell of the substeps of the substep shown in the top dialog.
	 */
	private ReactCompactFieldControl nestedCell() {
		RowSetEditSession substeps = rowsOfTopDialog();
		return cell(substeps, single(substeps.currentRows()), SUBSTEPS);
	}

	private ReactCompactFieldControl cell(RowSetEditSession session, TLObject row, String composition) {
		// Whether the column is offered at all is decided for the type, which is not under test.
		becomeUser(_root);
		ColumnSetup column = single(AttributeColumn.derived(type(STEP).getPartOrFail(composition))
			.resolve(new ColumnResolution(type(STEP), null)));
		becomeUser(_responsible);
		BoundFieldModel model = session.cellModel(row, column);
		assertNotNull(model);
		ReactControl control = column.editing().createControl(_context, row, model);
		assertTrue(control instanceof ReactCompactFieldControl);
		return (ReactCompactFieldControl) control;
	}

	/**
	 * The session of the parts displayed in the top dialog.
	 */
	private RowSetEditSession rowsOfTopDialog() {
		assertFalse("A dialog is open", _dialogs._open.isEmpty());
		return single(descendants(_dialogs._open.peek(), RowSetTableControl.class)).session();
	}

	private void open(ReactCompactFieldControl cell) {
		press(descendants(cell, ReactButtonControl.class).get(0));
	}

	private static void press(ReactButtonControl button) {
		button.executeCommand(CLICK, Map.of());
	}

	/**
	 * The buttons of the top dialog itself, without the ones of the table it displays.
	 */
	private List<ReactButtonControl> dialogActions() {
		assertFalse("A dialog is open", _dialogs._open.isEmpty());
		List<ReactButtonControl> result = new ArrayList<>();
		collectActions(_dialogs._open.peek(), result);
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

	private static TLObject singleObject(Object value) {
		return (TLObject) single((List<?>) value);
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
	 * A {@link DialogManager} holding a stack of open dialogs.
	 */
	private static final class RecordingDialogManager implements DialogManager {

		final Deque<ReactControl> _open = new ArrayDeque<>();

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_open.push(child);
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_open.pop();
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			// Not used.
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
