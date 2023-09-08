/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.Collection;


/**
 * Non-empty literal set of values.
 * 
 * @see None The literal empty set.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SetLiteral extends SetExpression {

	private final Collection<? extends Object> values;

	SetLiteral(Collection<? extends Object> values) {
		this.values = values;
	}
	
	public Collection<? extends Object> getValues() {
		return values;
	}
	
	@Override
	public <R, A> R visitSetExpr(SetExpressionVisitor<R, A> v, A arg) {
		return v.visitSetLiteral(this, arg);
	}

}
