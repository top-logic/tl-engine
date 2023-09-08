/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

/**
 * Kind of comparison.
 * 
 * @see ComparableCompare#getKind()
 */
public enum PrimitiveCompareKind {
	/**
	 * The values equal each other.
	 */
	EQUALS,

	/**
	 * The left-hand side is greater or equal to the right-hand side.
	 */
	GE,

	/**
	 * The left-hand side is greater to the right-hand side.
	 */
	GT,

	/**
	 * The left-hand side is less than or equal to the right-hand side.
	 */
	LE,

	/**
	 * The left-hand side is less than the right-hand side.
	 */
	LT,

	/**
	 * The left-hand side not equals the right-hand side.
	 */
	NOT_EQUALS;
}