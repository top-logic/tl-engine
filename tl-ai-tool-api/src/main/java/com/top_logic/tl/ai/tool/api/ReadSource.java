/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.time.Instant;

/**
 * Something a tool read for the run, which the answer may therefore point at.
 *
 * <p>
 * An answer cites by writing a link to a source into its text - {@code [Sommerfest](tl:GenericObject:11047)},
 * {@code [laut Tagesschau](https://...)} - and the framework keeps the link as a footnote only when
 * a tool of the run read that source. The model does not declare its sources; the tools that read
 * them do, through {@link ToolExecutionContext#readSource(ReadSource)}. A source no tool read does
 * not count.
 * </p>
 *
 * @param kind
 *        What kind of source this is, from the vocabulary the application declares - {@code web},
 *        {@code object}, or an application's own kind. Decided by the tool, never by the model.
 * @param id
 *        The reference an answer writes to point at this source: an object's identifier
 *        ({@code TypeName:id}), a URL, or an application's own record code.
 * @param title
 *        What to call the source in front of a reader: an object's label, a page's title.
 * @param url
 *        The address of a web source; empty for any other kind.
 * @param text
 *        What the tool read of the source, so that the popover can show the passage a citation
 *        rests on and a quoted sentence can be checked against it. Empty when the tool has nothing
 *        better than its answer. Held for the run only and never persisted with the reading list.
 * @param archiveUrl
 *        Where a copy of the source as it was read can be looked at, or empty. A web page is
 *        archived when it is read, so a citation still works after the page changed or disappeared.
 * @param readAt
 *        When the tool read the source.
 */
public record ReadSource(String kind, String id, String title, String url, String text,
		String archiveUrl, Instant readAt) {

	/** Empty strings rather than {@code null} for the optional fields. */
	public ReadSource {
		kind = kind == null ? "" : kind;
		id = id == null ? "" : id;
		title = title == null ? "" : title;
		url = url == null ? "" : url;
		text = text == null ? "" : text;
		archiveUrl = archiveUrl == null ? "" : archiveUrl;
		readAt = readAt == null ? Instant.now() : readAt;
	}

	/**
	 * A source the answer points at by its reference alone, read now.
	 *
	 * @param kind
	 *        See {@link #kind()}.
	 * @param id
	 *        See {@link #id()}.
	 * @param title
	 *        See {@link #title()}.
	 */
	public static ReadSource of(String kind, String id, String title) {
		return new ReadSource(kind, id, title, "", "", "", null);
	}

	/** What a reading list knows this source by: its {@link #url()}, or its {@link #id()} when it has none. */
	public String reference() {
		return url.isEmpty() ? id : url;
	}

	/** A copy of this source carrying the given read text. */
	public ReadSource withText(String newText) {
		return new ReadSource(kind, id, title, url, newText, archiveUrl, readAt);
	}

	/** A copy of this source without its read text, as the reading list is persisted. */
	public ReadSource withoutText() {
		return text.isEmpty() ? this : new ReadSource(kind, id, title, url, "", archiveUrl, readAt);
	}
}
