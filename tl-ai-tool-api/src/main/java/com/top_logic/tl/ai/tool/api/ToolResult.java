/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Structured return type for {@link Tool#execute}.
 *
 * <p>
 * A tool result is text in the common case. A tool that has something to <em>show</em> - an image, a
 * PDF - additionally fills {@link #content()}: the ordered content parts the model is to perceive in
 * place of the plain text. Every provider that can carry non-text content inside a tool result does
 * so natively; the one that cannot (the Chat Completions API, whose {@code tool} role is text-only)
 * falls back to a follow-up user message inside its own request builder.
 * </p>
 *
 * @param text           the tool's result text (never {@code null}). Stays authoritative for
 *                       logging, compaction, token accounting and replay, and is the text-only
 *                       projection of {@link #content()} when that is filled
 * @param summary        optional compact summary for use during context compaction,
 *                       or {@code null} if the tool does not provide one
 * @param truncationHint per-tool maximum character count before the agent loop
 *                       truncates the result, or {@code -1} to use the global default
 * @param content        the parts the model perceives, or empty for a text-only result. When
 *                       non-empty this list is the <em>complete</em> rendering of the result, so a
 *                       tool that wants its text read must include it as a
 *                       {@link ToolContent.Text} part - see {@link #multimodal}
 * @param failed         whether this answer reports a failure rather than an outcome - see
 *                       {@link #failure(String)}
 */
public record ToolResult(String text, String summary, int truncationHint,
		List<ToolContent> content, boolean failed) {

	/** */
	public ToolResult {
		Objects.requireNonNull(text, "Text must not be null.");
		content = content == null ? List.of() : List.copyOf(content);
	}

	/**
	 * Creates a successful {@link ToolResult}.
	 */
	public ToolResult(String text, String summary, int truncationHint, List<ToolContent> content) {
		this(text, summary, truncationHint, content, false);
	}

	/**
	 * Creates a text-only {@link ToolResult}.
	 */
	public ToolResult(String text, String summary, int truncationHint) {
		this(text, summary, truncationHint, List.of());
	}

	/**
	 * Creates a {@link ToolResult} with only the result text and default settings.
	 */
	public static ToolResult of(String text) {
		return new ToolResult(text, null, -1);
	}

	/**
	 * Creates a {@link ToolResult} that reports a failure.
	 *
	 * <p>
	 * The text is what the model reads, exactly as for a successful answer; what {@link #failed()}
	 * adds is the one bit every provider and the MCP protocol carry beside it - {@code is_error} for
	 * Anthropic and Bedrock, {@code isError} for MCP. Without it a sentence beginning with
	 * {@code "Error: "} arrives as a <em>successful</em> call whose text happens to say otherwise, and
	 * nothing but the model's reading of English tells the two apart.
	 * </p>
	 *
	 * <p>
	 * A failure is <b>text only</b>: the agent loop records it as a failed call, and a failed call
	 * carries no summary, no truncation hint and no content parts. There is nothing to summarize for
	 * a compaction and nothing to show, so the sentence has to say the whole thing.
	 * </p>
	 *
	 * <p>
	 * The text is still <b>bounded</b>: a failure stays in the conversation for every later request
	 * of the run exactly as a success does, so the agent loop cuts it to the installation's
	 * {@code max-tool-result-chars} ceiling and reports that it did. Having no truncation hint here
	 * is the decision, not an omission - a tool must not be able to lift the ceiling off its own
	 * error path, and most failures (a thrown exception, a remote server's error body) never pass a
	 * {@link ToolResult} at all, so a hint would bound only some of them.
	 * </p>
	 *
	 * <p>
	 * <b>A failure reported this way does not roll the turn back.</b> It is recorded as a synthetic
	 * failure, so the writes other tools of the same turn made are still committed. A tool whose own
	 * partial writes must be taken back throws instead - that is what an exception out of a tool
	 * means, and it is deliberately the more expensive of the two.
	 * </p>
	 *
	 * @param text
	 *        The result text, read by the model and used for logging and compaction.
	 */
	public static ToolResult failure(String text) {
		return new ToolResult(text, null, -1, List.of(), true);
	}

	/**
	 * Creates a {@link ToolResult} whose text is followed by content for the model to perceive.
	 *
	 * @param text
	 *        The result text, read by the model and used for logging and compaction.
	 * @param attachments
	 *        The images and documents to show alongside {@code text}, in display order.
	 */
	public static ToolResult multimodal(String text, ToolContent... attachments) {
		Objects.requireNonNull(text, "Text must not be null.");
		List<ToolContent> content = new ArrayList<>(attachments.length + 1);
		content.add(new ToolContent.Text(text));
		for (ToolContent attachment : attachments) {
			content.add(Objects.requireNonNull(attachment, "Attachment must not be null."));
		}
		return new ToolResult(text, null, -1, content);
	}
}
