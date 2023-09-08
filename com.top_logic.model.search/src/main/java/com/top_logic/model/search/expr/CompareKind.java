/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

/**
 * Kind of {@link CompareOp} operators with full comparable comparison.
 */
public enum CompareKind {
	/**
	 * The left-hand side is greater or equal to the right-hand side.
	 */
	GE(">="),

	/**
	 * The left-hand side is greater to the right-hand side.
	 */
	GT(">"),

	/**
	 * The left-hand side is less than or equal to the right-hand side.
	 */
	LE("<="),

	/**
	 * The left-hand side is less than the right-hand side.
	 */
	LT("<");

	/**
	 * Creates a {@link CompareKind}.
	 */
	private CompareKind(String operator) {
		_operator = operator;
	}

	private final String _operator;

	/**
	 * The syntax of the operator.
	 */
	public String getOperator() {
		return _operator;
	}
}