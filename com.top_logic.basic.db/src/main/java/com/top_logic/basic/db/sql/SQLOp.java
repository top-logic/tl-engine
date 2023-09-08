/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Enumeration of Operations found in SQL.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public enum SQLOp {

	/**
	 * Addition of two values.
	 */
	add,
	
	/**
	 * Subtraction of one value from another value.
	 */
	sub,
	
	/**
	 * Multiplication of two values.
	 */
	mul,

	/**
	 * Division of one value by another value.
	 */
	div,

	/**
	 * Logical conjunction of two values.
	 */
	//  AND  
	and,

	/**
	 * Logical disjunction of two values.
	 */
	// OR
	or,

	/**
	 * Equality of two values.
	 */
	// ==
	eq,

	/**
	 * Whether the first value is strict less than the second value.
	 * 
	 * @see SQLOp#le
	 */
	// <
	lt,

	/**
	 * Whether the first value is strict greater than the second value.
	 * 
	 * @see SQLOp#ge
	 */
	// >
	gt,

	/**
	 * Whether the first value is less than or equal to the second value.
	 * 
	 * @see SQLOp#lt
	 */
	// <=
	le,

	/**
	 * Whether the first value is greater than or equal to the second value.
	 * 
	 * @see SQLOp#ge
	 */
	// >=
	ge;

}
