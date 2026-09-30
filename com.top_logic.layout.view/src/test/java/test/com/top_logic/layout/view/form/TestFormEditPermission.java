/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.TransientObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests that the rule guarding a {@link FormControl}'s edit mode governs every transition into edit
 * mode: the Edit command, the {@code formEdit} command a client dispatches to the control, the
 * initial edit mode, the object switch of an auto-edit form, and the edit-mode channel.
 */
public class TestFormEditPermission extends TestCase {

	private static final String CMD_FORM_EDIT = "formEdit";

	private static final ViewExecutabilityRule DENIED = input -> ExecutableState.NO_EXEC_PERMISSION;

	/**
	 * Without a rule, editing is offered to everyone who sees the form.
	 */
	public void testEditPermittedByDefault() {
		FormControl form = newForm();

		assertTrue("Editing is offered without a guarding rule.", form.editPermission().isExecutable());
	}

	/**
	 * A denying rule withdraws the permission the Edit command is built on.
	 */
	public void testDeniedRuleWithdrawsPermission() {
		FormControl form = newForm();
		form.setEditRule(DENIED);

		assertFalse("A denied form reports no edit permission.", form.editPermission().isExecutable());
	}

	/**
	 * A denying rule also refuses the command dispatched to the control itself, which is the path a
	 * client takes past the Edit button.
	 */
	public void testDeniedRuleRefusesDispatchedCommand() {
		FormControl form = newForm();
		form.setEditRule(DENIED);

		HandlerResult result = form.executeCommand(CMD_FORM_EDIT, Map.of());

		assertFalse("A denied edit command reports failure.", result.isSuccess());
		assertFalse("A denied edit command does not enter edit mode.", form.isEditMode());
	}

	/**
	 * A denying rule refuses entering edit mode directly, the path of a form configured for initial
	 * edit mode.
	 */
	public void testDeniedRuleRefusesDirectEntry() {
		FormControl form = newForm();
		form.setEditRule(DENIED);

		assertFalse("A denied transition reports that no edit session started.", form.enterEditMode());
		assertFalse("A denied transition does not enter edit mode.", form.isEditMode());
	}

	/**
	 * A permitted direct entry starts an edit session.
	 */
	public void testPermittedDirectEntry() {
		FormControl form = newForm();

		assertTrue("A permitted transition reports the started edit session.", form.enterEditMode());
		assertTrue("A permitted transition enters edit mode.", form.isEditMode());
		assertFalse("A second transition starts no further edit session.", form.enterEditMode());
	}

	/**
	 * An auto-edit form switching objects enters edit mode for an object the rule permits and stays
	 * in view mode for an object the rule denies.
	 */
	public void testAutoEditObjectSwitchChecksNewObject() {
		TLObject first = new MockTLObject();
		TLObject permitted = new MockTLObject();
		TLObject denied = new MockTLObject();

		FormControl form = newForm(first);
		form.setEditRule(
			obj -> obj == denied ? ExecutableState.NO_EXEC_PERMISSION : ExecutableState.EXECUTABLE);
		ViewChannel input = newInput(form, first);
		form.setAutoEditMode(true);

		input.set(denied);
		assertSame("The form displays the object its input channel delivered.", denied, form.getCurrentObject());
		assertFalse("A denied object is displayed in view mode.", form.isEditMode());

		input.set(permitted);
		assertTrue("A permitted object is displayed in edit mode.", form.isEditMode());
	}

	/**
	 * An auto-edit form under a denying rule stays in view mode after an object switch.
	 */
	public void testAutoEditObjectSwitchDenied() {
		TLObject first = new MockTLObject();
		TLObject second = new MockTLObject();

		FormControl form = newForm(first);
		form.setEditRule(DENIED);
		ViewChannel input = newInput(form, first);
		form.setAutoEditMode(true);

		input.set(second);

		assertFalse("A denied object is displayed in view mode.", form.isEditMode());
	}

	/**
	 * An edit-mode channel set to {@code true} under a denying rule leaves the form in view mode and
	 * is reset to the form's actual mode.
	 */
	public void testEditModeChannelDenied() {
		FormControl form = newForm();
		form.setEditRule(DENIED);
		ViewChannel editMode = newEditModeChannel(form);

		editMode.set(Boolean.TRUE);

		assertFalse("A denied channel request does not enter edit mode.", form.isEditMode());
		assertEquals("The channel mirrors the form's actual mode.", Boolean.FALSE, editMode.get());
	}

	/**
	 * A listener registered on the edit-mode channel after the form observes the reset as the last
	 * value, not the refused request.
	 */
	public void testEditModeChannelDeniedLaterListenerSeesReset() {
		FormControl form = newForm();
		form.setEditRule(DENIED);
		ViewChannel editMode = newEditModeChannel(form);
		List<Object> observed = new ArrayList<>();
		editMode.addListener((sender, oldValue, newValue) -> observed.add(newValue));

		editMode.set(Boolean.TRUE);

		assertEquals("The later listener observes the request, then the reset.",
			List.of(Boolean.TRUE, Boolean.FALSE), observed);
		assertEquals("The channel mirrors the form's actual mode.", Boolean.FALSE, editMode.get());
		assertFalse("A denied channel request does not enter edit mode.", form.isEditMode());
	}

	/**
	 * An edit-mode channel set to {@code true} under a permitting rule enters edit mode.
	 */
	public void testEditModeChannelPermitted() {
		FormControl form = newForm();
		ViewChannel editMode = newEditModeChannel(form);

		editMode.set(Boolean.TRUE);

		assertTrue("A permitted channel request enters edit mode.", form.isEditMode());
		assertEquals("The channel mirrors the form's actual mode.", Boolean.TRUE, editMode.get());
	}

	private static FormControl newForm() {
		return newForm(new MockTLObject());
	}

	private static FormControl newForm(TLObject initialObject) {
		return new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test")), initialObject,
			"no model", NoTokenHandling.INSTANCE);
	}

	/**
	 * Binds the given form to an input channel that already delivers the form's initial object.
	 */
	private static ViewChannel newInput(FormControl form, TLObject initialObject) {
		ViewChannel input = new DefaultViewChannel("input");
		input.set(initialObject);
		form.setInputChannel(input);
		return input;
	}

	/**
	 * Binds the given form to an edit-mode channel reporting view mode.
	 */
	private static ViewChannel newEditModeChannel(FormControl form) {
		ViewChannel editMode = new DefaultViewChannel("editMode");
		editMode.set(Boolean.FALSE);
		form.setEditModeChannel(editMode);
		return editMode;
	}

	/**
	 * Object to display in the form; no attribute of it is read by these tests.
	 */
	private static class MockTLObject extends TransientObject {
		// Inherits the transient no-op implementation.
	}

}
