/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.exec;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.TLReference;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;

/**
 * A {@link Function1} that builds a {@link SearchExpression} to navigate an incoming
 * {@link TLReference}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class IncomingReferenceNavigationFunction extends Function1<SearchExpression, SearchExpression> {

	private final TLReference _incomingReference;

	IncomingReferenceNavigationFunction(TLReference incomingReference) {
		_incomingReference = incomingReference;
	}

	@Override
	public SearchExpression apply(SearchExpression baseValue) {
		return SearchExpressionFactory.referers(baseValue, _incomingReference);
	}
}
