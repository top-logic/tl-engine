/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.table.CellControlFactory;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.AttributeRowSetBinding;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.CellContent;
import com.top_logic.table.Row;
import com.top_logic.table.TableView;

/**
 * Tests that a {@link RowSetTableControl} over a composition of the form object follows the
 * display mode of the composition: its static visibility, its create visibility, a dynamic
 * visibility, and the access rights of the user.
 */
@SuppressWarnings("javadoc")
public class TestCompositionTableVisibility extends AbstractModelAccessTest {

	/** Name of the {@link #PROJECT} composition hidden by its visibility. */
	private static final String HIDDEN_MILESTONES = "hiddenMilestones";

	/** Name of the {@link #PROJECT} composition that is read-only by its visibility. */
	private static final String FIXED_MILESTONES = "fixedMilestones";

	/** Name of the {@link #PROJECT} composition hidden on creation. */
	private static final String LATER_MILESTONES = "laterMilestones";

	/**
	 * Name of the {@link #PROJECT} composition hidden by its dynamic visibility while the project is
	 * not {@link #APPROVED}.
	 */
	private static final String APPROVED_MILESTONES = "approvedMilestones";

	/** Name of the row added in the tests. */
	private static final String NEW_ROW = "new milestone";

	private ReactContext _reactContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		super.tearDown();
	}

	/**
	 * A composition hidden by its visibility hides the table, which displays no rows, and offers
	 * no edit.
	 */
	public void testStaticallyHidden() {
		newPart(_project, HIDDEN_MILESTONES, MILESTONE);
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		RowSetTableControl table = table(form, HIDDEN_MILESTONES);

		assertHiddenWithoutRows(table);

		assertTrue(form.enterEditMode());
		assertHiddenWithoutRows(table);
		assertNull("No session for a hidden composition.", table.session());
		assertNoRowCreation(table);
	}

	/**
	 * A composition that is read-only by its visibility is displayed, but not edited when the form
	 * is edited; a composition without annotation is edited.
	 */
	public void testStaticallyReadOnly() {
		newPart(_project, FIXED_MILESTONES, MILESTONE);
		newPart(_project, MILESTONES, MILESTONE);
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		RowSetTableControl fixed = table(form, FIXED_MILESTONES);
		RowSetTableControl plain = table(form, MILESTONES);

		assertFalse(fixed.isHidden());
		assertEquals(1, rowCount(fixed));

		assertTrue(form.enterEditMode());
		assertViewOnly(fixed);
		assertEditable(plain);
	}

	/**
	 * A composition with a dynamic visibility follows the field its mode depends on while the form
	 * is edited, and a row added while the table was shown is saved after the table was hidden
	 * again.
	 */
	public void testDynamicVisibility() {
		newPart(_project, APPROVED_MILESTONES, MILESTONE);
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		AttributeFieldControl approved = field(form, APPROVED);
		RowSetTableControl table = table(form, APPROVED_MILESTONES);

		assertTrue("Hidden while not approved.", table.isHidden());

		assertTrue(form.enterEditMode());
		assertTrue(table.isHidden());
		assertNoRowCreation(table);

		setValue(approved, Boolean.TRUE);
		assertEditable(table);
		assertNotNull(table.addRow(row -> row.tUpdateByName(NAME, NEW_ROW)));
		assertEquals(2, rowCount(table));

		setValue(approved, Boolean.FALSE);
		assertTrue(table.isHidden());
		assertNoRowCreation(table);
		assertTrue("The session keeps running.", table.session().isRunning());
		assertEquals(2, table.session().currentRows().size());

		form.executeSave();

		assertFalse(form.isEditMode());
		assertTrue(table.isHidden());
		List<?> saved = (List<?>) _project.tValueByName(APPROVED_MILESTONES);
		assertEquals(2, saved.size());
		TLObject created = (TLObject) saved.get(1);
		assertFalse(created.tTransient());
		assertEquals(NEW_ROW, created.tValueByName(NAME));
	}

	/**
	 * A dynamic visibility that shows the composition in view mode shows the table in view mode,
	 * and the table follows the mode when the form enters edit mode.
	 */
	public void testDynamicVisibilityInViewMode() {
		newPart(_project, APPROVED_MILESTONES, MILESTONE);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(APPROVED, Boolean.TRUE);
			tx.commit();
		}
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		AttributeFieldControl approved = field(form, APPROVED);
		RowSetTableControl table = table(form, APPROVED_MILESTONES);

		assertFalse(table.isHidden());
		assertEquals(1, rowCount(table));

		assertTrue(form.enterEditMode());
		assertEditable(table);

		setValue(approved, Boolean.FALSE);
		assertTrue(table.isHidden());

		form.executeCancel();
		assertFalse("The stored value shows the table again.", table.isHidden());
	}

	/**
	 * On a new object, a composition hidden on creation hides the table; on a stored object it is
	 * shown.
	 */
	public void testCreateVisibility() {
		becomeUser(_responsible);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(PROJECT), null);
		FormControl newForm = newForm(draft);
		RowSetTableControl later = table(newForm, LATER_MILESTONES);
		RowSetTableControl plain = table(newForm, MILESTONES);
		assertTrue(newForm.enterEditMode());

		assertHiddenWithoutRows(later);
		assertNull(later.session());
		assertFalse(plain.isHidden());
		assertFalse(plain.createTypes().isEmpty());

		newPart(_project, LATER_MILESTONES, MILESTONE);
		FormControl storedForm = newForm(_project);
		RowSetTableControl stored = table(storedForm, LATER_MILESTONES);
		assertTrue(storedForm.enterEditMode());
		assertEditable(stored);
	}

	/**
	 * A user who may not read the composition does not see the table, nor its rows.
	 */
	public void testReadRefused() {
		newPart(_project, MILESTONES, MILESTONE);
		becomeUser(_roleless);
		FormControl form = newForm(_project);
		RowSetTableControl table = table(form, MILESTONES);

		assertHiddenWithoutRows(table);
	}

	/**
	 * A composition the user may not write is displayed view only while the form is edited; a
	 * composition of the same object the user may write is edited.
	 */
	public void testWriteRefused() {
		TLObject step = newPart(_project, STEPS, STEP);
		newPart(step, LOCKED_ITEMS, ITEM);
		newPart(step, ITEMS, ITEM);
		becomeUser(_responsible);
		FormControl form = newForm(step);
		RowSetTableControl locked = table(form, LOCKED_ITEMS);
		RowSetTableControl items = table(form, ITEMS);

		assertTrue(form.enterEditMode());
		assertViewOnly(locked);
		assertEditable(items);
	}

	/**
	 * A form switching to an object whose type does not declare the composition, or to no object,
	 * displays none of the rows of the object it left.
	 */
	public void testUnresolvedBindingShowsNoRows() {
		newPart(_project, MILESTONES, MILESTONE);
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		ViewChannel input = new DefaultViewChannel("input");
		input.set(_project);
		form.setInputChannel(input);
		RowSetTableControl table = table(form, MILESTONES);
		assertEquals(1, rowCount(table));

		input.set(_category);
		assertEquals("No rows of the project left.", 0, rowCount(table));

		input.set(_project);
		assertEquals(1, rowCount(table));

		input.set(null);
		assertEquals("No rows without an object.", 0, rowCount(table));
	}

	private void assertViewOnly(RowSetTableControl table) {
		assertFalse(table.isHidden());
		assertNotNull(table.session());
		assertFalse(table.session().isEditable());
		assertNoRowCreation(table);
		TLObject row = table.session().currentRows().get(0);
		assertFalse("Cell not editable.", isCellEditable(table, row));
		table.removeRow(row);
		assertEquals("No row removed.", 1, table.session().currentRows().size());
	}

	private void assertEditable(RowSetTableControl table) {
		assertFalse(table.isHidden());
		assertNotNull(table.session());
		assertTrue(table.session().isEditable());
		assertFalse(table.createTypes().isEmpty());
		assertTrue(table.getAddRowCommand().isVisible());
		assertTrue(table.getAddRowCommand().isExecutable());
		assertTrue("Cell editable.", isCellEditable(table, table.session().currentRows().get(0)));
	}

	private static void assertNoRowCreation(RowSetTableControl table) {
		assertTrue(table.createTypes().isEmpty());
		assertFalse(table.getAddRowCommand().isVisible());
		assertFalse(table.getAddRowCommand().isExecutable());
		assertNull(table.addRow());
	}

	private static void assertHiddenWithoutRows(RowSetTableControl table) {
		assertTrue(table.isHidden());
		assertEquals("No rows reach the client.", 0, rowCount(table));
	}

	private static int rowCount(RowSetTableControl table) {
		return table.getTableControl().getView().rowCount();
	}

	/**
	 * Whether the cell of the given row in the {@link #NAME} column is edited through an editable
	 * input.
	 */
	private boolean isCellEditable(RowSetTableControl table, TLObject rowObject) {
		TableView<TLObject> view = table.getTableControl().getView();
		for (Row<TLObject> row : view.rows(0, view.rowCount())) {
			if (row.key() == rowObject) {
				CellContent cell = view.cell(row, NAME);
				if (cell instanceof CellContent.Raw raw && raw.payload() instanceof CellControlFactory factory) {
					ReactControl control = factory.create(_reactContext);
					if (control instanceof ReactFormFieldChromeControl chrome) {
						control = chrome.getField();
					}
					return control instanceof ReactFormFieldControl input && input.getFieldModel().isEditable();
				}
				return false;
			}
		}
		throw new AssertionError("No row " + rowObject);
	}

	private FormControl newForm(TLObject object) {
		return new FormControl(_reactContext, object, "no model", NoTokenHandling.INSTANCE);
	}

	private RowSetTableControl table(FormControl form, String composition) {
		RowSetTableControl result = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new AttributeRowSetBinding(composition), List.of(), RowEditPolicy.ALL);
		result.init();
		return result;
	}

	private AttributeFieldControl field(FormControl form, String attribute) {
		AttributeFieldControl result = new AttributeFieldControl(_reactContext, form, form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	/**
	 * Enters the given value into the input of the given field, as the user does.
	 */
	private static void setValue(AttributeFieldControl field, Object value) {
		((AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel()).setValue(value);
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons, the services
	 * building its columns, cells and fields, and the scripting the dynamic visibility computes
	 * with.
	 */
	public static Test suite() {
		return suiteWith(TestCompositionTableVisibility.class, TypeIndex.Module.INSTANCE,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
