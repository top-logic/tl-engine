/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.exec;

import static com.top_logic.basic.db.sql.SQLFactory.*;
import static com.top_logic.knowledge.service.db2.expr.exec.LifePeriodIntersection.*;
import static com.top_logic.knowledge.service.db2.expr.exec.LifePeriodUnion.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.db.sql.DefaultSQLVisitor;
import com.top_logic.basic.db.sql.SQLBinaryExpression;
import com.top_logic.basic.db.sql.SQLBoolean;
import com.top_logic.basic.db.sql.SQLCase;
import com.top_logic.basic.db.sql.SQLColumnDefinition;
import com.top_logic.basic.db.sql.SQLColumnReference;
import com.top_logic.basic.db.sql.SQLExpression;
import com.top_logic.basic.db.sql.SQLFun;
import com.top_logic.basic.db.sql.SQLFunction;
import com.top_logic.basic.db.sql.SQLInSet;
import com.top_logic.basic.db.sql.SQLIsNull;
import com.top_logic.basic.db.sql.SQLLiteral;
import com.top_logic.basic.db.sql.SQLNot;
import com.top_logic.basic.db.sql.SQLOp;
import com.top_logic.basic.db.sql.SQLParameter;
import com.top_logic.basic.db.sql.SQLPart;
import com.top_logic.basic.db.sql.SQLSetLiteral;
import com.top_logic.basic.db.sql.SQLSetParameter;

/**
 * Returns a {@link LifePeriodComputation} that computes the {@link LongRange}s of the result of the
 * visited {@link SQLPart}.
 * 
 * @author <a href=mailto:bhu@top-logic.com>bhu</a>
 */
public final class LifePeriodComputationBuilder extends DefaultSQLVisitor<LifePeriodComputation, Boolean> {
	
	private List<SQLColumnDefinition> oracleColumns = new ArrayList<>();

	private final Collection<String> _foreverAliveTables;
	
	/**
	 * Returns a list of definitions of columns whose value is needed to by certain
	 * {@link LifePeriodComputation}s to compute the required {@link LongRange}s.
	 */
	public List<SQLColumnDefinition> getOracleColumns() {
		return oracleColumns;
	}

	public LifePeriodComputationBuilder(Collection<String> foreverAliveTables) {
		_foreverAliveTables = foreverAliveTables;
	}

	/**
	 * Builds the {@link LifePeriodComputation} for the given {@link SQLExpression}.
	 * 
	 * @param sql
	 *        The SQL to create {@link LifePeriodComputation} for.
	 */
	public LifePeriodComputation build(SQLExpression sql) {
		return sql.visit(this, false);
	}

	@Override
	public LifePeriodComputation visitSQLBinaryExpression(SQLBinaryExpression sql, Boolean inverted) {
		SQLExpression leftExpr = sql.getLeftExpr();
		SQLExpression rightExpr = sql.getRightExpr();
		
		LifePeriodComputation leftComputation = leftExpr.visit(this, inverted);
		LifePeriodComputation rightComputation = rightExpr.visit(this, inverted);
		
		if (rightComputation instanceof RowLifePeriod) {
			if (leftComputation instanceof RowLifePeriod) {
				String rightTable = ((RowLifePeriod) rightComputation).getTableAlias();
				String leftTable = ((RowLifePeriod) leftComputation).getTableAlias();
				if (leftTable.equals(rightTable)) {
					return leftComputation;
				}
			}
		}

		switch (sql.getOp()) {
		case and: 
		case or:
			if (inverted) {
				throw new AssertionError("Expression normalization must be computed before.");
			}
			break;
		default:
			// Ignore.
		}
		
		switch (sql.getOp()) {
		case or: {
			if (leftComputation == ForeverAlive.INSTANCE) {
				return ForeverAlive.INSTANCE;
			}
			if (rightComputation == ForeverAlive.INSTANCE) {
				return ForeverAlive.INSTANCE;
			}
			
			leftComputation = addOracle(leftExpr, leftComputation);
			rightComputation = addOracle(rightExpr, rightComputation);

				return lifePeriodUnion(leftComputation, rightComputation);
		}

		case and:
		default: {
			return lifePeriodIntersection(leftComputation, rightComputation);
		}
		}
	}

	private LifePeriodComputation addOracle(SQLExpression expr, LifePeriodComputation computation) {
		if (expr instanceof SQLBinaryExpression) {
			if (((SQLBinaryExpression) expr).getOp() != SQLOp.or) {
				SQLExpression oracleExpr = function(SQLFun.isTrue, copy(expr));
				
				int expressionIndex = oracleColumns.size();
				oracleColumns.add(columnDef(oracleExpr, null));
				
				computation = new LifePeriodOracle(expressionIndex, computation);
			}
		}
		return computation;
	}

	@Override
	public LifePeriodComputation visitSQLColumnReference(SQLColumnReference sql, Boolean inverted) {
		String tableAlias = sql.getTableAlias();
		if (_foreverAliveTables.contains(tableAlias)) {
			return ForeverAlive.INSTANCE;
		}
		return new RowLifePeriod(tableAlias);
	}
	
	@Override
	public LifePeriodComputation visitSQLFunction(SQLFunction sql, Boolean inverted) {
		List<SQLExpression> arguments = sql.getArguments();
		if (arguments.size() == 0) {
			return ForeverAlive.INSTANCE;
		}
		else if (arguments.size() == 1) {
			return arguments.get(0).visit(this, inverted);
		}
		else {
			LifePeriodComputation result = 
				lifePeriodIntersection(arguments.get(0).visit(this, inverted), arguments.get(1).visit(this, inverted));

			for (int n = 2, cnt = arguments.size(); n < cnt; n++) {
				result = lifePeriodIntersection(result, arguments.get(n).visit(this, inverted));
			}
			
			return result;
		}
	}
	
	@Override
	public LifePeriodComputation visitSQLIsNull(SQLIsNull sql, Boolean inverted) {
		return sql.getExpr().visit(this, inverted);
	}
	
	@Override
	public LifePeriodComputation visitSQLNot(SQLNot sql, Boolean inverted) {
		return sql.getExpr().visit(this, ! inverted);
	}
	
	@Override
	public LifePeriodComputation visitSQLBoolean(SQLBoolean sql, Boolean inverted) {
		return ForeverAlive.INSTANCE;
	}
	
	@Override
	public LifePeriodComputation visitSQLLiteral(SQLLiteral sql, Boolean inverted) {
		return ForeverAlive.INSTANCE;
	}
	
	@Override
	public LifePeriodComputation visitSQLParameter(SQLParameter sql, Boolean inverted) {
		return ForeverAlive.INSTANCE;
	}
	
	@Override
	public LifePeriodComputation visitSQLCase(SQLCase sql, Boolean arg) {
		throw new UnsupportedOperationException("Ticket #10036:");
	}

	@Override
	public LifePeriodComputation visitSQLSetParameter(SQLSetParameter sql, Boolean arg) {
		return ForeverAlive.INSTANCE;
	}

	@Override
	public LifePeriodComputation visitSQLInSet(SQLInSet sql, Boolean arg) {
		return lifePeriodIntersection(sql.getExpr().visit(this, arg), sql.getValues().visit(this, arg));
	}

	@Override
	public LifePeriodComputation visitSQLSetLiteral(SQLSetLiteral sql, Boolean arg) {
		return ForeverAlive.INSTANCE;
	}

	@Override
	protected LifePeriodComputation visitSQLPart(SQLPart sql, Boolean inverted) {
		throw new UnsupportedOperationException("Only expressions expected in WHERE, found: " + sql.getClass().getName());
	}
}