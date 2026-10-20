/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Wraps failures from the transaction or locking layer.
 *
 * <p>Thrown by {@link TransactionScope#commit()} when the underlying KB transaction
 * fails, or by {@link TransactionScope#acquireLock(String, com.top_logic.model.TLObject...)}
 * when a lock cannot be acquired.</p>
 *
 * @see TransactionScope
 */
public class TransactionException extends RuntimeException {

	/**
	 * Creates a new exception with the given message.
	 */
	public TransactionException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with the given message and cause.
	 */
	public TransactionException(String message, Throwable cause) {
		super(message, cause);
	}
}
