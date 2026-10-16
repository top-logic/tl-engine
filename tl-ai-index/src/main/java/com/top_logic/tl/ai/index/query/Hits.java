/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.query;

import java.util.List;

/**
 * A page of hits.
 *
 * @param hits
 *        The objects, best first.
 * @param moreAvailable
 *        Whether a larger page could hold more; never how many hits the reader may not see.
 * @param mode
 *        What the search used: {@link SearchMode#WORDS} when the meaning side was asked for but is not
 *        available.
 * @param indexedRevision
 *        The revision up to which the index holds every change; -1 when no index is built yet.
 * @param pendingRevisions
 *        How many revisions the index is behind the knowledge base.
 * @param notice
 *        A sentence for the reader about the search's limits, or <code>null</code>.
 */
public record Hits(List<Hit> hits, boolean moreAvailable, SearchMode mode, long indexedRevision,
		long pendingRevisions, String notice) {
	// A plain carrier.
}
