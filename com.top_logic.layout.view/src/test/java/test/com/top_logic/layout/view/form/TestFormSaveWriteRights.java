/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import static com.top_logic.model.search.expr.I18NConstants.*;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.FormParticipant;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Tests that saving a {@link FormControl} checks the write rights of all its changes before any of
 * them is written.
 */
public class TestFormSaveWriteRights extends AbstractModelAccessTest {

	/**
	 * A save refused for a change of the form's object leaves the changes of its participants
	 * unapplied, although they alone would have been allowed.
	 */
	public void testRefusedSaveKeepsParticipantChanges() {
		TLStructuredTypePart taskName = part(TASK, NAME);
		TLStructuredTypePart secret = part(PROJECT, SECRET);

		becomeUser(_responsible);
		FormControl form = newForm();
		form.enterEditMode();
		RowParticipant row = new RowParticipant(_task);
		form.registerParticipant(row);

		row.overlay().tUpdate(taskName, "changed");
		form.getOverlay().tUpdate(secret, "revealed");

		assertRefused(WRITE_PERMISSION_DENIED__OBJECT_ATTRIBUTE, form::executeSave);

		assertEquals("task", _task.tValue(taskName));
		assertNull(_project.tValue(secret));
		assertTrue("The row keeps its change.", row.overlay().isChanged(taskName));
		assertTrue("The form keeps its change.", form.getOverlay().isChanged(secret));
		assertTrue(form.isEditMode());
	}

	/**
	 * A save with the right to write all changes writes the changes of the form and its
	 * participants.
	 */
	public void testAllowedSaveWritesAllChanges() {
		TLStructuredTypePart taskName = part(TASK, NAME);
		TLStructuredTypePart name = part(PROJECT, NAME);

		becomeUser(_responsible);
		FormControl form = newForm();
		form.enterEditMode();
		RowParticipant row = new RowParticipant(_task);
		form.registerParticipant(row);

		row.overlay().tUpdate(taskName, "changed task");
		form.getOverlay().tUpdate(name, "changed project");

		form.executeSave();

		assertEquals("changed task", _task.tValue(taskName));
		assertEquals("changed project", _project.tValue(name));
		assertFalse(form.isEditMode());
	}

	private FormControl newForm() {
		return new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), _project, "no model", NoTokenHandling.INSTANCE);
	}

	/**
	 * {@link FormParticipant} buffering the edit of a row object of the form in an overlay, as a
	 * composition table does.
	 */
	private static class RowParticipant implements FormParticipant {

		private final TLObjectOverlay _overlay;

		RowParticipant(TLObject row) {
			_overlay = new TLObjectOverlay(row);
		}

		TLObjectOverlay overlay() {
			return _overlay;
		}

		@Override
		public boolean validate() {
			return true;
		}

		@Override
		public void checkApplyState() {
			_overlay.checkApply();
		}

		@Override
		public void applyState() {
			_overlay.apply();
		}

		@Override
		public void persist(Transaction tx) {
			// Nothing to persist, the row object exists.
		}

		@Override
		public void cancel() {
			_overlay.reset();
		}

		@Override
		public void revealAll() {
			// No validation errors to reveal.
		}

		@Override
		public boolean isDirty() {
			return _overlay.isDirty();
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestFormSaveWriteRights.class);
	}

}
