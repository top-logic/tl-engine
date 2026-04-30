/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Thrown when a lock cannot be acquired because the target is already locked by another editor.
 *
 * <p>Contains details about the locked resource and current holder to help the LLM
 * produce a useful message for the user. The formatted message explicitly instructs
 * the LLM not to retry.</p>
 *
 * @see TransactionScope#acquireLock(String, com.top_logic.model.TLObject...)
 */
public class LockConflictException extends TransactionException {

	private final String _resource;

	private final String _holder;

	/**
	 * Creates a new lock conflict exception.
	 *
	 * @param resource   human-readable description of the locked resource
	 * @param holder     name of the current lock holder, or "unknown"
	 * @param retryCount number of acquisition attempts that were made
	 * @param cause      the underlying locking error
	 */
	public LockConflictException(String resource, String holder, int retryCount, Throwable cause) {
		super(buildMessage(resource, holder, retryCount), cause);
		_resource = resource;
		_holder = holder;
	}

	/**
	 * The locked resource description.
	 */
	public String resource() {
		return _resource;
	}

	/**
	 * The name of the current lock holder.
	 */
	public String holder() {
		return _holder;
	}

	private static String buildMessage(String resource, String holder, int retryCount) {
		return "LOCK_CONFLICT: The resource is currently locked by another editor.\n\n"
			+ "Resource: " + resource + "\n"
			+ "Lock holder: " + holder + "\n\n"
			+ "The system retried " + retryCount + " times over " + (retryCount * 10)
			+ " seconds but the lock was not released.\n\n"
			+ "Do NOT retry this tool call. Inform the user that the element is currently "
			+ "being edited and cannot be modified until the lock is released.";
	}
}
