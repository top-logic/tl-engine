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
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.table.AttributeColumn;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnResolution;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.TLValueNormalizer;
import com.top_logic.util.TLContextManager;

/**
 * Tests that the cell of a row edited in a composition table brings an entered value into the
 * canonical form the {@link TLValueNormalizer} of the row's attribute computes.
 *
 * <p>
 * The {@link #LABEL} of a task is trimmed and converted to lower case.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestValueNormalizerCompositionCell extends AbstractModelAccessTest {

	/** Name of the normalized attribute of a task. */
	private static final String LABEL = "label";

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
	 * A value entered in the cell of a row is normalized before it reaches the row, and stored in
	 * its canonical form.
	 */
	public void testCellNormalizesEnteredValue() {
		TLObject row = single(_tasks.session().currentRows());
		BoundFieldModel cell = _tasks.session().cellModel(row, column(LABEL));
		assertTrue(cell instanceof AttributeFieldModel);

		cell.setValue("  Hello   World ");

		assertEquals("hello   world", cell.getValue());
		assertEquals("The row receives the canonical form.", "hello   world", row.tValueByName(LABEL));

		_form.executeSave();

		assertEquals("hello   world", _task.tValueByName(LABEL));
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
		return suiteWith(TestValueNormalizerCompositionCell.class, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE);
	}

}
