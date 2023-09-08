/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Expression} representing the history context of the request.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RequestedHistoryContext extends Expression {

	/**
	 * Creates a new {@link RequestedHistoryContext}.
	 */
	RequestedHistoryContext() {
	}

	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return v.visitRequestedHistoryContext(this, arg);
	}

}

