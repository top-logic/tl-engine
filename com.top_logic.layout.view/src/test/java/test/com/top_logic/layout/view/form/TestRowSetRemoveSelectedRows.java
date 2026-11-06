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
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.QueryRowSetBinding;
import com.top_logic.layout.view.form.RowEditPolicy;
import com.top_logic.layout.view.form.RowSetBinding.RemoveMode;
import com.top_logic.layout.view.form.RowSetTableControl;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.table.SelectionMode;

/**
 * Tests the {@link RowSetTableControl#getRemoveSelectedRowsCommand() removal of the selected rows}
 * of a row-editing table: offered in edit mode of a table selecting several rows and removing
 * rows, executable while rows are selected, and removing all of them at once.
 */
public class TestRowSetRemoveSelectedRows extends AbstractModelAccessTest {

	private ReactContext _reactContext;

	private TLObject _first;

	private TLObject _second;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_first = newStep("first");
			_second = newStep("second");
			_project.tUpdateByName(STEPS, List.of(_first, _second));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_first = null;
		_second = null;
		super.tearDown();
	}

	/**
	 * The command is offered in edit mode only, and executable while rows are selected; executed,
	 * it removes all selected rows, which the save deletes.
	 */
	public void testRemoveSelectedRows() {
		becomeUser(_root);
		FormControl form = newForm();
		RowSetTableControl table = table(form, RemoveMode.DELETE, SelectionMode.MULTI);
		CommandModel command = table.getRemoveSelectedRowsCommand();
		assertFalse("Not offered in view mode.", command.isVisible());

		assertTrue(form.enterEditMode());
		assertTrue(command.isVisible());
		assertFalse("Nothing selected.", command.isExecutable());

		List<TLObject> rows = table.session().currentRows();
		assertEquals(2, rows.size());
		table.getTableControl().selectRows(rows);
		assertTrue(command.isExecutable());

		command.executeCommand(_reactContext);
		assertTrue(table.session().currentRows().isEmpty());

		form.executeSave();
		assertFalse(_first.tValid());
		assertFalse(_second.tValid());
	}

	/**
	 * A table selecting a single row or removing no rows does not offer the command.
	 */
	public void testNotOffered() {
		becomeUser(_root);
		FormControl singleForm = newForm();
		RowSetTableControl single = table(singleForm, RemoveMode.DELETE, SelectionMode.SINGLE);
		assertTrue(singleForm.enterEditMode());
		assertFalse(single.getRemoveSelectedRowsCommand().isVisible());

		FormControl keepingForm = newForm();
		RowSetTableControl keeping = table(keepingForm, RemoveMode.NONE, SelectionMode.MULTI);
		assertTrue(keepingForm.enterEditMode());
		assertFalse(keeping.getRemoveSelectedRowsCommand().isVisible());
	}

	private RowSetTableControl table(FormControl form, RemoveMode removeMode, SelectionMode selectionMode) {
		RowSetTableControl table = new RowSetTableControl(new DefaultViewContext(_reactContext), form,
			new QueryRowSetBinding(this::existingSteps, type(STEP), List.of(), removeMode), List.of(),
			RowEditPolicy.ALL);
		table.setSelectionMode(selectionMode);
		table.init();
		return table;
	}

	/**
	 * The rows of the table: the steps not deleted, as a query reads them.
	 */
	private List<TLObject> existingSteps() {
		return List.of(_first, _second).stream().filter(TLObject::tValid).toList();
	}

	private static TLObject newStep(String name) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(STEP));
		result.tUpdateByName(NAME, name);
		return result;
	}

	private FormControl newForm() {
		return new FormControl(_reactContext, _project, "no model", NoTokenHandling.INSTANCE);
	}

	/**
	 * The test suite, starting the theme and resources the table needs for its icons and the
	 * services building its data columns and their cells.
	 */
	public static Test suite() {
		return suiteWith(TestRowSetRemoveSelectedRows.class, ThemeFactory.Module.INSTANCE,
			ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			ColumnProviderService.Module.INSTANCE, LabelProviderService.Module.INSTANCE,
			SearchBuilder.Module.INSTANCE);
	}

}
