/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.CancellationException;

/**
 * Cooperative cancellation token for long-running tool operations.
 *
 * <p>Tools can check {@link #isCancelled()} periodically during execution,
 * or call {@link #checkCancelled()} to throw if cancellation was requested.
 * The agent loop calls {@link #cancel()} when the user cancels the run.</p>
 *
 * <p>Thread-safe: the cancelled flag is volatile.</p>
 *
 * @see ToolContext#cancellationToken()
 */
public class CancellationToken {

	/** Token that can never be cancelled. For use in test/internal contexts. */
	public static final CancellationToken UNCANCELLABLE = new CancellationToken() {
		@Override
		public void cancel() {
			// Ignored — this token cannot be cancelled.
		}
	};

	private volatile boolean _cancelled;

	/** Whether cancellation has been requested. */
	public boolean isCancelled() {
		return _cancelled;
	}

	/** Requests cancellation of the associated agent run. */
	public void cancel() {
		_cancelled = true;
	}

	/**
	 * Throws {@link CancellationException} if cancellation has been requested.
	 *
	 * @throws CancellationException if {@link #isCancelled()} is {@code true}
	 */
	public void checkCancelled() {
		if (_cancelled) {
			throw new CancellationException("Tool execution cancelled.");
		}
	}
}
