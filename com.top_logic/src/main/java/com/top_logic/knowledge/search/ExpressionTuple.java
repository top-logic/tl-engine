/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import java.util.List;

/**
 * Tuple of {@link Expression}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ExpressionTuple extends Expression {

	private List<Expression> expressions;

	/*package protected*/ ExpressionTuple(List<Expression> expressions) {
		this.expressions = expressions;
	}
	
	public List<Expression> getExpressions() {
		return expressions;
	}
	
	public void setExpressions(List<Expression> expressions) {
		this.expressions = expressions;
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitTuple(this, arg);
	}

}
