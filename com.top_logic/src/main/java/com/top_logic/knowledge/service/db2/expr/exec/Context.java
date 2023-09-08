/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;


/**
 * Context to access the database result.
 * 
 * @see LifePeriodComputation
 * 
 *          com.top_logic.knowledge.service.db2.expr.exec.LifePeriodComputation
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface Context {

	/**
	 * Determines the minimum revision in which the data of the represented table in the processed
	 * database result are valid.
	 * 
	 * @return <code>null</code> when no match for the given table is contained in the database
	 *         result.
	 */
	Long getMinimumValidity(String tableAlias);

	/**
	 * Determines the maximum revision (inclusive) in which the data of the represented table in the
	 * processed database result are valid.
	 * 
	 * @return <code>null</code> when no match for the given table is contained in the database
	 *         result.
	 */
	Long getMaximumValidity(String tableAlias);

	/**
	 * Determines whether the expression at the given index participate in the
	 * {@link LifePeriodComputation}s.
	 * 
	 * @param expressionIndex
	 *        Index of the expression under test.
	 * @return Whether the corresponding {@link LifePeriodComputation} must return non empty
	 *         result.
	 */
	boolean getOracleResult(int expressionIndex);

}
