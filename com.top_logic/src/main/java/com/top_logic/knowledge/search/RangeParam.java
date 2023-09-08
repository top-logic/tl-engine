/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Specification of the requested result range of a query.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum RangeParam {

	/**
	 * Only the first result that matches the query is requested.
	 */
	first, 
	
	/**
	 * The first <code>n</code> rows are requested.
	 * 
	 * @see QueryArguments#getStopRow()
	 */
	head, 
	
	/**
	 * A range of result indices are requested.
	 */
	range, 
	
	/**
	 * All results are requested.
	 */
	complete;

	/**
	 * Throws an {@link AssertionError} that the given {@link RangeParam} does not exists.
	 */
	public static AssertionError unknownRangeParam(RangeParam rangeParam) {
		return new AssertionError("Unknown " + RangeParam.class.getName() + ": " + rangeParam);
	}
	
}
