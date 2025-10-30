/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.search.expr.interpreter;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Filter;
import com.top_logic.model.search.expr.Foreach;
import com.top_logic.model.search.expr.Lambda;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Expansion of short forms in a {@link SearchExpression} to the form that are actually intended.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AbbreviationExpander {

	/**
	 * Transforms the given expression.
	 * 
	 * @param expr
	 *        The expression to transform. Must no longer be used after this method returns, since
	 *        parts may have been reused in the result.
	 * @return The transformed expression.
	 */
	public static SearchExpression transform(SearchExpression expr) {
		return expr.visit(Expander.INSTANCE, null);
	}

	/**
	 * Inline transformation that expands abbreviations, i.e. short forms that are actually invalid
	 * to the function actually intended.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Expander<A> extends Rewriter<A> {

		/** Instance of {@link Expander}. */
		public static final Expander<Void> INSTANCE = new Expander<>();

		@Override
		protected SearchExpression composeFilter(Filter expr, A arg, SearchExpression baseResult,
				SearchExpression functionResult) {
			functionResult = replaceLiteralPartByAccess(functionResult, arg);

			return super.composeFilter(expr, arg, baseResult, functionResult);
		}

		private SearchExpression replaceLiteralPartByAccess(SearchExpression functionResult, A arg) {
			if (ConstantFolding.isLiteral(functionResult)) {
				Object literalValue = ConstantFolding.literalValue(functionResult);
				if (literalValue instanceof TLStructuredTypePart part) {
					String varName = "obj_synthetic";
					Lambda partAccess = lambda(varName, access(var(varName), part));
					functionResult = partAccess.visit(this, arg);
				}
			}
			return functionResult;
		}

		@Override
		protected SearchExpression composeForeach(Foreach expr, A arg, SearchExpression baseResult,
				SearchExpression functionResult) {
			functionResult = replaceLiteralPartByAccess(functionResult, arg);

			return super.composeForeach(expr, arg, baseResult, functionResult);
		}

	}

}

