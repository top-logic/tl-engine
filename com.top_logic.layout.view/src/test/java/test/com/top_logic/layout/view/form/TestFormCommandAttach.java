/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the built-in commands of a form (Edit, Save, Cancel) show the current state of the
 * form when it is displayed, also after the form changed its mode while the commands did not
 * observe it.
 *
 * <p>
 * The commands are wired to the form as {@link FormElement} does: they are created while the form
 * is built and observe it from its attach on.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFormCommandAttach extends AbstractTicketFormTest {

	/** Name of the channel holding the edit mode of the form. */
	private static final String EDIT_MODE = "editMode";

	private FormCommandModel _edit;

	private FormCommandModel _save;

	private FormCommandModel _cancel;

	private int[] _notifications;

	@Override
	protected void tearDown() throws Exception {
		_edit = null;
		_save = null;
		_cancel = null;
		_notifications = null;
		super.tearDown();
	}

	/**
	 * A form whose edit-mode channel holds {@code true} when it is built enters edit mode before
	 * it is displayed: displayed, it shows Save and Cancel, not Edit.
	 */
	public void testEditModeChannelTrueFromStart() {
		FormControl form = new FormControl(_context, _ticket, NO_MODEL_MESSAGE, NoTokenHandling.INSTANCE);
		ViewChannel editMode = new DefaultViewChannel(EDIT_MODE);
		editMode.set(Boolean.TRUE);
		form.setModelScope(_scope);
		form.setEditModeChannel(editMode);
		createCommands(form);
		assertViewMode("The commands are created while the form is still in view mode.");

		form.enterEditMode();
		assertTrue(form.isEditMode());

		form.attach();

		assertEditMode("The displayed form is in edit mode.");
		assertEquals("Each command whose state changed notifies once.", List.of(1, 1, 1), notifications());
	}

	/**
	 * A form changing its mode while hidden shows the new mode in its commands when displayed
	 * again.
	 */
	public void testModeChangeWhileHidden() {
		createCommands(_form);
		assertViewMode("The displayed form is in view mode.");

		_form.detach();
		_form.enterEditMode();
		assertViewMode("Hidden commands do not follow the form.");
		assertEquals(List.of(0, 0, 0), notifications());

		_form.attach();
		assertEditMode("The form entered edit mode while hidden.");
		assertEquals("Each command whose state changed notifies once.", List.of(1, 1, 1), notifications());

		_form.detach();
		_form.executeCancel();
		_form.attach();
		assertViewMode("The form left edit mode while hidden.");
		assertEquals(List.of(2, 2, 2), notifications());
	}

	/**
	 * A form displayed again without a state change does not notify about its commands.
	 */
	public void testNoChangeNoNotification() {
		createCommands(_form);

		_form.detach();
		_form.attach();

		assertViewMode("The form did not change.");
		assertEquals("Commands without a state change do not notify.", List.of(0, 0, 0), notifications());
	}

	/**
	 * Creates the Edit, Save and Cancel commands of the given form and registers them for the
	 * form's attach and detach, as {@link FormElement} does. Commands of a form displayed already
	 * are attached right away.
	 */
	private void createCommands(FormControl form) {
		_edit = FormCommandModel.editCommand(form);
		_save = FormCommandModel.saveCommand(form);
		_cancel = FormCommandModel.cancelCommand(form);
		List<FormCommandModel> commands = List.of(_edit, _save, _cancel);

		_notifications = new int[commands.size()];
		for (int n = 0; n < commands.size(); n++) {
			int index = n;
			commands.get(n).addStateChangeListener(() -> _notifications[index]++);
		}

		form.addAttachListener(() -> commands.forEach(FormCommandModel::attach));
		form.addDetachListener(() -> commands.forEach(FormCommandModel::detach));
	}

	private List<Integer> notifications() {
		return List.of(_notifications[0], _notifications[1], _notifications[2]);
	}

	private void assertViewMode(String message) {
		assertTrue(message, _edit.isVisible());
		assertTrue(message, _edit.isExecutable());
		assertFalse(message, _save.isVisible());
		assertFalse(message, _save.isExecutable());
		assertFalse(message, _cancel.isVisible());
		assertFalse(message, _cancel.isExecutable());
	}

	private void assertEditMode(String message) {
		assertFalse(message, _edit.isVisible());
		assertFalse(message, _edit.isExecutable());
		assertTrue(message, _save.isVisible());
		assertTrue(message, _save.isExecutable());
		assertTrue(message, _cancel.isVisible());
		assertTrue(message, _cancel.isExecutable());
	}

	/**
	 * Test suite requiring a knowledge base, and the {@link FieldControlService} building the
	 * inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormCommandAttach.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
