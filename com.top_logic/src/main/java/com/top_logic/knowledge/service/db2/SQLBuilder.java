/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.sql.CollationHint;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.knowledge.search.Attribute;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.ContextAccess;
import com.top_logic.knowledge.search.Eval;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.Flex;
import com.top_logic.knowledge.search.GetEntry;
import com.top_logic.knowledge.search.HasType;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.InstanceOf;
import com.top_logic.knowledge.search.IsCurrent;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.OrderSpec;
import com.top_logic.knowledge.search.OrderTuple;
import com.top_logic.knowledge.search.OrderVisitor;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.Reference;
import com.top_logic.knowledge.search.RequestedHistoryContext;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.search.SetLiteral;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;
import com.top_logic.knowledge.service.db2.expr.visit.LiteralValue;

/**
 * {@link Expression} to {@link SQL} compiler.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLBuilder implements ExpressionVisitor<Object, SQLBuilder.SQLBuffer>, OrderVisitor<Object, SQLBuilder.SQLBuffer> {

	/**
	 * Mutable {@link SQL} implementation.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class SQLBuffer implements SQL, Appendable {
		
		final DBHelper sqlDialect;
		
		/**
		 * @see #getTableName()
		 */
		final String tableName;
		
		/**
		 * The SQL source buffer.
		 */
		StringBuilder buffer = new StringBuilder();
		
		/**
		 * Current arguments of the SQL {@link #getSource()}
		 */
		ArrayList<Argument> arguments = new ArrayList<>();
		
		/**
		 * Creates a {@link SQLBuffer}.
		 *
		 * @param sqlDialect The SQL dialect in which the statement is constructed.
		 * @param tableName See {@link #getTableName()}
		 */
		public SQLBuffer(DBHelper sqlDialect, String tableName) {
			this.sqlDialect = sqlDialect;
			this.tableName  = tableName;
		}
		
		@Override
		public List<Argument> getArguments() {
			return arguments;
		}
		
		@Override
		public String getSource() {
			return buffer.toString();
		}
		
		/**
		 * Use {@link #getSource()}.
		 */
		@Override
		public String toString() {
		    return getSource();
		}

		/**
		 * The (alias) name of the table that should be used to qualify
		 * attribute access.
		 */
		public String getTableName() {
			return tableName;
		}

		/**
		 * Adds a new argument to the query. 
		 * 
		 * @param argument The argument to add.
		 * 
		 * @see #getArguments()
		 */
		public final void addArgument(Argument argument) {
			arguments.add(argument);
		}

		@Override
		public final Appendable append(CharSequence sql) {
			buffer.append(sql);
			return this;
		}

		@Override
		public Appendable append(char ch) {
			buffer.append(ch);
			return this;
		}

		@Override
		public Appendable append(CharSequence sql, int start, int end) {
			buffer.append(sql, start, end);
			return this;
		}
	}

	private static final SQLBuilder INSTANCE = new SQLBuilder();

	private static HashMap<Operator, String> SQL_SYMBOLS_BY_OPERATOR;
	static {
		HashMap<Operator, String> map = new HashMap<>();
		map.put(Operator.AND, "AND");
		map.put(Operator.OR, "OR");
		map.put(Operator.NOT, "NOT");
		map.put(Operator.IS_NULL, "IS NULL");
		
		map.put(Operator.EQBINARY, "=");
		map.put(Operator.EQCI, "=");
		map.put(Operator.GT, ">");
		map.put(Operator.GE, ">=");
		map.put(Operator.LT, "<");
		map.put(Operator.LE, "<=");
		map.put(Operator.LE, "<=");
		
		SQL_SYMBOLS_BY_OPERATOR = map;
	}
	
	private SQLBuilder() {
		// Singleton constructor.
	}
	
	@Override
	public Object visitAttribute(Attribute expr, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		
		String tableName = sql.getTableName();
		if (tableName != null) {
			buffer.append(tableName);
			buffer.append('.');
		}
		buffer.append(sql.sqlDialect.columnRef(((DBAttribute) expr.getAttribute()).getDBName()));
		
		return null;
	}

	@Override
	public Object visitLiteral(Literal expr, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		
		Object value = expr.getValue();
		if (value == null) {
			buffer.append("NULL");
		} else {
			buffer.append('?');
			
			MOPrimitive concreteType = MOPrimitive.getPrimitive(value.getClass());
			sql.arguments.add(new SQL.Argument(concreteType.getDefaultSQLType(), value));
		}
		return null;
	}
	
	@Override
	public Object visitParameter(Parameter expr, SQLBuffer sql) {
		// TODO: No value here.
		Object value = null;
		sql.arguments.add(new SQL.Argument(((MOPrimitive) expr.getDeclaredType()).getDefaultSQLType(), value));
		return null;
	}

	@Override
	public Object visitBinaryOperation(BinaryOperation expr, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		Expression left = expr.getLeft();
		Expression right = expr.getRight();
		Operator symbol = expr.getOperator();
		if (isEQ(symbol) && isNull(left)) {
			right.visit(this, sql);
			buffer.append(' ');
			buffer.append("IS");
			buffer.append(' ');
			left.visit(this, sql);
		} else if (isEQ(symbol) && isNull(right)) {
			left.visit(this, sql);
			buffer.append(' ');
			buffer.append("IS");
			buffer.append(' ');
			right.visit(this, sql);
		} else {
			boolean needLeftParenthesis = (left instanceof BinaryOperation) && (((BinaryOperation) left).getOperator() != symbol);
			boolean needRightParenthesis = (right instanceof BinaryOperation) && (((BinaryOperation) right).getOperator() != symbol);
			if (needLeftParenthesis) {
				buffer.append('(');
			}
			left.visit(this, sql);
			if (needLeftParenthesis) {
				buffer.append(')');
			}
			buffer.append(' ');
			buffer.append(SQL_SYMBOLS_BY_OPERATOR.get(symbol));
			buffer.append(' ');
			if (needRightParenthesis) {
				buffer.append('(');
			}
			right.visit(this, sql);
			if (needRightParenthesis) {
				buffer.append(')');
			}
		}
		
		return null;
	}

	private boolean isEQ(Operator symbol) {
		return symbol == Operator.EQBINARY | symbol == Operator.EQCI;
	}
	
	private static boolean isNull(Expression expr) {
		return LiteralValue.isLiteral(expr) && LiteralValue.getLiteralValue(expr) == null;
	}

	@Override
	public Object visitUnaryOperation(UnaryOperation expr, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		final Operator operator = expr.getOperator();
		switch (operator) {
			case NOT:
				buffer.append(SQL_SYMBOLS_BY_OPERATOR.get(operator));
				buffer.append(' ');
				expr.getExpr().visit(this, sql);
				break;

			case IS_NULL:
				expr.getExpr().visit(this, sql);
				buffer.append(' ');
				buffer.append(SQL_SYMBOLS_BY_OPERATOR.get(operator));
				break;
			case BRANCH:
			case REVISION:
			case HISTORY_CONTEXT:
			case IDENTIFIER:
			case TYPE_NAME:
				throw new UnsupportedOperationException("Unable to express in SQL");
			default:
				throw new UnreachableAssertion("Unknown " + Operator.class.getSimpleName() + ": " + operator);
		}
		return null;
	}
	
	@Override
	public Object visitIsCurrent(IsCurrent expr, SQLBuffer arg) {
		throw new UnsupportedOperationException("Unable to express in SQL.");
	}

	@Override
	public Object visitRequestedHistoryContext(RequestedHistoryContext expr, SQLBuffer arg) {
		throw new UnsupportedOperationException("Unable to express in SQL.");
	}

	@Override
	public Object visitHasType(HasType expr, SQLBuffer sql) {
		// TODO: Test for current table.
		StringBuilder buffer = sql.buffer;
		// SQL equivalent of true. The query must only be executed on tables
		// representing matching types.
		buffer.append("1=1");
		return null;
	}
	
	@Override
	public Object visitInstanceOf(InstanceOf expr, SQLBuffer sql) {
		// TODO: Test for current table.
		StringBuilder buffer = sql.buffer;
		// SQL equivalent of true. The query must only be executed on tables
		// representing matching types.
		buffer.append("1=1");
		return null;
	}
	
	@Override
	public Object visitTuple(ExpressionTuple tuple, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		List<Expression> expressions = tuple.getExpressions();
		for (int n = 0, cnt = expressions.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(", ");
			}
			expressions.get(n).visit(this, sql);
		}
		
		return null;
	}

	@Override
	public Object visitMatches(Matches expr, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		
		expr.getExpr().visit(this, sql);
		buffer.append(" LIKE ");
		buffer.append('"');
		buffer.append(ExpressionPrinter.quoteString(expr.getRegex()));
		buffer.append('"');
		
		return null;
	}
	
	@Override
	public Object visitOrderTuple(OrderTuple tuple, SQLBuffer sql) {
		StringBuilder buffer = sql.buffer;
		List<OrderSpec> orderSpecs = tuple.getOrderSpecs();
		for (int n = 0, cnt = orderSpecs.size(); n < cnt; n++) {
			if (n > 0) {
				buffer.append(", ");
			}
			orderSpecs.get(n).visitOrder(this, sql);
		}
		
		return null;
	}
	
	@Override
	public Object visitOrderSpec(OrderSpec orderSpec, SQLBuffer sql) {
		StringBuilder sqlBuffer = sql.buffer;
		
		// Analyze type of expression. Only a string typed expression must be
		// wrapped with a collation specification.
		CollationHint orderHint = orderSpec.getOrderExpr().visit(CollationHintComputation.INSTANCE, null);
		
		if (orderHint == CollationHint.NONE) {
			orderSpec.getOrderExpr().visit(this, sql);
		} else {
			// Create an order expression that can be made collation sensitive.
			String sqlExpr;
			{
				int start = sqlBuffer.length();
				orderSpec.getOrderExpr().visit(this, sql);
				sqlExpr = sqlBuffer.substring(start, sqlBuffer.length());
				sqlBuffer.setLength(start);
			}
			
			// Wrap expression with collation information.
			sql.sqlDialect.appendCollatedExpression(sqlBuffer, sqlExpr, orderHint);
		}
		
		if (orderSpec.isDescending()) {
			sqlBuffer.append(" DESC");
		} else {
			sqlBuffer.append(" ASC");
		}

		return null;
	}

	@Override
	public Object visitInSet(InSet expr, SQLBuffer sql) {
		SetExpression setExpr = expr.getSetExpr();
		if (setExpr instanceof SetLiteral) {
			SetLiteral setLiteral = (SetLiteral) setExpr;

			StringBuilder buffer = sql.buffer;

			expr.getContext().visit(this, sql);
			buffer.append(" IN ");

			Collection collection = setLiteral.getValues();
			buffer.append('(');
			Iterator it = collection.iterator();
			if (it.hasNext()) {
				while (true) {
					Object element = it.next();
					if (element instanceof TLID) {
						element = ((TLID) element).toStorageValue();
					}
					if (element instanceof String) {
						buffer.append('\'');
						buffer.append(ExpressionPrinter.quoteString((String) element));
						buffer.append('\'');
					} else {
						buffer.append(element);
					}
					if (it.hasNext()) {
						buffer.append(',');
					} else {
						break;
					}
				}
			}
			buffer.append(')');
			return null;
		}

		throw new IllegalArgumentException("Only literal sets can be directly translated to simple SQL expressions.");
	}
	
	@Override
	public Object visitGetEntry(GetEntry expr, SQLBuffer arg) {
		//  Complex expression that cannot be transformed into a SQL expression.
		throw new IllegalArgumentException("Tuple access cannot be translated to simle SQL expressions.");
	}

	@Override
	public Object visitFlex(Flex expr, SQLBuffer sql) {
		//  Complex expression that cannot be transformed into a SQL expression.
		throw new IllegalArgumentException("Flexible attribute access cannot be translated to simle SQL expressions.");
	}

	@Override
	public Object visitReference(Reference expr, SQLBuffer sql) {
		final ReferencePart columnType = expr.getAccessType();
		if (columnType == null) {
			// Complex expression that cannot be transformed into a SQL expression.
			throw new IllegalArgumentException("Navigation cannot be translated to simple SQL expressions.");
		}

		StringBuilder buffer = sql.buffer;

		String tableName = sql.getTableName();
		if (tableName != null) {
			buffer.append(tableName);
			buffer.append('.');
		}
		buffer.append(sql.sqlDialect.columnRef(expr.getColumn(columnType).getDBName()));
		return null;
	}
	
	@Override
	public Object visitEval(Eval expr, SQLBuffer sql) {
		//  Complex expression that cannot be transformed into a SQL expression.
		throw new IllegalArgumentException("Evaluation with changed context cannot be translated to simle SQL expressions except for eval(expr, in(SetExpression)).");
	}

	@Override
	public Object visitContext(ContextAccess expr, SQLBuffer arg) {
		//  Complex expression that cannot be transformed into a SQL expression.
		throw new IllegalArgumentException("Access to context object cannot be translated to simle SQL expressions.");
	}
	
	/**
	 * Factory method for {@link SQLBuffer}.
	 */
	public static SQLBuffer createBuffer(DBHelper sqlDialect, String tableName) {
		return new SQLBuffer(sqlDialect, tableName);
	}

	/**
	 * Append the given expression to the given buffer.
	 * 
	 * @param buffer
	 *        The buffer to append.
	 * @param expr
	 *        The expression to append.
	 * @return The given buffer to join calls.
	 */
	public static SQLBuffer appendExpression(SQLBuffer buffer, Expression expr) {
		if (expr != null) {
			expr.visit(INSTANCE, buffer);
		} else {
			buffer.buffer.append("1=1");
		}
		return buffer;
	}
	
	/**
	 * Append the given order expression to this buffer.
	 * 
	 * @see #appendExpression(SQLBuffer, Expression)
	 */
	public static SQLBuffer appendOrder(SQLBuffer buffer, Order expr) {
		expr.visitOrder(INSTANCE, buffer);
		return buffer;
	}

}
