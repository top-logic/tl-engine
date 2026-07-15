/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.table.filter;

/**
 * Comparison operator of a {@link RangeFilterState range filter}.
 */
public enum ComparisonOperator {

	/** Equal to the primary value. */
	EQ,

	/** Not equal to the primary value. */
	NE,

	/** Strictly less than the primary value. */
	LT,

	/** Less than or equal to the primary value. */
	LE,

	/** Strictly greater than the primary value. */
	GT,

	/** Greater than or equal to the primary value. */
	GE,

	/** Between the primary (inclusive) and secondary (inclusive) values. */
	BETWEEN;

}
