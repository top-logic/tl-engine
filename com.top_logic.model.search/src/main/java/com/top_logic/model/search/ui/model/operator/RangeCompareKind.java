/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

/**
 * Kind of {@link IntegerRangeCompare} comparison.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum RangeCompareKind {
	/**
	 * Check that the value is within the range.
	 */
	IN_RANGE,

	/**
	 * Check that the value is not within the range.
	 */
	NOT_IN_RANGE,
}