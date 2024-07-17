/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.And;
import com.top_logic.model.search.expr.ArithmeticExpr;
import com.top_logic.model.search.expr.Call;
import com.top_logic.model.search.expr.Compare;
import com.top_logic.model.search.expr.CompareOp;
import com.top_logic.model.search.expr.DynamicGet;
import com.top_logic.model.search.expr.DynamicInstanceOf;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.GetDay;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.IsEmpty;
import com.top_logic.model.search.expr.IsEqual;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Length;
import com.top_logic.model.search.expr.ListExpr;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.Not;
import com.top_logic.model.search.expr.Or;
import com.top_logic.model.search.expr.Round;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SingleElement;
import com.top_logic.model.search.expr.Singleton;
import com.top_logic.model.search.expr.Size;
import com.top_logic.model.search.expr.Union;
import com.top_logic.model.search.expr.Var;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;

/**
 * Inline transformation that directly computes operations on constant values.
 * 
 * <p>
 * Prerequisite: {@link InlineLocals} to drop functions that effectively do not use their arguments.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantFolding {

	/**
	 * Transforms the given expression.
	 * 
	 * @param expr
	 *        The expression to transform. Must no longer be used after this method returns, since
	 *        parts may have been reused in the result.
	 * @return The transformed expression.
	 */
	public static SearchExpression transform(SearchExpression expr) {
		return expr.visit(new Fold(), null);
	}

	private static class Fold extends Rewriter<Void> {

		private final Map<NamedConstant, Object> _bindings = new HashMap<>();

		private final List<SearchExpression> _stack = new ArrayList<>();

		Fold() {
			// Singleton constructor.
		}

		@Override
		protected SearchExpression composeCompareOp(CompareOp expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult) && isLiteral(rightResult)) {
				return literal(expr.compute(literalValue(leftResult), literalValue(rightResult)));
			}
			return super.composeCompareOp(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeCompare(Compare expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult) && isLiteral(rightResult)) {
				return literal(expr.compute(literalValue(leftResult), literalValue(rightResult)));
			}
			return super.composeCompare(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeRound(Round expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult) && isLiteral(rightResult)) {
				return literal(expr.compute(literalValue(leftResult), literalValue(rightResult)));
			}
			return super.composeRound(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeGetDay(GetDay expr, Void arg, SearchExpression argumentResult) {
			if (isLiteral(argumentResult)) {
				return literal(expr.compute(literalValue(argumentResult)));
			}
			return super.composeGetDay(expr, arg, argumentResult);
		}

		@Override
		protected SearchExpression composeArithmetic(ArithmeticExpr expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult) && isLiteral(rightResult)) {
				return literal(expr.compute(literalValue(leftResult), literalValue(rightResult)));
			}
			return super.composeArithmetic(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeNot(Not expr, Void arg, SearchExpression argumentResult) {
			if (isLiteral(argumentResult)) {
				Literal literal = (Literal) argumentResult;
				boolean value = SearchExpression.isTrue(literal.getValue());
				literal.setValue(Boolean.valueOf(!value));
				return argumentResult;
			}
			return super.composeNot(expr, arg, argumentResult);
		}

		@Override
		protected SearchExpression composeAnd(And expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult)) {
				boolean literalLeft = literalBoolean(leftResult);
				if (literalLeft) {
					return toBoolean(rightResult);
				} else {
					// Convert a literal of any other type to a boolean literal.
					return literal(Boolean.valueOf(literalLeft));
				}
			}

			if (isLiteral(rightResult)) {
				boolean literalRight = literalBoolean(rightResult);
				if (literalRight) {
					return toBoolean(leftResult);
				} else {
					return literal(Boolean.valueOf(literalRight));
				}
			}

			return super.composeAnd(expr, arg, leftResult, rightResult);
		}

		private SearchExpression toBoolean(SearchExpression result) {
			if (isLiteral(result)) {
				// Make sure, no non-boolean type is ever returned from an and combinator.
				return literal(Boolean.valueOf(literalBoolean(result)));
			} else {
				try {
					MethodBuilder<?> toBooleanBuilder = SearchBuilder.getInstance().getBuilder("toBoolean");
					if (toBooleanBuilder == null) {
						throw new IllegalStateException("Missing built-in method 'toBoolean'.");
					}
					return toBooleanBuilder.build(null, new SearchExpression[] { result });
				} catch (ConfigurationException ex) {
					throw new ConfigurationError(ex);
				}
			}
		}

		@Override
		protected SearchExpression composeOr(Or expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult)) {
				boolean literalLeft = literalBoolean(leftResult);
				if (!literalLeft) {
					return rightResult;
				} else {
					return leftResult;
				}
			}

			// Note: Must not take rightResult, if it is a literal and considered to be true. This
			// would break expressions like $x || $y || "unknown" where one expects the first
			// non-empty value to be retrieved.

			return super.composeOr(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeFilter(Filter expr, Void arg, SearchExpression baseResult,
				SearchExpression functionResult) {
			if (isLiteral(functionResult)) {
				if (literalBoolean(functionResult)) {
					// Drop filter.
					return baseResult;
				} else {
					// Drop base.
					return literal(Collections.emptySet());
				}
			} else if (isLiteral(baseResult)) {
				if (isEmptyCollection(baseResult)) {
					// Drop filter.
					return baseResult;
				}
			}

			return super.composeFilter(expr, arg, baseResult, functionResult);
		}

		@Override
		protected SearchExpression composeUnion(Union expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult)) {
				if (isEmptyCollection(leftResult)) {
					return rightResult;
				} else if (isLiteral(rightResult)) {
					Set<Object> union = new HashSet<>();
					union.addAll(literalCollection(leftResult));
					union.addAll(literalCollection(rightResult));
					return literal(union);
				}
			} else if (isLiteral(rightResult)) {
				if (isEmptyCollection(rightResult)) {
					return leftResult;
				}
			}

			return super.composeUnion(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeLength(Length expr, Void arg, SearchExpression stringResult) {
			if (isLiteral(stringResult)) {
				return literal(expr.compute(literalValue(stringResult)));
			}
			return super.composeLength(expr, arg, stringResult);
		}

		@Override
		protected SearchExpression composeSize(Size expr, Void arg, SearchExpression listResult) {
			if (isLiteral(listResult)) {
				return literal(expr.compute(literalValue(listResult)));
			}
			if (listResult instanceof ListExpr) {
				return literal(SearchExpression.toNumber(((ListExpr) listResult).getElements().length));
			}
			return super.composeSize(expr, arg, listResult);
		}

		@Override
		protected SearchExpression composeList(ListExpr expr, Void arg, List<SearchExpression> elementsResult) {
			if (elementsResult.isEmpty()) {
				return literal(Collections.emptyList());
			}

			boolean allLiterals = true;
			for (SearchExpression element : elementsResult) {
				if (!isLiteral(element)) {
					allLiterals = false;
					break;
				}
			}
			if (allLiterals) {
				ArrayList<Object> value = new ArrayList<>(elementsResult.size());
				for (SearchExpression element : elementsResult) {
					value.add(literalValue(element));
				}
				return literal(Collections.unmodifiableList(value));
			}
			return super.composeList(expr, arg, elementsResult);
		}

		@Override
		protected SearchExpression composeIfElse(IfElse expr, Void arg, SearchExpression conditionResult,
				SearchExpression ifResult, SearchExpression elseResult) {
			if (isLiteral(conditionResult)) {
				boolean decision = literalBoolean(conditionResult);
				if (decision) {
					return ifResult;
				} else {
					return elseResult;
				}
			}
			return super.composeIfElse(expr, arg, conditionResult, ifResult, elseResult);
		}

		@Override
		protected SearchExpression composeEquals(IsEqual expr, Void arg, SearchExpression leftResult,
				SearchExpression rightResult) {
			if (isLiteral(leftResult) && isLiteral(rightResult)) {
				return literal(expr.compute(literalValue(leftResult), literalValue(rightResult)));
			}
			return super.composeEquals(expr, arg, leftResult, rightResult);
		}

		@Override
		protected SearchExpression composeSingleElement(SingleElement expr, Void arg, SearchExpression argumentResult) {
			if (isLiteral(argumentResult)) {
				return literal(expr.compute(literalValue(argumentResult)));
			}
			return super.composeSingleElement(expr, arg, argumentResult);
		}

		@Override
		protected SearchExpression composeSingleton(Singleton expr, Void arg, SearchExpression argumentResult) {
			if (isLiteral(argumentResult)) {
				return literal(Singleton.compute(literalValue(argumentResult)));
			}
			return super.composeSingleton(expr, arg, argumentResult);
		}

		@Override
		protected SearchExpression composeIsEmpty(IsEmpty expr, Void arg, SearchExpression argumentResult) {
			if (isLiteral(argumentResult)) {
				return literal(expr.compute(literalValue(argumentResult)));
			}
			return super.composeIsEmpty(expr, arg, argumentResult);
		}

		@Override
		protected SearchExpression composeGenericMethod(GenericMethod expr, Void arg,
				List<SearchExpression> argumentsResult) {
			if (!expr.isSideEffectFree()) {
				return super.composeGenericMethod(expr, arg, argumentsResult);
			}

			// Make model access static, if possible.
			if (expr instanceof DynamicGet) {
				SearchExpression property = argumentsResult.get(1);
				if (isLiteral(property)) {
					return access(argumentsResult.get(0), (TLStructuredTypePart) literalValue(property));
				} else {
					return super.composeGenericMethod(expr, arg, argumentsResult);
				}
			} else if (expr instanceof DynamicInstanceOf) {
				SearchExpression type = argumentsResult.get(1);
				if (isLiteral(type)) {
					return instanceOf(argumentsResult.get(0), (TLStructuredType) literalValue(type));
				} else {
					return super.composeGenericMethod(expr, arg, argumentsResult);
				}
			}

			for (SearchExpression argExpr : argumentsResult) {
				if (!isLiteral(argExpr)) {
					return super.composeGenericMethod(expr, arg, argumentsResult);
				}
			}

			int size = argumentsResult.size();
			Object[] arguments = new Object[size];
			for (int n = 0; n < size; n++) {
				arguments[n] = literalValue(argumentsResult.get(n));
			}

			if (expr.canEvaluateAtCompileTime(arguments)) {
				return literal(expr.evalAtCompileTime(arguments));
			} else {
				return super.composeGenericMethod(expr, arg, argumentsResult);
			}
		}

		@Override
		public SearchExpression visitCall(Call expr, Void arg) {
			SearchExpression argument = descendPart(expr, arg, expr.getArgument());
			push(argument);
			SearchExpression function = descendPart(expr, arg, expr.getFunction());
			SearchExpression top = pop();

			if (top == null) {
				return function;
			} else {
				// Keep function call.
				return composeCall(expr, arg, function, top);
			}
		}

		private SearchExpression pop() {
			return _stack.remove(_stack.size() - 1);
		}

		@Override
		public SearchExpression visitLambda(Lambda expr, Void arg) {
			if (_stack.isEmpty()) {
				// The result is a function value.
				return super.visitLambda(expr, arg);
			}

			SearchExpression top = pop();

			SearchExpression result;
			if (isLiteral(top)) {
				NamedConstant key = expr.getKey();

				_bindings.put(key, literalValue(top));
				super.visitLambda(expr, arg);
				_bindings.remove(key);

				// Remove call.
				result = expr.getBody();
				push(null);
			} else {
				// Keep call.
				result = super.visitLambda(expr, arg);

				push(top);
			}
			return result;
		}

		private void push(SearchExpression top) {
			_stack.add(top);
		}

		@Override
		protected SearchExpression composeVar(Var expr, Void arg) {
			NamedConstant key = expr.getKey();

			Object value = _bindings.get(key);
			if (value != null || _bindings.containsKey(key)) {
				return literal(value);
			}

			return super.composeVar(expr, arg);
		}
	}

	/**
	 * Whether the given {@link SearchExpression} represents a literal value.
	 */
	public static boolean isLiteral(SearchExpression result) {
		return result instanceof Literal;
	}

	/**
	 * The literal value represented by the given {@link SearchExpression} converted to boolean.
	 */
	public static boolean literalBoolean(SearchExpression leftResult) {
		return SearchExpression.isTrue(literalValue(leftResult));
	}

	/**
	 * Whether the literal value represented by the given {@link SearchExpression} can be
	 * interpreted as empty collection.
	 */
	public static boolean isEmptyCollection(SearchExpression baseResult) {
		return literalCollection(baseResult).isEmpty();
	}

	/**
	 * The literal value represented by the given {@link SearchExpression} as collection.
	 */
	public static Collection<?> literalCollection(SearchExpression leftResult) {
		return SearchExpression.asCollection(literalValue(leftResult));
	}

	/**
	 * The literal value represented by the given {@link SearchExpression}.
	 */
	public static Object literalValue(SearchExpression leftResult) {
		return ((Literal) leftResult).getValue();
	}
}
