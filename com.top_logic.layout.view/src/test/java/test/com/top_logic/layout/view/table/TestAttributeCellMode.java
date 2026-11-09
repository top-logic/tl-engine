/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.AttributeModeTracker;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.CompositionCellModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowSetEditSession;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.AttributeCellEditing;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContextManager;

/**
 * Tests that a cell of a row edited in a table is edited only where the model lets its attribute
 * be edited on the row, as a form field is, see
 * {@link AttributeModeTracker#isStaticallyEditable(TLObject, com.top_logic.model.TLStructuredTypePart)}:
 * a derived attribute and an attribute of read-only visibility are displayed read-only, an
 * attribute editable on creation is edited on a new row only, and the parts of a composition of
 * read-only visibility are displayed but not changed in the dialog.
 *
 * <p>
 * The mode the dynamic visibility of an attribute computes from another value of the row follows
 * that value while the row is edited, see {@link AttributeModeTracker#computeMode(TLObject, boolean,
 * boolean)}.
 * </p>
 *
 * <p>
 * The cells are requested from the {@link RowSetEditSession} editing the steps of a project, the
 * way the row-edit table requests them, through the {@link AttributeCellEditing} of the column.
 * </p>
 */
public class TestAttributeCellMode extends AbstractModelAccessTest {

	/** Name of the derived {@link #STEP} attribute computed from its {@link #NAME}. */
	private static final String SUMMARY = "summary";

	/** Name of the {@link #STEP} attribute of read-only visibility, editable on creation. */
	private static final String KEY = "key";

	/** Name of the {@link #STEP} composition of read-only visibility. */
	private static final String FIXED_ITEMS = "fixedItems";

	/** Name of the {@link #STEP} attribute naming the mode of the {@link #REMARK}. */
	private static final String STATE = "state";

	/** Name of the {@link #STEP} attribute whose dynamic visibility is its {@link #STATE}. */
	private static final String REMARK = "remark";

	private TLObject _step;

	private FormControl _form;

	private StepsControl _steps;

