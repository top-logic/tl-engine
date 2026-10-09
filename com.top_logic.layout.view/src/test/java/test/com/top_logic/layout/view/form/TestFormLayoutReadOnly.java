/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.util.model.ModelService;

/**
 * Test that a {@link FormControl} publishes its grid as read-only
 * ({@link ReactFormLayoutControl#READ_ONLY}) exactly while it is not in edit mode, so that the chrome
 * of its fields omits the required marker, the messages and the help in view mode.
 */
@SuppressWarnings("javadoc")
public class TestFormLayoutReadOnly extends AbstractTicketFormTest {

	public void testReadOnlyInViewMode() {
		assertEquals(Boolean.TRUE, readOnly());
	}

	public void testEditableInEditMode() {
		assertTrue(_form.enterEditMode());
		assertEquals(Boolean.FALSE, readOnly());
	}

	public void testReadOnlyAgainAfterCancel() {
		assertTrue(_form.enterEditMode());
		_form.executeCancel();

		assertFalse(_form.isEditMode());
		assertEquals(Boolean.TRUE, readOnly());
	}

	public void testReadOnlyAgainAfterSave() {
		assertTrue(_form.enterEditMode());
		model(_title).setValue("Login works");
		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertEquals(Boolean.TRUE, readOnly());
	}

	private Object readOnly() {
		return _form.scriptingScalarState().get(ReactFormLayoutControl.READ_ONLY);
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormLayoutReadOnly.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
