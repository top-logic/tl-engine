/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.List;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;
import com.top_logic.knowledge.search.Expression;

/**
 * Delegates the computation of the ranges to an inner {@link LifePeriodComputation} iff the
 * represented {@link Expression} takes part on the {@link LifePeriodComputation}.
 * 
 * <p>
 * Such an {@link LifePeriodComputation} is necessary for the sub results of OR-expression, e.g.:
 * The result set may contain a join with two different tables (introduced by the OR). The correct
 * life period is the life period of that part of the result which causes the line to be in the
 * result. This can not be determined when just the result is used.
 * </p>
 * 
 * @see Context#getOracleResult(int)
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public final class LifePeriodOracle implements LifePeriodComputation {

	private final int expressionIndex;
	private final LifePeriodComputation innerComputation;

	/**
	 * Creates a new {@link LifePeriodOracle}.
	 * 
	 * @param expressionIndex
	 *        Index of the corresponding expression.
	 * @param innerComputation
	 *        {@link LifePeriodComputation}
	 */
	public LifePeriodOracle(int expressionIndex, LifePeriodComputation innerComputation) {
		this.expressionIndex = expressionIndex;
		this.innerComputation = innerComputation;
	}

	@Override
	public List<LongRange> computeRanges(Context context) {
		if (context.getOracleResult(expressionIndex)) {
			return innerComputation.computeRanges(context);
		} else {
			return LongRangeSet.EMPTY_SET;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Oracle [index=");
		builder.append(expressionIndex);
		builder.append(", computation=");
		builder.append(innerComputation);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + expressionIndex;
		result = prime * result + innerComputation.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LifePeriodOracle other = (LifePeriodOracle) obj;
		if (expressionIndex != other.expressionIndex)
			return false;
		return innerComputation.equals(other.innerComputation);
	}

}
