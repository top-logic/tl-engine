/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.expr.visit;

import java.util.List;

import com.top_logic.basic.col.TupleFactory;
import com.top_logic.knowledge.search.BinaryOperation;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionTuple;
import com.top_logic.knowledge.search.ExpressionVisitor;
import com.top_logic.knowledge.search.Literal;
import com.top_logic.knowledge.search.Matches;
import com.top_logic.knowledge.search.Operation;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.OperatorVisitor;
import com.top_logic.knowledge.search.Parameter;
import com.top_logic.knowledge.search.UnaryOperation;
import com.top_logic.util.Utils;

/**
 * Base class for interpreter implementations of {@link Expression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractExpressionEvaluator<EC extends AbstractExpressionEvaluator.ExpressionContext> implements ExpressionVisitor<Object, EC>, OperatorVisitor<Object, EC> {

	protected static abstract class ExpressionContext {

		private Operation operationContext;
		
		public ExpressionContext() {
			// Default constructor.
		}
		
		public Expression getArgument(int index) {
			return operationContext.getArgument(index);
		}

		public int getArgumentCount() {
			return operationContext.getArgumentCount();
		}
		
		public Operation setOperationContext(Operation operationContext) {
			Operation oldOperationContext = this.operationContext;
			this.operationContext = operationContext;
			return oldOperationContext;
		}
		
		public abstract Object getParameterValue(String name);
		
	}
	
	@Override
	public Object visitAnd(Operator operator, EC context) {
		for (int n = 0, cnt = context.getArgumentCount(); n < cnt; n++) {
			Expression expr = context.getArgument(n);
			Boolean exprResult = (Boolean) expr.visit(this, context);
			if (! exprResult.booleanValue()) {
				return Boolean.FALSE;
			}
		}
		
		return Boolean.TRUE;
	}
	
	@Override
	public Object visitOr(Operator operator, EC context) {
		for (int n = 0, cnt = context.getArgumentCount(); n < cnt; n++) {
			Expression expr = context.getArgument(n);
			Boolean exprResult = (Boolean) expr.visit(this, context);
			if (exprResult.booleanValue()) {
				return Boolean.TRUE;
			}
		}
		
		return Boolean.FALSE;
	}
	
	@Override
	public Object visitEqBinary(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		Expression right = context.getArgument(1);
		
		Object leftResult = left.visit(this, context);
		Object rightResult = right.visit(this, context);
		
		return Boolean.valueOf(Utils.equals(leftResult, rightResult));
	}
	
	@Override
	public Object visitEqCi(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		Expression right = context.getArgument(1);
		
		Object leftResult = left.visit(this, context);
		Object rightResult = right.visit(this, context);
		
		// TODO #18743/BHU: Currently it is not guaranteed that EQCI is only used for string valued
		// comparisons.
		boolean result;
		if (leftResult instanceof String && rightResult instanceof String) {
			result = ((String) leftResult).equalsIgnoreCase((String) rightResult);
		} else {
			result = Utils.equals(leftResult, rightResult);
		}
		return Boolean.valueOf(result);
	}
	
	@Override
	public Object visitGt(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		Expression right = context.getArgument(1);
		
		Object leftResult = left.visit(this, context);
		Object rightResult = right.visit(this, context);
		
		return Boolean.valueOf(compare(leftResult, rightResult) > 0);
	}

	private int compare(Object leftResult, Object rightResult) {
		if (leftResult == null) {
			throw new NullPointerException();
		}
		return ((Comparable) leftResult).compareTo(rightResult);
	}

	@Override
	public Object visitGe(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		Expression right = context.getArgument(1);
		
		Object leftResult = left.visit(this, context);
		Object rightResult = right.visit(this, context);
		
		return Boolean.valueOf(compare(leftResult, rightResult) >= 0);
	}
	
	@Override
	public Object visitLt(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		Expression right = context.getArgument(1);
		
		Object leftResult = left.visit(this, context);
		Object rightResult = right.visit(this, context);
		
		return Boolean.valueOf(compare(leftResult, rightResult) < 0);
	}

	@Override
	public Object visitLe(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		Expression right = context.getArgument(1);
		
		Object leftResult = left.visit(this, context);
		Object rightResult = right.visit(this, context);
		return Boolean.valueOf(compare(leftResult, rightResult) <= 0);
	}

	@Override
	public Object visitNot(Operator operator, EC context) {
		Expression left = context.getArgument(0);
		
		Object leftResult = left.visit(this, context);

		return Boolean.valueOf(! ((Boolean) leftResult).booleanValue());
	}

	@Override
	public Object visitIsNull(EC context) {
		Expression expr = context.getArgument(0);

		Object exprResult = expr.visit(this, context);

		return Boolean.valueOf(exprResult == null);
	}

	@Override
	public Object visitLiteral(Literal expr, EC context) {
		return expr.getValue();
	}
	
	@Override
	public Object visitParameter(Parameter expr, EC context) {
		return context.getParameterValue(expr.getName());
	}
	
	@Override
	public Object visitBinaryOperation(BinaryOperation expr, EC context) {
		Operation oldContext = context.setOperationContext(expr);

		Object result = expr.getOperator().visit(this, context);
		
		context.setOperationContext(oldContext);
		return result;
	}

	@Override
	public Object visitUnaryOperation(UnaryOperation expr, EC context) {
		Operation oldContext = context.setOperationContext(expr);

		Object result = expr.getOperator().visit(this, context);
		
		context.setOperationContext(oldContext);
		return result;
	}

	@Override
	public Object visitTuple(ExpressionTuple expr, EC context) {
		List<Expression> expressions = expr.getExpressions();
		int size = expressions.size();
		Object[] result = new Object[size];
		for (int n = 0; n < size; n++) {
			result[n] = expressions.get(n).visit(this, context);
		}
		return TupleFactory.newTuple(result);
	}
	
	@Override
	public Object visitMatches(Matches expr, EC arg) {
		String result = (String) expr.visit(this, arg);
		return Boolean.valueOf(result.matches(expr.getRegex()));
	}

}
