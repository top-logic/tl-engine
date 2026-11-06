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
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.AttributeValueListener;
import com.top_logic.model.annotate.TLValueListeners;
import com.top_logic.util.TLContextManager;

/**
 * Tests that the {@link AttributeValueListener}s annotated to an attribute of a row of a
 * composition table (see {@link TLValueListeners}) run for a change of the row, and that the cells
 * of the row follow the values they write.
 *
 * <p>
 * A change of the {@link #KIND} of a task writes its {@link #DETAIL}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestCompositionRowValueListeners extends AbstractModelAccessTest {

	/** Name of the task attribute whose change runs a listener. */
	private static final String KIND = "kind";

	/** Name of the task attribute the listener writes. */
	private static final String DETAIL = "detail";

	private FormControl _form;

	private TasksControl _tasks;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		TLContextManager.inPersonContext(_responsible, () -> {
			_form = new FormControl(context, _project, "no model", NoTokenHandling.INSTANCE);
			_tasks = new TasksControl(_form);
			_tasks.init();
			assertTrue(_form.enterEditMode());
		});
	}

	@Override
	protected Person initialUser() {
		return _responsible;
	}

	@Override
	protected void tearDown() throws Exception {
		_tasks = null;
		_form = null;
		super.tearDown();
	}

	/**
	 * Editing a cell of an existing row runs the listener of its attribute on the overlay of the
	 * row, and the cell of the written attribute shows the written value as change.
	 */
	public void testCellEditOfExistingRow() {
		TLObject row = single(_tasks.session().currentRows());
		assertTrue(row instanceof TLObjectOverlay);
		BoundFieldModel kind = _tasks.session().cellModel(row, column(KIND));
		BoundFieldModel detail = _tasks.session().cellModel(row, column(DETAIL));
		detail.setValue("entered");
		assertEquals("entered", row.tValueByName(DETAIL));

		kind.setValue("bug");

		assertEquals("for bug", row.tValueByName(DETAIL));
		assertEquals("The cell of the written attribute shows the value.", "for bug", detail.getValue());
		assertTrue(detail.isDirty());
		assertTrue(_form.isDirty());
		assertNull("Nothing is stored before the save.", _task.tValueByName(DETAIL));

		_form.executeSave();

		assertEquals("bug", _task.tValueByName(KIND));
		assertEquals("for bug", _task.tValueByName(DETAIL));
	}

	/**
	 * A new row is edited in an overlay over its new object as well: editing its cell runs the
	 * listener, the cell of the written attribute shows the value, and the save creates the row
	 * with both values.
	 */
	public void testCellEditOfNewRow() {
		TLObject row = _tasks.session().addRow(task -> task.tUpdateByName(NAME, "new task"));
		assertTrue(row instanceof TLObjectOverlay);
		assertTrue(((TLObjectOverlay) row).getBase().tTransient());
		assertEquals("new task", row.tValueByName(NAME));
		BoundFieldModel kind = _tasks.session().cellModel(row, column(KIND));
		BoundFieldModel detail = _tasks.session().cellModel(row, column(DETAIL));
		assertNull("Creating the row runs no listener.", detail.getValue());

		kind.setValue("bug");

		assertEquals("for bug", detail.getValue());
		assertTrue(detail.isDirty());
		assertTrue(_form.isDirty());

		_form.executeSave();

		List<?> tasks = (List<?>) _project.tValueByName(TASKS);
		assertEquals(2, tasks.size());
		TLObject created = (TLObject) tasks.get(1);
		assertFalse(created.tTransient());
		assertEquals("new task", created.tValueByName(NAME));
		assertEquals("bug", created.tValueByName(KIND));
		assertEquals("for bug", created.tValueByName(DETAIL));
	}

	/**
	 * A removed new row is not created, and an edit of the remaining rows runs their listeners
	 * only.
	 */
	public void testRemovedNewRow() {
		TLObject row = _tasks.session().addRow(null);
		_tasks.session().cellModel(row, column(KIND)).setValue("bug");
		_tasks.session().removeRow(row);

		TLObject existing = single(_tasks.session().currentRows());
		_tasks.session().cellModel(existing, column(KIND)).setValue("feature");
		assertEquals("for feature", existing.tValueByName(DETAIL));

		_form.executeSave();

		assertEquals(List.of(_task), _project.tValueByName(TASKS));
		assertEquals("for feature", _task.tValueByName(DETAIL));
	}

	/**
	 * The listener writes an attribute that has no cell.
	 */
	public void testAttributeWithoutCell() {
		TLObject row = single(_tasks.session().currentRows());
		BoundFieldModel kind = _tasks.session().cellModel(row, column(KIND));

		kind.setValue("feature");

		assertEquals("for feature", row.tValueByName(DETAIL));
		assertTrue(_form.isDirty());
	}

	/**
	 * Starting the edit of the rows runs no listener.
	 */
	public void testLoadRunsNoListener() {
		TLObject row = single(_tasks.session().currentRows());
		assertNull(row.tValueByName(DETAIL));
		assertFalse(_form.isDirty());
	}

	/**
	 * The column of the given attribute of a task, as the table of the tasks resolves it.
	 */
	private ColumnSetup column(String attribute) {
		// Whether the column is offered at all is decided for the type, which is not under test.
		becomeUser(_root);
		try {
			return single(AttributeColumn.derived(part(TASK, attribute))
				.resolve(new ColumnResolution(type(TASK), null)));
		} finally {
			becomeUser(_responsible);
		}
	}

	private static <T> T single(List<T> list) {
		assertEquals(1, list.size());
		return list.get(0);
	}

	/**
	 * {@link AbstractCompositionControl} over the tasks of the form object, without a presentation.
	 */
	private static class TasksControl extends AbstractCompositionControl {

		TasksControl(FormControl form) {
			super(form.getReactContext(), form, TASKS, "TLPanel");
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
	 * The test suite, starting the services creating the columns and the fields of the cells.
	 */
	public static Test suite() {
		return suiteWith(TestCompositionRowValueListeners.class, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
