/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

/**
 * Provider-neutral outcome of a web search.
 *
 * @param answer
 *        An optional synthesized answer supplied by the engine. May be {@code null}.
 * @param hits
 *        The ranked search results. Never {@code null}.
 */
public record WebSearchResults(String answer, List<WebSearchHit> hits) {
	// Neutral value type returned by every WebSearchEngine.
}
