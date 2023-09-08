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
 * Computes the life period of the row in the represented table.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public final class RowLifePeriod implements LifePeriodComputation {

	private final String _tableAlias;

	/**
	 * Creates a new {@link RowLifePeriod}.
	 * 
	 * @param tableAlias
	 *        value of {@link #getTableAlias()}.
	 */
	public RowLifePeriod(String tableAlias) {
		assert tableAlias != null;
		this._tableAlias = tableAlias;
	}
	
	/**
	 * The alias of the represented table.
	 */
	public String getTableAlias() {
		return _tableAlias;
	}

	@Override
	public List<LongRange> computeRanges(Context context) {
		Long minimumValidity = context.getMinimumValidity(_tableAlias);
		Long maximumValidity = context.getMaximumValidity(_tableAlias);
		if (minimumValidity != null) {
			assert maximumValidity != null;
			return LongRangeSet.range(minimumValidity.longValue(), maximumValidity.longValue());
		} else {
			assert maximumValidity == null;
			/* It is expected that a result for the represented table is absent if the table is the
			 * right part of a left join (so null, when there is no match in the right table for the
			 * left table).
			 * 
			 * Currently this can only occur during equality check of a flex attribute and no match
			 * for the attribute is found. But then that row is only included into result iff it is
			 * expected that there is no match which means that the attribute is null.
			 * 
			 * This results in an intersection of the life period of the object and the life period
			 * of the flex data (computed by this computation). */
			return LongRangeSet.FULL_SET;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RowLifePeriod [table=");
		builder.append(_tableAlias);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _tableAlias.hashCode();
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
		RowLifePeriod other = (RowLifePeriod) obj;
		return _tableAlias.equals(other._tableAlias);
	}

}
