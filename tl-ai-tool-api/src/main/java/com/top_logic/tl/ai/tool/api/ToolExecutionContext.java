/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * Execution context available to all tools, regardless of which module defines them.
 *
 * <p>Tools in {@code tl-ai-core} may downcast to the full {@code ToolContext} for
 * access to security, transactions, and approval. Tools in external modules use
 * only this interface.</p>
 */
public interface ToolExecutionContext {

	/**
	 * Report progress to the UI during long-running operations.
	 */
	ProgressReporter progressReporter();

	/**
	 * Token for cooperative cancellation. Tools should check this periodically
	 * during long-running operations.
	 */
	CancellationToken cancellationToken();
}
