/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.Objects;

/**
 * Structured return type for {@link Tool#execute}.
 *
 * @param text           the tool's result text (never {@code null})
 * @param summary        optional compact summary for use during context compaction,
 *                       or {@code null} if the tool does not provide one
 * @param truncationHint per-tool maximum character count before the agent loop
 *                       truncates the result, or {@code -1} to use the global default
 */
public record ToolResult(String text, String summary, int truncationHint) {

	/** */
	public ToolResult {
		Objects.requireNonNull(text, "Text must not be null.");
	}

	/**
	 * Creates a {@link ToolResult} with only the result text and default settings.
	 */
	public static ToolResult of(String text) {
		return new ToolResult(text, null, -1);
	}
}
