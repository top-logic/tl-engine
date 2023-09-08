/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.exec;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.func.Function1;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredTypePart;

/**
 * A {@link Function1} that builds a {@link SearchExpression} to accessing a {@link TLClassPart}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
class AttributeAccessFunction extends Function1<SearchExpression, SearchExpression> {

	private final TLStructuredTypePart _attribute;

	AttributeAccessFunction(TLStructuredTypePart attribute) {
		_attribute = attribute;
	}

	@Override
	public SearchExpression apply(SearchExpression baseValue) {
		return access(baseValue, _attribute);
	}

}
