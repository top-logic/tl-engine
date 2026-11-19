/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.discovery;

/**
 * One section of the <code>llms.txt</code> document of an application, contributed by an
 * {@link AgentDiscoveryContribution}.
 *
 * <p>
 * Written as a level-two Markdown heading followed by the body.
 * </p>
 *
 * @param title
 *        The heading of the section (plain text, a single line).
 * @param markdown
 *        The body of the section in Markdown, typically a short paragraph followed by a list of
 *        links (<code>- [name](url): description</code>). Absolute URLs are built with
 *        {@link DiscoveryContext#url(String)}.
 */
public record LlmsTxtSection(String title, String markdown) {
	// Pure value.
}
