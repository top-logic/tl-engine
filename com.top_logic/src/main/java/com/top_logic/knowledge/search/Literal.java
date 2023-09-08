/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * A literal value in an {@link Expression}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Literal extends Expression {
	
	/**
	 * @see #getValue()
	 */
	private final Object value;
	
	/*package protected*/ Literal(Object value) {
		assert value != null : "#9479: Literals with value 'null' are not allowed.";
		if (value instanceof KnowledgeItem) {
			this.value = ((KnowledgeItem) value).tId();
		} else {
			this.value = value;
		}
	}
	
	/**
	 * The value of this literal.
	 * 
	 * @return never <code>null</code>.
	 */
	public Object getValue() {
		return value;
	}
	
	@Override
	public <R,A> R visit(ExpressionVisitor<R,A> v, A arg) {
		return v.visitLiteral(this, arg);
	}
}