	private TLObject _stepRow;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_step = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
			_step.tUpdateByName(NAME, "step");
			_project.tUpdateByName(STEPS, List.of(_step));
			tx.commit();
		}

		TLContextManager.inPersonContext(_responsible, () -> {
			_form = new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test")), _project, "no model", NoTokenHandling.INSTANCE);
			_steps = new StepsControl(_form);
			_steps.init();
			assertTrue(_form.enterEditMode());
			List<? extends TLObject> rows = _steps.session().currentRows();
			assertEquals(1, rows.size());
			_stepRow = rows.get(0);
		});
	}

	@Override
	protected void tearDown() throws Exception {
		_step = null;
		_form = null;
		_steps = null;
		_stepRow = null;
		super.tearDown();
	}

	@Override
	protected Person initialUser() {
		return _responsible;
	}

	/**
	 * A plain attribute the user may write is edited.
	 */
	public void testPlainAttributeEdited() {
		BoundFieldModel cell = cell(_stepRow, NAME);
		assertNotNull(cell);
		assertTrue(cell.isEditable());
		assertTrue(new AttributeCellEditing(NAME).canEdit(_step));
	}

	/**
	 * A derived attribute is displayed read-only, on the stored row and on the overlay it is
	 * edited in, although the user may write the row.
	 */
	public void testDerivedAttributeNotEdited() {
		AttributeCellEditing summary = new AttributeCellEditing(SUMMARY);
		assertFalse(summary.canEdit(_step));
		assertFalse(summary.canEdit(new TLObjectOverlay(_step)));
		assertNull(cell(_stepRow, SUMMARY));
	}

	/**
	 * An attribute of read-only visibility is displayed read-only, on a new row as well, since its
	 * create visibility is its visibility.
	 */
	public void testReadOnlyVisibilityNotEdited() {
		AttributeCellEditing code = new AttributeCellEditing(CODE);
		assertFalse(code.canEdit(_step));
		assertFalse(code.canEdit(new TLObjectOverlay(_step)));
		assertNull(cell(_stepRow, CODE));
		assertNull(cell(newRow(), CODE));
	}

	/**
	 * An attribute of read-only visibility that its create visibility makes editable is edited on a
	 * new row only.
	 */
	public void testCreateVisibility() {
		assertNull(cell(_stepRow, KEY));

		BoundFieldModel cell = cell(newRow(), KEY);
		assertNotNull(cell);
		assertTrue(cell.isEditable());
	}

	/**
	 * The parts of a composition of read-only visibility are displayed in the dialog, but not
	 * changed; the parts of a plain composition are.
	 */
	public void testReadOnlyComposition() {
		BoundFieldModel fixed = cell(_stepRow, FIXED_ITEMS);
		assertTrue(fixed instanceof CompositionCellModel);
		assertFalse(fixed.isEditable());

		BoundFieldModel items = cell(_stepRow, ITEMS);
		assertTrue(items instanceof CompositionCellModel);
		assertTrue(items.isEditable());
	}

	/**
	 * The mode the dynamic visibility of an attribute computes from another value of the row is
	 * applied to the cell, and applied again when that value changes in the row.
	 */
	public void testDynamicMode() {
		BoundFieldModel state = cell(_stepRow, STATE);
		BoundFieldModel remark = cell(_stepRow, REMARK);
		assertNotNull("Edited wherever the user may write it, the mode decides.", remark);
		assertTrue("No mode: the static visibility.", remark.isEditable());
		assertTrue(remark.isVisible());
		assertFalse(remark.isMandatory());

		state.setValue("read-only");
		assertFalse(remark.isEditable());
		assertTrue(remark.isVisible());

		state.setValue("mandatory");
		assertTrue(remark.isEditable());
		assertTrue(remark.isMandatory());

		state.setValue("disabled");
		assertFalse(remark.isEditable());
		assertTrue(remark.isDisabled());
		assertFalse(remark.isMandatory());

		state.setValue("hidden");
		assertFalse(remark.isVisible());
		assertFalse(remark.isEditable());

		state.setValue("editable");
		assertTrue(remark.isVisible());
		assertTrue(remark.isEditable());
		assertFalse(remark.isDisabled());
		assertFalse(remark.isMandatory());
	}

	/**
	 * An empty cell its mode makes mandatory is an error that refuses the rows, as long as the
	 * mode of its own row requires a value.
	 */
	public void testDynamicMandatoryValidated() {
		RowSetEditSession session = _steps.session();
		BoundFieldModel remark = cell(_stepRow, REMARK);
		// As after an attempt to save: the errors of the cells are shown.
		session.revealAll();
		assertFalse(remark.hasError());

		cell(_stepRow, STATE).setValue("mandatory");
		assertTrue("The mode requires a value.", remark.hasError());
		assertFalse(session.validate());

		cell(_stepRow, STATE).setValue("read-only");
		assertFalse("The mode requires no value.", remark.hasError());
		assertTrue(session.validate());

		TLObject other = newRow();
		BoundFieldModel otherRemark = cell(other, REMARK);
		session.revealAll();
		cell(other, STATE).setValue("mandatory");
		assertTrue(otherRemark.hasError());
		assertFalse("The mode of another row requires nothing of this one.", remark.hasError());

		otherRemark.setValue("entered");
		assertFalse(otherRemark.hasError());
		assertTrue(session.validate());
	}

	/**
	 * A hidden cell shows nothing: its control is hidden, and shown again with the value.
	 */
	public void testHiddenCellControl() {
		BoundFieldModel remark = cell(_stepRow, REMARK);
		ReactControl control = new AttributeCellEditing(REMARK).createControl(_form.getReactContext(), _stepRow,
			remark);
		assertFalse(control.isHidden());

		cell(_stepRow, STATE).setValue("hidden");
		assertTrue(control.isHidden());

		cell(_stepRow, STATE).setValue(null);
		assertFalse(control.isHidden());
	}

	/**
	 * The mode of a cell follows the values of its own row only.
	 */
	public void testDynamicModeFollowsOwnRow() {
		BoundFieldModel remark = cell(_stepRow, REMARK);
		TLObject other = newRow();
		BoundFieldModel otherRemark = cell(other, REMARK);

		cell(other, STATE).setValue("read-only");
		assertFalse(otherRemark.isEditable());
		assertTrue("A change of another row leaves the cell as it is.", remark.isEditable());
	}

	/**
	 * A value written to the row by others than its cells - here directly to its buffer - changes
	 * the mode of the cell as well.
	 */
	public void testDynamicModeFollowsWrittenValue() {
		BoundFieldModel remark = cell(_stepRow, REMARK);

		_stepRow.tUpdateByName(STATE, "read-only");
		assertFalse(remark.isEditable());
	}

	/**
	 * A cell without a dynamic visibility keeps its mode when the value its neighbour depends on
	 * changes.
	 */
	public void testStaticModeUnchanged() {
		BoundFieldModel name = cell(_stepRow, NAME);
		cell(_stepRow, STATE).setValue("hidden");
		assertTrue(name.isEditable());
		assertTrue(name.isVisible());
	}

	/**
	 * A disposed cell no longer follows its row: the session editing the rows ended.
	 */
	public void testDisposedCellDetached() {
		BoundFieldModel remark = cell(_stepRow, REMARK);
		BoundFieldModel state = cell(_stepRow, STATE);
		_steps.session().end();

		state.setValue("hidden");
		assertTrue("The disposed cell is not updated anymore.", remark.isVisible());
	}

	private TLObject newRow() {
		TLObject result = _steps.addRow();
		assertNotNull(result);
		return result;
	}

	/**
	 * The model of the given cell of the given row, as the row-edit table requests it, or
	 * {@code null} if the cell is not edited.
	 */
	private BoundFieldModel cell(TLObject row, String attribute) {
		// Whether the column is offered at all is decided for the type, which is not under test.
		becomeUser(_root);
		ColumnSetup column = AttributeColumn.derived(type(STEP).getPartOrFail(attribute))
			.resolve(new ColumnResolution(type(STEP), null)).get(0);
		becomeUser(_responsible);
		return _steps.session().cellModel(row, column);
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
	 * The test suite, starting the services the cells are built with.
	 */
	public static Test suite() {
		return suiteWith(TestAttributeCellMode.class, ResourcesModule.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
