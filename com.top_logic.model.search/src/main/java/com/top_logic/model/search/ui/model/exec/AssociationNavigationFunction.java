/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.exec;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;

/**
 * A {@link Function1} that builds a {@link SearchExpression} to navigate a {@link TLAssociation}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class AssociationNavigationFunction extends Function1<SearchExpression, SearchExpression> {

	private final TLAssociationEnd _incomingEnd;

	private final TLAssociationEnd _outgoingEnd;

	AssociationNavigationFunction(TLAssociationEnd incomingEnd, TLAssociationEnd outgoingEnd) {
		_incomingEnd = incomingEnd;
		_outgoingEnd = outgoingEnd;
	}

	@Override
	public SearchExpression apply(SearchExpression baseValue) {
		return associationNavigation(baseValue, _incomingEnd, _outgoingEnd);
	}

}
