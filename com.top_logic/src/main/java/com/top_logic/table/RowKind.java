/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table;

/**
 * Kind of a displayed {@link Row}.
 *
 * <p>
 * Unifies plain data rows with the synthetic rows produced by grouping, so that flat
 * tables, tree tables, grouping and aggregation all share one windowed row API.
 * </p>
 */
public enum RowKind {

	/** A regular data row backed by a business object ({@link Row#data()}). */
	DATA,

	/** A synthetic group header row introduced by grouping ({@link Row#group()}). */
	GROUP_HEADER,

	/** A synthetic aggregation/footer row computed over a {@link Group}. */
	AGGREGATE;

}
