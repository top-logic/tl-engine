/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;
import static com.top_logic.model.search.expr.visit.Copy.*;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.model.search.expr.IfElse;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressions;
import com.top_logic.model.search.expr.StringStartsWith;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link MethodBuilder} creating {@link StringStartsWith} expressions.
 * 
 * @see SearchExpressions#stringStartsWith(SearchExpression, SearchExpression, boolean)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringStartsWithBuilder extends ThreeArgsOptionalBooleanMethodBuilder<IfElse> {

	/**
	 * Creates a {@link StringStartsWithBuilder}.
	 */
	public StringStartsWithBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override IfElse internalBuild(Expr expr, SearchExpression arg0, SearchExpression arg1,
			SearchExpression arg2) {
		return ifElse(arg2, stringStartsWith(arg0, arg1, true), stringStartsWith(copy(arg0), copy(arg1), false));
	}

}
