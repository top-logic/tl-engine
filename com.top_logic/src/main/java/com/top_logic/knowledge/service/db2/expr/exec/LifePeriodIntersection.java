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
 * Intersection of two {@link LifePeriodComputation}.
 * 
 * <p>
 * Computes a list of {@link LongRange} such that each {@link LongRange} in the list is a sub range
 * of a {@link LongRange} of the ranges computed by the intersected computations.
 * </p>
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public final class LifePeriodIntersection implements LifePeriodComputation {

	/**
	 * Intersects the given {@link LifePeriodComputation}s.
	 */
	public static LifePeriodComputation lifePeriodIntersection(LifePeriodComputation left, LifePeriodComputation right) {
		if (left == ForeverAlive.INSTANCE) {
			return right;
		}
		if (right == ForeverAlive.INSTANCE) {
			return left;
		}
		if (left == NeverAlive.INSTANCE || right == NeverAlive.INSTANCE) {
			return NeverAlive.INSTANCE;
		}
		if (left.equals(right)) {
			return left;
		}
		return new LifePeriodIntersection(left, right);
	}

	private final LifePeriodComputation left;
	private final LifePeriodComputation right;

	private LifePeriodIntersection(LifePeriodComputation left, LifePeriodComputation right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public List<LongRange> computeRanges(Context context) {
		List<LongRange> leftPeriod = left.computeRanges(context);
		if (leftPeriod.isEmpty()) {
			return leftPeriod;
		}

		List<LongRange> rightPeriod = right.computeRanges(context);
		if (rightPeriod.isEmpty()) {
			return rightPeriod;
		}
		
		return LongRangeSet.intersect(leftPeriod, rightPeriod);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Cut [");
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
		LifePeriodIntersection other = (LifePeriodIntersection) obj;
		if (!left.equals(other.left))
			return false;
		if (!right.equals(other.right))
			return false;
		return true;
	}

}
