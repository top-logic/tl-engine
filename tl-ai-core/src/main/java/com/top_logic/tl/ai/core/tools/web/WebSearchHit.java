/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

/**
 * A single, provider-neutral web search result.
 *
 * @param title
 *        The result title.
 * @param url
 *        The result URL.
 * @param snippet
 *        A short, query-relevant extract. May be {@code null}.
 * @param content
 *        The full page content, if the engine supplied it. May be {@code null}.
 */
public record WebSearchHit(String title, String url, String snippet, String content) {
	// Neutral value type shared across WebSearchEngine implementations.
}
