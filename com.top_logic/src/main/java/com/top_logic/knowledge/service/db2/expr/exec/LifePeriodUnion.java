/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.List;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;

/**
 * Union of two {@link LifePeriodComputation}s.
 * 
 * <p>
 * Each {@link LongRange} of the united {@link LifePeriodComputation} is a sub range of a
 * {@link LongRange} in the result of this {@link LifePeriodUnion}.
 * </p>
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public final class LifePeriodUnion implements LifePeriodComputation {

	/**
	 * Joins the given {@link LifePeriodComputation}s.
	 */
	public static LifePeriodComputation lifePeriodUnion(LifePeriodComputation left, LifePeriodComputation right) {
		if (left == NeverAlive.INSTANCE) {
			return right;
		}
		if (right == NeverAlive.INSTANCE) {
			return left;
		}
		if (left == ForeverAlive.INSTANCE || right == ForeverAlive.INSTANCE) {
			return ForeverAlive.INSTANCE;
		}
		if (left.equals(right)) {
			return left;
		}
		return new LifePeriodUnion(left, right);
	}

	private final LifePeriodComputation left;
	private final LifePeriodComputation right;

	private LifePeriodUnion(LifePeriodComputation left, LifePeriodComputation right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public List<LongRange> computeRanges(Context context) {
		List<LongRange> period1 = left.computeRanges(context);
		List<LongRange> period2 = right.computeRanges(context);
		
		return LongRangeSet.union(period1, period2);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Union [");
		builder.append(left);
		builder.append(", ");
		builder.append(right);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + left.hashCode();
		result = prime * result + right.hashCode();
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
		LifePeriodUnion other = (LifePeriodUnion) obj;
		if (!left.equals(other.left))
			return false;
		if (!right.equals(other.right))
			return false;
		return true;
	}

}
