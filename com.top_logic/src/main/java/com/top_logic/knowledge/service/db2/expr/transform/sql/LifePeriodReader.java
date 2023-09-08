/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.transform.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.sql.ResultSetReader;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.db2.expr.exec.Context;
import com.top_logic.knowledge.service.db2.expr.exec.LifePeriodComputation;

/**
 * {@link ResultSetReader} that reads <code>rev_min</code>/<code>rev_max</code> column values into
 * {@link LongRange}s.
 * 
 *          com.top_logic.knowledge.service.db2.expr.transform.sql.SQLBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class LifePeriodReader implements ResultSetReader<List<LongRange>>, Context {
	private final LifePeriodComputation lifePeriodComputation;

	private int oracleExpressionResultOffset;
	
	private Map<String, Integer> _lifeRangeResultColumns;
	
	private ResultSet resultSet;

	public LifePeriodReader(LifePeriodComputation lifePeriodComputation, int oracleExpressionColumnOffset,
			Map<String, Integer> lifeRangeColumnOffset) {
		this.lifePeriodComputation = lifePeriodComputation;
		this.oracleExpressionResultOffset = oracleExpressionColumnOffset + 1;
		this._lifeRangeResultColumns = lifeRangeColumnOffset;
	}

	@Override
	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	@Override
	public List<LongRange> read() {
		return lifePeriodComputation.computeRanges(this);
	}
	
	@Override
	public boolean getOracleResult(int expressionIndex) {
		try {
			return resultSet.getBoolean(oracleExpressionResultOffset + expressionIndex);
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	@Override
	public Long getMinimumValidity(String tableAlias) {
		try {
			int tableOffset = _lifeRangeResultColumns.get(tableAlias);

			long revMin = resultSet.getLong(tableOffset + 1);
			if (resultSet.wasNull()) {
				return null;
			} else {
				return revMin;
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

	@Override
	public Long getMaximumValidity(String tableAlias) {
		try {
			int tableOffset = _lifeRangeResultColumns.get(tableAlias);

			long revMax = resultSet.getLong(tableOffset + 2);
			if (resultSet.wasNull()) {
				return null;
			} else {
				return revMax;
			}
		} catch (SQLException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}
}
