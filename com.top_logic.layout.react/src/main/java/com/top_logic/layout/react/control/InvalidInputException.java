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
 * first field showing an error in the control the command was sent to, see
 * {@link ReactCommandAnswer#ERROR_CODE_INVALID_INPUT}. Created through
 * {@link CommandErrors#invalidInput(ResKey, java.util.List)}.
 * </p>
 */
public class InvalidInputException extends TopLogicException {

	/**
	 * Creates a {@link InvalidInputException} with a message of its own.
	 *
	 * @param message
	 *        The summary of the refusal.
	 */
	InvalidInputException(ResKey message) {
		super(message);
	}

	/**
	 * Creates a {@link InvalidInputException} with the given details chained as cause.
	 *
	 * @param message
	 *        The summary of the refusal.
	 * @param cause
	 *        The first of the detail messages, see {@link CommandErrors#error(ResKey, java.util.List)}.
	 */
	InvalidInputException(ResKey message, Throwable cause) {
		super(message, cause);
	}

}
