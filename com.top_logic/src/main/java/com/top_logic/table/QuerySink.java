/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Backend-defined sink that collects sort and filter contributions for a query-backed
 * {@link RowSource}.
 *
 * <p>
 * This is an extension seam: the model tier carries {@link OrderPushdown} and
 * {@link FilterPushdown} contributions opaquely, and a concrete query backend (e.g. one
 * built on the TopLogic search/knowledge-base API) provides a {@link QuerySink}
 * implementation that they target. Keeping the type opaque here lets the model tier stay
 * free of any data-tier dependency.
 * </p>
 */
public interface QuerySink {
	// Marker extension point; concrete query backends define the contribution API.
}
