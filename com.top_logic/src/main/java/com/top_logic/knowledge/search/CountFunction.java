/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * {@link Function} that computes the size of the context set.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CountFunction extends Function {

	/**
	 * @see ExpressionFactory#count()
	 */
	CountFunction() {
		// Default constructor.
	}
	
	@Override
	public <R, A> R visitFunction(FunctionVisitor<R, A> v, A arg) {
		return v.visitCount(this, arg);
	}

}
