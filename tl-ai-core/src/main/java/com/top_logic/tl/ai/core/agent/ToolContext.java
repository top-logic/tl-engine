/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Context passed to tool execution.
 *
 * <p>Will carry SecurityContext, TransactionScope, and ProgressReporter
 * as those layers are added. For now, provides the seam so the
 * {@link ToolExecutor} interface doesn't need to change later.</p>
 *
 * @see ToolExecutor
 */
public class ToolContext {

	/** Shared empty context for use when no security/transaction context is needed. */
	public static final ToolContext EMPTY = new ToolContext();
}
