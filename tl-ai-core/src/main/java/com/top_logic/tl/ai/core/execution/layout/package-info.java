/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */

/**
 * UI support for the agent activity monitor.
 *
 * <p>
 * The views themselves are layout XML plus TL-Script; this package holds the one operation that
 * cannot be expressed declaratively - cancelling a running agent run has to go through
 * {@link com.top_logic.tl.ai.core.execution.AgentExecutionStore}, not through a UI transaction.
 * </p>
 */
package com.top_logic.tl.ai.core.execution.layout;
