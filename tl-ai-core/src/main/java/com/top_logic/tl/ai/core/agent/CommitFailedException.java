/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.List;

/**
 * The transaction of a turn could not be committed, for the {@link #reasons()} the application and
 * the database gave.
 *
 * <p>
 * The reasons are kept apart from any sentence around them: a veto of the application is resolved
 * in the language of the run's user, and whoever reports the failure - to the model, to the user -
 * puts them into a field of its own result rather than into its own prose.
 * </p>
 *
 * @see TransactionScope#commit()
 */
public class CommitFailedException extends TransactionException {

	/** How the {@link #getMessage() message} of a {@link CommitFailedException} starts. */
	public static final String MESSAGE_PREFIX = "Commit failed: ";

	private final List<String> _reasons;

	/**
	 * Creates a {@link CommitFailedException}.
	 *
	 * @param reasons
	 *        See {@link #reasons()}.
	 * @param cause
	 *        What the commit threw.
	 */
	public CommitFailedException(List<String> reasons, Throwable cause) {
		super(MESSAGE_PREFIX + String.join(" ", reasons), cause);
		_reasons = List.copyOf(reasons);
	}

	/**
	 * Why the commit failed: the application's and the database's own texts, one each; empty if the
	 * failure gave none.
	 */
	public List<String> reasons() {
		return _reasons;
	}
}
