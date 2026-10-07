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

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormModel;
import com.top_logic.layout.view.form.FormModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a {@link FormControl} displayed again after it was {@link FormControl#detach()
 * detached} (a hidden sidebar section or tab) shows its object as stored, although it did not
 * observe the object in its {@link ModelScope} while hidden.
 */
@SuppressWarnings("javadoc")
public class TestFormAttachCatchUp extends AbstractTicketFormTest {

	/** State key of the message the form shows when it displays no object. */
	private static final String NO_MODEL_MESSAGE_STATE = "noModelMessage";

	private RecordingLockHandler _lockHandler;

	@Override
	protected LockHandler lockHandler() {
		_lockHandler = new RecordingLockHandler();
		return _lockHandler;
	}

	@Override
	protected void tearDown() throws Exception {
		_lockHandler = null;
		super.tearDown();
	}

	/**
	 * In view mode, a form displayed again shows the values stored while it was hidden.
	 */
	public void testViewModeCatchesUp() {
		_form.detach();
		storeChange("Login broken", "closed");

		assertEquals("A hidden form does not follow stored changes.", "Login fails", shown(_title));
		assertEquals("A hidden form does not follow stored changes.", "open", shown(_status));

		_form.attach();

		assertEquals("Login broken", shown(_title));
		assertEquals("closed", shown(_status));
		assertFalse(_form.isDirty());
	}

	/**
	 * In edit mode, a form displayed again shows the stored values where the user has not changed
	 * anything, and keeps the user's changes, which the save writes.
	 */
	public void testEditModeCatchesUp() {
		_form.enterEditMode();
		model(_title).setValue("Login fails on Mondays");

		_form.detach();
		storeChange("Login broken", "closed");
		_form.attach();

		assertTrue(_form.isEditMode());
		assertEquals("A field the user has left alone shows the stored value.", "closed", shown(_status));
		assertFalse(_status.isDirty());
		assertEquals("A field the user has changed keeps the user's value.",
			"Login fails on Mondays", shown(_title));
		assertTrue(_title.isDirty());

		_form.executeSave();

		assertFalse(_form.isEditMode());
		assertEquals("Login fails on Mondays", _ticket.tValueByName(TITLE));
		assertEquals("closed", _ticket.tValueByName(STATUS));
	}

	/**
	 * A form whose object was deleted while it was hidden displays nothing when shown again, and
	 * ends its edit session.
	 */
	public void testDeletedWhileHidden() {
		_form.enterEditMode();
		assertTrue(_lockHandler.hasLock());

		_form.detach();
		_ticket.delete();
		_form.attach();

		assertNull("The form displays nothing after its object was deleted.", _form.getCurrentObject());
		assertEquals(NO_MODEL_MESSAGE, _form.scriptingScalarState().get(NO_MODEL_MESSAGE_STATE));
		assertFalse("The edit session of the deleted object has ended.", _form.isEditMode());
		assertFalse("The lock of the deleted object is released.", _lockHandler.hasLock());
		assertEquals("The form no longer observes the deleted object.", List.of(), _scope.listeners(_ticket));
	}

	/**
	 * The first attach of a form does not notify its listeners: the fields were just built from the
	 * object.
	 */
	public void testFirstAttachIsSilent() {
		FormControl form = new FormControl(_context, _ticket, NO_MODEL_MESSAGE, new RecordingLockHandler());
		form.setModelScope(_scope);
		Counter counter = new Counter();
		form.addFormModelListener(counter);

		form.attach();

		assertEquals("The first attach does not notify.", 0, counter._stateChanges);
		assertEquals(List.of(_form, form), _scope.listeners(_ticket));
	}

	/**
	 * A form displayed again after a hide notifies its listeners even without a stored change: what
	 * happened while hidden is unknown.
	 */
	public void testResumeNotifies() {
		Counter counter = new Counter();
		_form.addFormModelListener(counter);

		_form.detach();
		assertEquals("A hide does not notify.", 0, counter._stateChanges);

		_form.attach();
		assertEquals("A form displayed again notifies once.", 1, counter._stateChanges);
	}

	/**
	 * A hidden form whose object is switched by its input channel does not observe the object until
	 * it is displayed again, and then observes it exactly once.
	 */
	public void testObjectSwitchWhileHidden() {
		ViewChannel input = new DefaultViewChannel("input");
		input.set(_ticket);
		_form.setInputChannel(input);
		IdentifiedObject other = newTicket("Logout fails", "open");

		_form.detach();
		assertEquals("A hidden form observes nothing.", List.of(), _scope.listeners(_ticket));

		input.set(other);

		assertSame(other, _form.getCurrentObject());
		assertEquals("A hidden form observes nothing.", List.of(), _scope.listeners(other));
		assertEquals(List.of(), _scope.listeners(_ticket));

		_form.attach();

		assertEquals("A form displayed again observes its object exactly once.",
			List.of(_form), _scope.listeners(other));
		assertEquals(List.of(), _scope.listeners(_ticket));
		assertEquals("Logout fails", shown(_title));
	}

	/**
	 * {@link FormModelListener} counting the notifications of form state changes.
	 */
	private static class Counter implements FormModelListener {

		int _stateChanges;

		@Override
		public void onFormStateChanged(FormModel source) {
			_stateChanges++;
		}
	}

	/**
	 * {@link LockHandler} that grants every lock and records whether it is held.
	 */
	private static class RecordingLockHandler implements LockHandler {

		private boolean _locked;

		@Override
		public boolean hasLock() {
			return _locked;
		}

		@Override
		public Lock getLock() {
			return null;
		}

		@Override
		public void acquireLock(Object model) {
			_locked = true;
		}

		@Override
		public void releaseLock() {
			_locked = false;
		}

		@Override
		public void updateLock() {
			// The lock does not expire.
		}
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormAttachCatchUp.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
