/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Locale;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.form.FormCommandModel;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.security.ModelAccessPolicy;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Tests that editing a {@link FormControl} follows the right to write its object, see
 * {@link ModelAccessPolicy#onEdit(TLObject)}, combined with the configured edit rule.
 */
@SuppressWarnings("javadoc")
public class TestFormEditRights extends AbstractModelAccessTest {

	private static final ViewExecutabilityRule DENIED = input -> ExecutableState.NO_EXEC_PERMISSION;

	private static final ViewExecutabilityRule HIDDEN = input -> ExecutableState.NOT_EXEC_HIDDEN;

	/**
	 * A project the {@link #_roleless} user is responsible for; on {@link #_project} the same user
	 * is viewer only.
	 */
	private TLObject _own;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_own = newObject(qualified(PROJECT), "own");
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_project.tUpdateByName(VIEWER, _roleless);
			_own.tUpdateByName(RESPONSIBLE, _roleless);
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_own = null;
		super.tearDown();
	}

	/**
	 * A user with the right to write the object edits it.
	 */
	public void testWriteGranted() {
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		FormCommandModel edit = FormCommandModel.editCommand(form);

		assertSame(ExecutableState.EXECUTABLE, form.editPermission());
		assertTrue(edit.isExecutable());
		assertTrue(form.enterEditMode());
		assertTrue(form.isEditMode());
	}

	/**
	 * A user who may read but not write the object gets the Edit command disabled, giving the
	 * refusal; the form stays in view mode.
	 */
	public void testWriteRefusedOnObject() {
		becomeUser(_roleless);
		FormControl form = newForm(_project);
		FormCommandModel edit = FormCommandModel.editCommand(form);

		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_WRITE_DENIED, form.editPermission());
		assertTrue(edit.isVisible());
		assertFalse(edit.isExecutable());
		assertEquals(Resources.getInstance().getString(com.top_logic.layout.view.security.I18NConstants.ERROR_WRITE_DENIED),
			edit.getTooltip());
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_WRITE_DENIED, edit.getExecutableState());

		assertFalse(form.enterEditMode());
		assertFalse(form.isEditMode());
	}

	/**
	 * An edit-mode channel requesting edit mode on an object the user may not write is reset.
	 */
	public void testEditModeChannelRefused() {
		becomeUser(_roleless);
		FormControl form = newForm(_project);
		ViewChannel editMode = new DefaultViewChannel("editMode");
		editMode.set(Boolean.FALSE);
		form.setEditModeChannel(editMode);

		editMode.set(Boolean.TRUE);

		assertFalse(form.isEditMode());
		assertEquals(Boolean.FALSE, editMode.get());
	}

	/**
	 * An auto-edit form enters edit mode only for an object the user may write, also after an input
	 * switch; the Edit command follows the switch.
	 */
	public void testAutoEditFollowsInput() {
		becomeUser(_roleless);
		FormControl form = newForm(_project);
		ViewChannel input = new DefaultViewChannel("input");
		input.set(_project);
		form.setInputChannel(input);
		FormCommandModel edit = FormCommandModel.editCommand(form);
		edit.attach();
		form.setAutoEditMode(true);
		assertFalse("Initial edit mode is refused.", form.enterEditMode());
		assertFalse(form.isEditMode());
		assertFalse(edit.isExecutable());

		input.set(_own);
		assertTrue("The writable object is edited.", form.isEditMode());

		form.executeCancel();
		form.setAutoEditMode(false);
		assertTrue("The Edit command follows the input.", edit.isExecutable());

		input.set(_project);
		assertFalse(form.isEditMode());
		assertFalse("The Edit command follows the input.", edit.isExecutable());
		assertTrue(edit.isVisible());
	}

	/**
	 * A restricted user may never write: the Edit command is hidden.
	 */
	public void testRestrictedUserHidden() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_responsible.setRestrictedUser(Boolean.TRUE);
			tx.commit();
		}
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		FormCommandModel edit = FormCommandModel.editCommand(form);

		assertSame(ExecutableState.NOT_EXEC_HIDDEN, form.editPermission());
		assertFalse(edit.isVisible());
		assertFalse(form.enterEditMode());
	}

	/**
	 * A type granting writing to no role hides the Edit command.
	 */
	public void testTypeGrantsWriteToNoRole() {
		becomeUser(_responsible);
		FormControl form = newForm(_category);

		assertSame(ExecutableState.NOT_EXEC_HIDDEN, form.editPermission());
		assertFalse(FormCommandModel.editCommand(form).isVisible());
		assertFalse(form.enterEditMode());
	}

	/**
	 * The approval of writing the object decides as well and gives its reason.
	 */
	public void testApprovalRefuses() {
		TLObject frozen = newObject(qualified(PROJECT), FROZEN);
		becomeUser(_root);
		FormControl form = newForm(frozen);

		ExecutableState state = form.editPermission();
		assertTrue("Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals("The project is frozen.",
			Resources.getInstance(Locale.ENGLISH).getString(state.getI18NReasonKey()));
		assertFalse(form.enterEditMode());
	}

	/**
	 * The configured edit rule still applies where the right is granted.
	 */
	public void testConfiguredRuleApplies() {
		becomeUser(_responsible);
		FormControl form = newForm(_project);
		form.setEditRule(DENIED);

		assertSame(ExecutableState.NO_EXEC_PERMISSION, form.editPermission());
		assertFalse(form.enterEditMode());
	}

	/**
	 * Of two refusals, the stronger one wins; of two equally strong ones, the right gives the reason.
	 */
	public void testStrongerRefusalWins() {
		becomeUser(_roleless);
		FormControl form = newForm(_project);
		form.setEditRule(HIDDEN);
		assertSame(ExecutableState.NOT_EXEC_HIDDEN, form.editPermission());

		form.setEditRule(DENIED);
		assertDisabled(com.top_logic.layout.view.security.I18NConstants.ERROR_WRITE_DENIED, form.editPermission());
	}

	/**
	 * A draft of a create dialog is editable, even for a user who may write no persistent object of
	 * its type: its creation was checked when it was created.
	 */
	public void testDraftEditable() {
		becomeUser(_roleless);
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(CATEGORY));
		FormControl form = newForm(draft);

		assertSame(ExecutableState.EXECUTABLE, form.editPermission());
		assertTrue(form.enterEditMode());
	}

	/**
	 * A user bypassing the model security edits every object.
	 */
	public void testRoot() {
		becomeUser(_root);
		FormControl form = newForm(_category);

		assertSame(ExecutableState.EXECUTABLE, form.editPermission());
		assertTrue(form.enterEditMode());
	}

	private static FormControl newForm(TLObject object) {
		return new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")), object, "no model", NoTokenHandling.INSTANCE);
	}

	private static void assertDisabled(ResKey expectedReason, ExecutableState state) {
		assertTrue("Expected a disabled command, got: " + state, state.isDisabled());
		assertEquals(expectedReason.getKey(), state.getI18NReasonKey().getKey());
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestFormEditRights.class);
	}
}
