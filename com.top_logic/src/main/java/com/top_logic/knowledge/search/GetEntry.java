/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.basic.col.TupleFactory.Tuple;

/**
 * Access to an entry of a {@link Tuple} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GetEntry extends ContextExpression {

	private final int index;

	GetEntry(Expression context, int index) {
		super(context);
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public <R, A> R visit(ExpressionVisitor<R, A> v, A arg) {
		return v.visitGetEntry(this, arg);
	}

}
