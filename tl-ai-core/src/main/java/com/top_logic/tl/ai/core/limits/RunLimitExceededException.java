/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import com.top_logic.basic.util.ResKey;

/**
 * Thrown where a run enters when a ceiling this installation set would be exceeded by starting it.
 *
 * <p>
 * Not a failure and not a security refusal: nothing is wrong with the request, there is only no room
 * for it right now. Callers that have a surface show {@link #reason()} and offer the user to try
 * again; the English {@link #getMessage() message} is for the log.
 * </p>
 *
 * @see RunLimits#admit
 */
public class RunLimitExceededException extends RuntimeException {

	private final ResKey _reason;

	private final boolean _temporary;

	/**
	 * Creates a {@link RunLimitExceededException}.
	 *
	 * @param reason
	 *        The refusal in the acting user's language, for a client that has to show it to them.
	 * @param message
	 *        The same refusal in English, naming the account and the limit, for the log.
	 * @param temporary
	 *        Whether trying again shortly can succeed - true for a concurrency ceiling, false for a
	 *        spent budget, which only the passing of the window or an administrator changes.
	 */
	public RunLimitExceededException(ResKey reason, String message, boolean temporary) {
		super(message);
		_reason = reason;
		_temporary = temporary;
	}

	/** The refusal in the acting user's language. */
	public ResKey reason() {
		return _reason;
	}

	/** Whether trying again shortly can succeed. */
	public boolean isTemporary() {
		return _temporary;
	}
}
