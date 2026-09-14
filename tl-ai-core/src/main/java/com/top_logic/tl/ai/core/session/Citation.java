/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

/**
 * One source a {@code [n]} marker in an assistant answer points to.
 *
 * @param index 1-based marker shown in the answer
 * @param toolName tool that produced the cited id or URL
 * @param toolCallId that tool's call id, or empty
 * @param kind host kind such as {@code web} or {@code invoice}
 * @param id id the tool returned
 * @param label human label for the chip popover
 * @param url link for a web source, or empty
 */
public record Citation(int index, String toolName, String toolCallId, String kind, String id, String label,
		String url) {

	/** Empty strings rather than {@code null} for optional fields. */
	public Citation {
		toolName = toolName == null ? "" : toolName;
		toolCallId = toolCallId == null ? "" : toolCallId;
		kind = kind == null ? "" : kind;
		id = id == null ? "" : id;
		label = label == null ? "" : label;
		url = url == null ? "" : url;
	}
}
