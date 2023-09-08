/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.sym;

import com.top_logic.basic.db.sql.SQLExpression;

/**
 * Base class for {@link Symbol}s that read data from a database row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DataSymbol extends Symbol {

	/**
	 * Checks whether the data represented by this symbol has an own life period. In such case
	 * {@link #createRevMaxExpr()} represents the maximum revision and {@link #createRevMinExpr()}
	 * represents the minimum revision in which the data are valid.
	 * 
	 * @see DataSymbol#createRevMaxExpr()
	 * @see DataSymbol#createRevMinExpr()
	 */
	boolean hasOwnLifetime();
	
	SQLExpression createTypeExpr();
	SQLExpression createIdentifierExpr();
	SQLExpression createBranchExpr();

	/**
	 * Creates an expression that represents the minimum revision in which the represented data are
	 * valid.
	 */
	SQLExpression createRevMinExpr();

	/**
	 * Creates an expression that represents the maximum revision (inclusive) in which the
	 * represented data are valid.
	 */
	SQLExpression createRevMaxExpr();

	/**
	 * Whether the represented data is potentially <code>null</code>, also if it is actually
	 * declared as non null. This may happen if the represented data is contained as right part of a
	 * join.
	 */
	boolean isPotentiallyNull();

	/**
	 * Sets value of {@link #isPotentiallyNull()}
	 * 
	 * @see DataSymbol#isPotentiallyNull()
	 */
	void setPotentiallyNull(boolean potentiallyNull);

}
