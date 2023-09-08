/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Expression} for direct access to the context object of an
 * {@link Expression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContextAccess extends Expression {

	/**
	 * Creates a {@link ContextAccess}.
	 */
	ContextAccess() {
		// No properties.
	}
	
	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return v.visitContext(this, arg);
	}

}
