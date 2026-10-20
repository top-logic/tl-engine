/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.sync;

/**
 * Where the index stands.
 *
 * @param ready
 *        Whether a generation serves searches.
 * @param indexedRevision
 *        The revision up to which the serving generation holds every change; -1 when none serves.
 * @param headRevision
 *        The newest revision of the knowledge base this node knows.
 * @param pendingRevisions
 *        How many revisions the serving generation is behind.
 * @param rebuilding
 *        Whether a new generation is being built.
 * @param buildTotal
 *        How many objects the new generation indexes.
 * @param buildDone
 *        How many of them it has indexed.
 * @param meaning
 *        Whether searches can use the meaning side; <code>false</code> without an embedder or while
 *        the serving generation's vectors come from another model.
 * @param lastError
 *        Why the last step of the worker failed, or <code>null</code>.
 */
public record IndexStatus(boolean ready, long indexedRevision, long headRevision, long pendingRevisions,
		boolean rebuilding, long buildTotal, long buildDone, boolean meaning, String lastError) {
	// A plain carrier.
}
