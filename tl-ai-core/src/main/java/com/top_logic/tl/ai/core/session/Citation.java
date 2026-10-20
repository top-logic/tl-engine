/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

/**
 * One source a {@code [n]} footnote in an assistant answer points to.
 *
 * @param index 1-based marker shown in the answer
 * @param toolName tool that produced the cited id or URL, or empty
 * @param toolCallId that tool's call id, or empty
 * @param kind host kind such as {@code web} or {@code invoice}, as the application declared it
 * @param kindLabel what to call that kind in front of a reader, in the reader's language; empty
 *        when the host declared none, and the {@code kind} is then shown instead
 * @param id id the tool returned
 * @param label human label for the chip popover
 * @param url link for a web source, or empty
 * @param passage the sentence of the source the fact rests on, or empty
 * @param excerpt a window of the source around {@code passage}, for the marker's popover
 * @param archiveUrl where the copy of a web page archived when it was read can be looked at, or
 *        empty
 * @param archivedAt when that copy was taken, ISO-8601; empty without a copy
 */
public record Citation(int index, String toolName, String toolCallId, String kind, String kindLabel,
		String id, String label, String url, String passage, String excerpt, String archiveUrl,
		String archivedAt) {

	/** A citation without the passage the popover shows. */
	public Citation(int index, String toolName, String toolCallId, String kind, String id, String label,
			String url) {
		this(index, toolName, toolCallId, kind, "", id, label, url, "", "");
	}

	/** A citation without an archived copy. */
	public Citation(int index, String toolName, String toolCallId, String kind, String kindLabel,
			String id, String label, String url, String passage, String excerpt) {
		this(index, toolName, toolCallId, kind, kindLabel, id, label, url, passage, excerpt, "", "");
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
		archiveUrl = archiveUrl == null ? "" : archiveUrl;
		archivedAt = archivedAt == null ? "" : archivedAt;
	}
}
