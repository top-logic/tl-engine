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
import com.top_logic.element.model.copy.I18NConstants;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AbstractCompositionControl;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;

/**
 * Tests that saving a form checks the right to create the rows added to a composition of the form
 * object before anything is written.
 */
public class TestCompositionCreateRights extends AbstractModelAccessTest {

	/**
	 * A row added by a user who may not create it in the form object is refused on save, and
	 * nothing is written.
	 */
	public void testRefusedCreationKeepsEverything() {
		becomeUser(_responsible);
		FormControl form = newForm();
		CompositionControl tasks = new CompositionControl(form);
		tasks.init();
		form.enterEditMode();
		assertNotNull(tasks.addRow(row -> row.tUpdateByName(NAME, "new task")));

		becomeUser(_roleless);
		assertRefused(I18NConstants.ERROR_PERSIST_PERMISSION_DENIED__TYPE, form::executeSave);

		assertEquals(List.of(_task), _project.tValueByName(TASKS));
		assertTrue(form.isEditMode());
	}

	/**
	 * A row added by a user who may create it in the form object is created on save.
	 */
	public void testAllowedCreation() {
		becomeUser(_responsible);
		FormControl form = newForm();
		CompositionControl tasks = new CompositionControl(form);
		tasks.init();
		form.enterEditMode();
		assertNotNull(tasks.addRow(row -> row.tUpdateByName(NAME, "new task")));

		form.executeSave();

		List<?> rows = (List<?>) _project.tValueByName(TASKS);
		assertEquals(2, rows.size());
		TLObject created = (TLObject) rows.get(1);
		assertFalse(created.tTransient());
		assertEquals("new task", created.tValueByName(NAME));
		assertFalse(form.isEditMode());
	}

	private FormControl newForm() {
		return new FormControl(reactContext(), _project, "no model", NoTokenHandling.INSTANCE);
	}

	private static ReactContext reactContext() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	/**
	 * {@link AbstractCompositionControl} over the tasks of the form object, without a presentation.
	 */
	private static class CompositionControl extends AbstractCompositionControl {

		CompositionControl(FormControl form) {
			super(reactContext(), form, TASKS, "TLPanel");
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
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestCompositionCreateRights.class);
	}

}
