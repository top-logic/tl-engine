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
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a {@link FormControl} in edit mode follows a change stored to its object by someone
 * else than the form, e.g. by a command run while the form is being edited.
 *
 * <p>
 * A field the user has left alone shows the stored value and stays unchanged; a field the user has
 * changed keeps the user's value, which the next save writes. The change is delivered by the
 * {@link ModelScope} the form observes its object in, as the scope of a browser window delivers it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFormFollowsStoredChanges extends AbstractTicketFormTest {

	/**
	 * A stored change shows in a field the user has left alone, while the field the user has
	 * changed keeps the user's value, which the save writes over the stored one.
	 */
	public void testUntouchedFieldsFollowStoredChanges() {
		_form.enterEditMode();
		model(_title).setValue("Login fails on Mondays");

		storeChange("Login broken", "closed");

		assertEquals("A field the user has left alone shows the stored value.",
			"closed", shown(_status));
		assertFalse("A field showing the stored value holds no change of the user.", _status.isDirty());
		assertEquals("A field the user has changed keeps the user's value.",
			"Login fails on Mondays", shown(_title));
		assertTrue("The user's change is still to be saved.", _title.isDirty());
		assertTrue(_form.isDirty());

		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertEquals("The save writes the user's value.",
			"Login fails on Mondays", _ticket.tValueByName(TITLE));
		assertEquals("The save keeps the stored value of the field the user has left alone.",
			"closed", _ticket.tValueByName(STATUS));
	}

	/**
	 * A field the user has changed and changed back holds no change, so it follows a stored
	 * change, and the save does not write the value the field started with over it.
	 */
	public void testRevertedFieldFollowsStoredChanges() {
		_form.enterEditMode();
		model(_status).setValue("in progress");
		model(_status).setValue("open");
		assertFalse("Changing a field back leaves it unchanged.", _status.isDirty());

		storeChange("Login fails", "closed");

		assertEquals("closed", shown(_status));
		assertFalse(_status.isDirty());
		assertFalse("Nothing is left to save.", _form.isDirty());

		_form.executeSave();

		assertEquals("The save does not write the value the field started with.",
			"closed", _ticket.tValueByName(STATUS));
	}

	/**
	 * A field whose input was rejected holds text the user typed, which a stored change must not
	 * replace.
	 */
	public void testFieldWithRejectedInputKeepsIt() {
		_form.enterEditMode();
		ResKey inputError = ResKey.text("Not a status.");
		model(_status).setError(inputError);

		storeChange("Login fails", "closed");

		assertEquals("A field showing a rejected input keeps it.", "open", shown(_status));
		assertEquals(inputError, model(_status).getInputError());
	}

	/**
	 * In view mode, the fields show the stored values.
	 */
	public void testViewModeShowsStoredChanges() {
		storeChange("Login broken", "closed");

		assertEquals("Login broken", shown(_title));
		assertEquals("closed", shown(_status));
		assertFalse(_title.isDirty());
		assertFalse(_status.isDirty());
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormFollowsStoredChanges.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
