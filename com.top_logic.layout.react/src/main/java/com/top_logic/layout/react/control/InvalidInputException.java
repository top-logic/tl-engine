/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control;

import com.top_logic.basic.util.ResKey;
import com.top_logic.util.error.TopLogicException;

/**
 * The refusal of a command because of input the user has to correct first, e.g. fields with
 * validation errors.
 *
 * <p>
 * Besides showing the message as any {@link TopLogicException}, the client moves the focus to the
 * first field showing an error in the {@link #getScope() scope} of the refusal, see
 * {@link ReactCommandAnswer#ERROR_CODE_INVALID_INPUT}. Created through
 * {@link CommandErrors#invalidInput(ResKey, java.util.List, ReactControl)}.
 * </p>
 */
public class InvalidInputException extends TopLogicException {

	private final String _scope;

	/**
	 * Creates a {@link InvalidInputException} with a message of its own.
	 *
	 * @param message
	 *        The summary of the refusal.
	 * @param scope
	 *        See {@link #getScope()}.
	 */
	InvalidInputException(ResKey message, String scope) {
		super(message);
		_scope = scope;
	}

	/**
	 * Creates a {@link InvalidInputException} with the given details chained as cause.
	 *
	 * @param message
	 *        The summary of the refusal.
	 * @param cause
	 *        The first of the detail messages, see {@link CommandErrors#error(ResKey, java.util.List)}.
	 * @param scope
	 *        See {@link #getScope()}.
	 */
	InvalidInputException(ResKey message, Throwable cause, String scope) {
		super(message, cause);
		_scope = scope;
	}

	/**
	 * The ID of the control whose fields hold the input to correct, e.g. the form a save command
	 * stores; {@code null} for the control the refused command was sent to.
	 *
	 * <p>
	 * A command is not necessarily sent to an element inside the fields it applies: the Save button
	 * of a form lives in the toolbar of the enclosing panel, or in the footer of a dialog.
	 * </p>
	 */
	public String getScope() {
		return _scope;
	}

}
