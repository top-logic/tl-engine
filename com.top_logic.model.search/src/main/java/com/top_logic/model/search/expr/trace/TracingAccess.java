/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.trace;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.Access;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;

/**
 * Replacement for the {@link Access} function for tracing.
 */
class TracingAccess extends Access {
	/**
	 * Creates a {@link TracingAccess}.
	 */
	TracingAccess(SearchExpression self, TLStructuredTypePart part) {
		super(self, part);
	}

	@Override
	public Object lookupValue(EvalContext definitions, TLObject self, TLStructuredTypePart part) {
		TracingAccessRewriter.traceAccess(definitions, self, part);
		return super.lookupValue(definitions, self, part);
	}
}