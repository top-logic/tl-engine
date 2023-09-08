/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * SQL functions that have implementations on all supported databases.
 * 
 * @see SQLFunction
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum SQLFun {

	/**
	 * 1-ary function fetching the minimum of all results of its argument.
	 * 
	 * <p>
	 * In contrast to {@link #least}, this function computes the minimum of all results of its
	 * argument in the result set.
	 * </p>
	 */
	min,

	/**
	 * 1-ary function fetching the minimum of all results of its argument.
	 * 
	 * <p>
	 * In contrast to {@link #greatest}, this function computes the max of all results of its
	 * argument in the result set.
	 * </p>
	 */
	max,

	/**
	 * N-ary function that returns the greatest of its arguments.
	 * 
	 * <p>
	 * In contrast to {@link #max}, this function computes the greatest of its argument in each
	 * result row.
	 * </p>
	 */
	greatest, 
	
	/**
	 * N-ary function that returns the least of its arguments.
	 * 
	 * <p>
	 * In contrast to {@link #min}, this function computes the least of its argument in each result
	 * row.
	 * </p>
	 */
	least,

	/**
	 * Function that returns <code>1</code>, if its argument is
	 * <code>true</code>, <code>0</code> otherwise.
	 */
	isTrue;
}
