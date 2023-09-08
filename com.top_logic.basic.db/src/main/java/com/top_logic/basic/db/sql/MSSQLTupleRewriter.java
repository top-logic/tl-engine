/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.TupleFactory.Tuple;
import com.top_logic.basic.sql.DBType;

/**
 * {@link SQLVisitor} rewriting a {@link SQLPart} to remove tuple.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MSSQLTupleRewriter extends SQLCopy {

	private final SQLVisitor<SQLPart, SQLExpression> IN_VISITOR =
		new DefaultSQLVisitor<>() {

			@Override
			public SQLPart visitSQLSetLiteral(SQLSetLiteral sql, SQLExpression arg) {
				if (sql.getValues().isEmpty()) {
					return literalFalseLogical();
				}
				Iterator<?> values = sql.getValues().iterator();
				SQLExpression result = literalFalseLogical();
				do {
					SQLExpression rightExpr = createSQLExpression(sql, values.next());
					SQLExpression leftExpr;
					if (values.hasNext()) {
						leftExpr = arg;
					} else {
						leftExpr = copy(arg);
					}
					result =
						or(result,
							rewriter().handleBinaryTupleOperation(SQLOp.eq, leftExpr, rightExpr));
				} while (values.hasNext());
				return result;
			}

			private SQLExpression createSQLExpression(SQLSetLiteral sql, Object value) {
				SQLExpression sqlExpression;
				if (value instanceof SQLExpression) {
					sqlExpression = (SQLExpression) value;
				} else {
					Tuple tupleValue = (Tuple) value;
					SQLExpression[] expressions = new SQLExpression[tupleValue.size()];
					for (int i = 0; i < expressions.length; i++) {
						DBType jdbcType = sql.getTypes()[i];
						expressions[i] = literal(jdbcType, tupleValue.get(i));
					}
					sqlExpression = tuple(expressions);
				}
				return sqlExpression;
			}

			@Override
			public SQLPart visitSQLSetParameter(SQLSetParameter sql, SQLExpression arg) {
				SQLExpression fulfilledSetParam =
					inSet(arg, rewriter()._resolver.fillSetParameter(sql));
				return fulfilledSetParam.visit(rewriter(), none);
			}

			private MSSQLTupleRewriter rewriter() {
				return MSSQLTupleRewriter.this;
			}

			@Override
			protected SQLPart visitSQLExpression(SQLExpression sql, SQLExpression arg) {
				return inSet(arg, sql);
			}

			@Override
			protected SQLPart visitSQLPart(SQLPart sql, SQLExpression arg) {
				throw new UnsupportedOperationException();
			}
	};

	final SetParameterResolver _resolver;

	/**
	 * Removes {@link SQLTuple} from the given {@link SQLPart}.
	 * 
	 * @param part
	 *        The part potentially containing an {@link SQLTuple}.
	 * @param resolver
	 *        Callback that resolves {@link SQLSetParameter}.
	 * 
	 * @return A {@link SQLPart} logically identical to the given part but not containing
	 *         {@link SQLTuple}.
	 */
	public static SQLPart removeTuple(SQLPart part, SetParameterResolver resolver) {
		return part.visit(new MSSQLTupleRewriter(resolver), none);
	}

	MSSQLTupleRewriter(SetParameterResolver resolver) {
		_resolver = resolver;
	}

	@Override
	public SQLPart visitSQLBinaryExpression(SQLBinaryExpression sql, Void arg) {
		SQLExpression leftExpr = sql.getLeftExpr();
		if (isTuple(leftExpr)) {
			return handleBinaryTupleOperation(sql.getOp(), leftExpr, sql.getRightExpr());
		}
		return super.visitSQLBinaryExpression(sql, arg);
	}

	SQLExpression handleBinaryTupleOperation(SQLOp op, SQLExpression leftExpr, SQLExpression rightExpr) {
		List<SQLExpression> leftExpressions = asTuple(leftExpr).getExpressions();
		List<SQLExpression> rightExpressions = asTuple(rightExpr).getExpressions();
		int tupleSize = leftExpressions.size();
		switch (op) {
			case sub:
			case add:
			case div:
			case mul:
				throw new UnsupportedOperationException("Rewrite of arithmetic operation not yet implemented.");
			case eq: {
				SQLExpression result = literalTrueLogical();
				for (int i = 0; i < tupleSize; i++) {
					SQLExpression part = descendedEq(leftExpressions.get(i), rightExpressions.get(i));
					result = and(result, part);
				}
				return result;
			}
			case gt:
			case le:
			case lt:
			case ge: {
				SQLExpression result = literalFalseLogical();
				for (int i = 0; i < tupleSize; i++) {
					SQLExpression part = literalTrueLogical();
					for (int j = 0; j < i; j++) {
						and(part, descendedEq(copy(leftExpressions.get(j)), copy(rightExpressions.get(j))));
					}
					SQLExpression leftVisit = descend(leftExpressions.get(i));
					SQLExpression rightVisit = descend(rightExpressions.get(i));
					part = and(part, binaryExpression(op, leftVisit, rightVisit));
					result = or(result, part);
				}
				return result;
			}
			case or:
			case and:
				throw new IllegalArgumentException("No boolean operation with tuples.");
			default:
				throw new UnreachableAssertion("No suche operation: " + op);

		}
	}

	private SQLExpression descendedEq(SQLExpression left, SQLExpression right) {
		SQLExpression leftVisit = descend(left);
		SQLExpression rightVisit = descend(right);
		SQLExpression part = eq(leftVisit, rightVisit);
		return part;
	}

	private SQLExpression descend(SQLExpression part) {
		return (SQLExpression) part.visit(this, none);
	}

	private SQLTuple asTuple(SQLExpression expr) {
		return (SQLTuple) expr;
	}

	private boolean isTuple(SQLExpression expr) {
		return expr instanceof SQLTuple;
	}

	@Override
	public SQLPart visitSQLInSet(SQLInSet sql, Void arg) {
		SQLExpression expr = sql.getExpr();
		if (isTuple(expr)) {
			return sql.getValues().visit(IN_VISITOR, expr);
		}
		return super.visitSQLInSet(sql, arg);
	}

}
