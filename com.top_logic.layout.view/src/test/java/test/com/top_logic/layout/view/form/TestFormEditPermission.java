/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TransientObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests that the rule guarding a {@link FormControl}'s edit mode governs both the Edit command and
 * the {@code formEdit} command a client dispatches to the control.
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

	private static FormControl newForm() {
		return new FormControl(new DefaultReactContext("", "test", new SSEUpdateQueue()), new MockTLObject(),
			"no model", NoTokenHandling.INSTANCE);
	}

	/**
	 * Object to display in the form; no attribute of it is read by these tests.
	 */
	private static class MockTLObject extends TransientObject {
		// Inherits the transient no-op implementation.
	}

}
