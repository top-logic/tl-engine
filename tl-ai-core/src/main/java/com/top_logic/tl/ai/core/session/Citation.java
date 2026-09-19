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
 * @param kind host kind such as {@code web} or {@code invoice}, the model's own vocabulary
 * @param kindLabel what to call that kind in front of a reader, in the reader's language; empty
 *        when the host declared none, and the {@code kind} is then shown instead
 * @param id id the tool returned
 * @param label human label for the chip popover
 * @param url link for a web source, or empty
 * @param passage the sentence of the source the fact rests on, or empty
 * @param excerpt a window of the source around {@code passage}, for the marker's popover
 */
public record Citation(int index, String toolName, String toolCallId, String kind, String kindLabel,
		String id, String label, String url, String passage, String excerpt) {

	/** A citation without the passage the popover shows. */
	public Citation(int index, String toolName, String toolCallId, String kind, String id, String label,
			String url) {
		this(index, toolName, toolCallId, kind, "", id, label, url, "", "");
	}

	/** Empty strings rather than {@code null} for optional fields. */
	public Citation {
		toolName = toolName == null ? "" : toolName;
		toolCallId = toolCallId == null ? "" : toolCallId;
		kind = kind == null ? "" : kind;
		kindLabel = kindLabel == null ? "" : kindLabel;
		id = id == null ? "" : id;
		label = label == null ? "" : label;
		url = url == null ? "" : url;
		passage = passage == null ? "" : passage;
		excerpt = excerpt == null ? "" : excerpt;
	}
}
