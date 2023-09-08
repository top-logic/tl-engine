/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.transform;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultDescendingQueryVisitor;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.KBQuery;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.compile.eval.CompiledExpression;
import com.top_logic.model.search.expr.compile.eval.InterpretedExpression;
import com.top_logic.model.search.expr.compile.eval.Value;
import com.top_logic.model.search.expr.interpreter.Rewriter;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;

/**
 * {@link Rewriter} that (partially) compiles {@link Filter#getFunction()} and injects the compiled
 * part of the filter function into the {@link SetExpression} of {@link KBQuery} access expressions.
 * 
 * <p>
 * As pre-requisite, the {@link CreateTableAccess} transformation must be executed.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FilterCompiler extends Rewriter<Void> {

	private final TypeContext _typeContext;

	/**
	 * Creates a {@link FilterCompiler}.
	 *
	 * @param typeContext
	 *        The table types.
	 */
	public FilterCompiler(TypeContext typeContext) {
		_typeContext = typeContext;
	}

	@Override
	public SearchExpression visitFilter(Filter expr, Void arg) {
		SearchExpression base = expr.getBase();
		String tableName = base.visit(GetTableType.INSTANCE, null);
		if (tableName != null) {
			SearchExpression testFunction = expr.getFunction();
			Object varName = testFunction.visit(FirstArg.INSTANCE, 0);
			if (varName == null) {
				throw new IllegalArgumentException("Invalid filter function, no free variable found: " + testFunction);
			}
			MetaObject tableType;
			try {
				tableType = _typeContext.getType(tableName);
			} catch (UnknownTypeException ex) {
				throw new IllegalArgumentException("Unknown table type in: " + base, ex);
			}
			Value evaluator = testFunction.visit(ExpressionCompiler.INSTANCE, new VarBinding(varName, tableType));
			if (evaluator.hasCompiledPart()) {
				Expression compiled = evaluator.compiled();

				SearchExpression enhancedBase = base.visit(InjectCompiled.INSTANCE, compiled);
				if (evaluator.hasInterpretedPart()) {
					SearchExpression subExpression = evaluator.interpreted();
					return SearchExpressionFactory.filter(enhancedBase,
						SearchExpressionFactory.lambda(varName, subExpression));
				} else {
					return enhancedBase;
				}
			}
		}
		return super.visitFilter(expr, arg);
	}

	private static class InjectCompiled extends Rewriter<Expression> {
		/**
		 * Singleton {@link FilterCompiler.InjectCompiled} instance.
		 */
		public static final FilterCompiler.InjectCompiled INSTANCE = new FilterCompiler.InjectCompiled();

		private InjectCompiled() {
			// Singleton constructor.
		}

		@Override
		public SearchExpression visitFilter(Filter expr, Expression arg) {
			// Only transform into base.
			return composeFilter(expr, arg, descendPart(expr, arg, expr.getBase()), expr.getFunction());
		}

		@Override
		public SearchExpression visitForeach(Foreach expr, Expression arg) {
			throw new UnreachableAssertion("Foreach was not considered during filter optimization, see GetTableType.");
		}

		@Override
		public SearchExpression visitAll(All expr, Expression arg) {
			throw new UnreachableAssertion("Meta-model access should have been eliminated before.");
		}

		@Override
		public SearchExpression visitKBQuery(KBQuery expr, Expression arg) {
			return SearchExpressionFactory.query(expr.getClassType(), ExpressionFactory.filter(expr.getQuery(), arg));
		}
	}

	private static class VarBinding {

		private final Object _varName;

		private final MetaObject _tableType;

		public VarBinding(Object varName, MetaObject tableType) {
			_varName = varName;
			_tableType = tableType;
		}

		public Object getVarName() {
			return _varName;
		}

		public MetaObject getTableType() {
			return _tableType;
		}
	}

	private static class ExpressionCompiler extends DefaultDescendingVisitor<Value, VarBinding> {

		/**
		 * Singleton {@link FilterCompiler.ExpressionCompiler} instance.
		 */
		public static final FilterCompiler.ExpressionCompiler INSTANCE = new FilterCompiler.ExpressionCompiler();

		private ExpressionCompiler() {
			// Singleton constructor.
		}

		@Override
		protected Value composeAnd(And expr, VarBinding arg, Value leftResult, Value rightResult) {
			return leftResult.processAnd(expr, rightResult);
		}

		@Override
		protected Value composeOr(Or expr, VarBinding arg, Value leftResult, Value rightResult) {
			return leftResult.processOr(expr, rightResult);
		}

		@Override
		protected Value composeNot(Not expr, VarBinding arg, Value argumentResult) {
			return argumentResult.processNot(expr);
		}

		@Override
		protected Value composeEquals(IsEqual expr, VarBinding arg, Value leftResult, Value rightResult) {
			return leftResult.processEquals(expr, rightResult);
		}

		@Override
		protected Value composeAccess(Access expr, VarBinding arg, Value selfResult) {
			return selfResult.processAccess(expr, expr.getPart());
		}

		@Override
		public Value visitLambda(Lambda expr, VarBinding arg) {
			if (arg.getVarName().equals(expr.getName())) {
				// Reduce the filter function to its body. The function is re-assembled afterwards.
				return expr.getBody().visit(this, arg);
			}
			return super.visitLambda(expr, arg);
		}

		@Override
		public Value visitLiteral(Literal expr, VarBinding arg) {
			return Value.literal(expr, expr.getValue());
		}

		@Override
		public Value visitVar(Var expr, VarBinding arg) {
			if (expr.getName().equals(arg.getVarName())) {
				return new CompiledExpression(arg.getTableType(), ExpressionFactory.context());
			}
			return super.visitVar(expr, arg);
		}

		@Override
		protected Value compose(SearchExpression expr, VarBinding arg, Value result) {
			// By default, treat all expressions as interpreted.
			return new InterpretedExpression(expr);
		}

	}

	/**
	 * Finds the {@link Var#getName() name} of the variable, the first argument of the visited
	 * {@link SearchExpression} is bound to, or <code>null</code> if the visited expression does no
	 * variable binding.
	 */
	private static class FirstArg extends DefaultDescendingVisitor<Object, Integer> {
		/**
		 * Singleton {@link FilterCompiler.FirstArg} instance.
		 */
		public static final FilterCompiler.FirstArg INSTANCE = new FilterCompiler.FirstArg();

		private FirstArg() {
			// Singleton constructor.
		}

		@Override
		public Object visitLambda(Lambda expr, Integer bindings) {
			if (bindings > 0) {
				return expr.getBody().visit(this, bindings - 1);
			} else {
				return expr.getName();
			}
		}

		@Override
		public Object visitCall(Call expr, Integer bindings) {
			return expr.getFunction().visit(this, bindings + 1);
		}
	}

	static class GetTableType extends DefaultDescendingVisitor<String, Void> {

		public static final GetTableType INSTANCE = new GetTableType();

		private GetTableType() {
			// Singleton constructor.
		}

		@Override
		public String visitKBQuery(KBQuery expr, Void arg) {
			return expr.getQuery().visitQuery(QueryType.INSTANCE, arg);
		}

		@Override
		protected String descendPart(SearchExpression expr, Void arg, SearchExpression part) {
			// Do never descend into the self-part of any other expression than the ones listed
			// here.
			return null;
		}

		@Override
		public String visitFilter(Filter expr, Void arg) {
			return expr.getBase().visit(this, arg);
		}

		/**
		 * Retrieves the unique table name that is accessed by a certain {@link SetExpression}, or
		 * <code>null</code> if unknown or not unique.
		 */
		static class QueryType extends DefaultDescendingQueryVisitor<String, String, String, String, String, Void> {

			@SuppressWarnings("hiding")
			public static final GetTableType.QueryType INSTANCE = new GetTableType.QueryType();

			private QueryType() {
				// Singleton constructor.
			}

			@Override
			public String visitAllOf(AllOf expr, Void arg) {
				return expr.getTypeName();
			}

			@Override
			public String visitFilter(com.top_logic.knowledge.search.Filter expr, Void arg) {
				return expr.getSource().visitQuery(this, arg);
			}
		}

	}

}
