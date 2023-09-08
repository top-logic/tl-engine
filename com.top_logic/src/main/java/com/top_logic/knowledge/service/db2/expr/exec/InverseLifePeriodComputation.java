/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import java.util.List;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;

/**
 * {@link LifePeriodComputation} that computes the inverted list of {@link LongRange}s of an inner
 * {@link LifePeriodComputation}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class InverseLifePeriodComputation implements LifePeriodComputation {

	/**
	 * Returns a {@link LifePeriodComputation} that computes the reverse list of {@link LongRange}s
	 * of the inner {@link LifePeriodComputation}.
	 */
	public static LifePeriodComputation invertLifePeriodComputation(LifePeriodComputation innerComputation) {
		if (innerComputation instanceof InverseLifePeriodComputation) {
			return ((InverseLifePeriodComputation) innerComputation)._innerComputation;
		}
		if (innerComputation == ForeverAlive.INSTANCE) {
			return NeverAlive.INSTANCE;
		}
		if (innerComputation == NeverAlive.INSTANCE) {
			return ForeverAlive.INSTANCE;
		}
		return new InverseLifePeriodComputation(innerComputation);
	}

	private final LifePeriodComputation _innerComputation;

	private InverseLifePeriodComputation(LifePeriodComputation innerComputation) {
		_innerComputation = innerComputation;
	}

	@Override
	public List<LongRange> computeRanges(Context context) {
		return LongRangeSet.invert(_innerComputation.computeRanges(context));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Inverse [");
		builder.append(_innerComputation);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _innerComputation.hashCode();
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
		InverseLifePeriodComputation other = (InverseLifePeriodComputation) obj;
		return _innerComputation.equals(other._innerComputation);
	}

}

