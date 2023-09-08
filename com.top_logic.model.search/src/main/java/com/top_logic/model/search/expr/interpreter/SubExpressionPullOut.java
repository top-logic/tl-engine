/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.interpreter;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.NamedConstant;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.All;
import com.top_logic.model.search.expr.AssociationNavigation;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Flatten;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.Var;

/**
 * Inline Transformation that pulls subexpressions out of functions whose variables they do not use.
 * 
 * <p>
 * Must not be followed by {@link InlineLocals}, since this would revert the effect.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubExpressionPullOut {

	/**
	 * Transforms the given expression.
	 * 
	 * @param expr
	 *        The expression to transform. Must no longer be used after this method returns, since
	 *        parts may have been reused in the result.
	 * @return The transformed expression.
	 */
	public static SearchExpression transform(SearchExpression expr) {
		PullUp pullUp = new PullUp(expr);
		expr.visit(pullUp, null);
		return pullUp.getTop();
	}

	private static final class PullUp extends Rewriter<Void> {

		private final List<NamedConstant> _definitionKeys = new ArrayList<>();

		private final List<Lambda> _definitions = new ArrayList<>();

		private Set<NamedConstant> _usedVariables = new HashSet<>();

		private int nextVar;

		private SearchExpression _top;

		private Lambda _topDef;

		public PullUp(SearchExpression top) {
			_top = top;
		}

		public SearchExpression getTop() {
			return _top;
		}

		@Override
		protected SearchExpression descendPart(SearchExpression expr, Void arg, SearchExpression part) {
			return descendParts(expr, arg, part).get(0);
		}

		@Override
		protected List<SearchExpression> descendParts(SearchExpression expr, Void arg, SearchExpression... parts) {
			Set<NamedConstant> usageResult = _usedVariables;

			List<SearchExpression> result = newResult(parts);
			result.add(super.descendPart(expr, arg, parts[0]));
			for (int n = 1; n < parts.length; n++) {
				// Descend into each part with a fresh usage set.
				_usedVariables = new HashSet<>();
				result.add(super.descendPart(expr, arg, parts[n]));
				usageResult.addAll(_usedVariables);
			}

			_usedVariables = usageResult;

			return result;
		}

		@Override
		public SearchExpression visitLambda(Lambda expr, Void arg) {
			SearchExpression result;

			int index = _definitions.size();
			_definitions.add(expr);
			_definitionKeys.add(expr.getKey());
			{
				result = super.visitLambda(expr, arg);
			}
			_definitions.remove(index);
			_definitionKeys.remove(index);

			return result;
		}

		@Override
		protected SearchExpression composeVar(Var expr, Void arg) {
			_usedVariables.add(expr.getKey());
			return keep(expr);
		}

		@Override
		protected SearchExpression composeLambda(Lambda expr, Void arg, SearchExpression bodyResult) {
			_usedVariables.remove(expr.getKey());
			return keep(expr);
		}

		@Override
		protected SearchExpression composeLiteral(Literal expr, Void arg) {
			return keep(expr);
		}

		private SearchExpression keep(SearchExpression expr) {
			return expr;
		}

		@Override
		protected SearchExpression composeAccess(Access expr, Void arg, SearchExpression selfResult) {
			return check(super.composeAccess(expr, arg, selfResult));
		}

		@Override
		protected SearchExpression composeAssociationNavigation(AssociationNavigation expr, Void arg,
				SearchExpression sourceResult) {
			return check(super.composeAssociationNavigation(expr, arg, sourceResult));
		}

		@Override
		protected SearchExpression composeAll(All expr, Void arg) {
			return check(super.composeAll(expr, arg));
		}

		@Override
		protected SearchExpression composeFilter(Filter expr, Void arg, SearchExpression baseResult,
				SearchExpression functionResult) {
			return check(super.composeFilter(expr, arg, baseResult, functionResult));
		}

		@Override
		protected SearchExpression composeFlatten(Flatten expr, Void arg, SearchExpression argumentResult) {
			return check(super.composeFlatten(expr, arg, argumentResult));
		}

		@Override
		protected SearchExpression composeForeach(Foreach expr, Void arg, SearchExpression baseResult,
				SearchExpression functionResult) {
			return check(super.composeForeach(expr, arg, baseResult, functionResult));
		}

		private SearchExpression check(SearchExpression expr) {
			int scopeSize = _definitions.size();
			int actualScopeIndex = scopeSize - 1;

			int targetScopeIndex = actualScopeIndex;
			while (targetScopeIndex >= 0 && !_usedVariables.contains(_definitionKeys.get(targetScopeIndex))) {
				targetScopeIndex--;
			}

			if (targetScopeIndex < actualScopeIndex) {
				// The current expression does not use any variables defined in any scope up to the
				// target scope. Therefore, the expression can be moved just within the body of the
				// innermost lambda in the current scope that defines a variable used by the current
				// expression.
				Object x = newName();

				SearchExpression oldBody = oldBody(targetScopeIndex);
				Lambda definition = lambda(x, oldBody);
				SearchExpression newBody = call(definition, expr);
				if (targetScopeIndex >= 0) {
					_definitions.get(targetScopeIndex).setBody(newBody);
					_definitions.set(targetScopeIndex, definition);
				} else {
					if (_topDef == null) {
						_top = newBody;
					} else {
						_topDef.setBody(newBody);
					}
					_topDef = definition;
				}

				Var placeholder = var(x);
				placeholder.setDef(definition);
				return placeholder;
			}
			
			return expr;
		}

		private SearchExpression oldBody(int targetScopeIndex) {
			return targetScopeIndex >= 0 ? _definitions.get(targetScopeIndex).getBody() : topBody();
		}

		private SearchExpression topBody() {
			return _topDef != null ? _topDef.getBody() : _top;
		}

		private Object newName() {
			return new NamedConstant("const" + (nextVar++));
		}
	}

}
