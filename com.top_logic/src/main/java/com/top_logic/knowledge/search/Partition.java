/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Set of {@link #getRepresentative()} values of equivalence classes for a {@link #getSource()} set
 * partitioned according to a {@link #getEquivalence()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Partition extends SetExpression {

	private SetExpression source;
	private Expression equivalence;
	private Function representative;

	Partition(SetExpression source, Expression equivalence, Function representative) {
		this.source = source;
		this.equivalence = equivalence;
		this.representative = representative;
	}
	
	public SetExpression getSource() {
		return source;
	}
	
	public void setSource(SetExpression source) {
		this.source = source;
	}
	
	public Expression getEquivalence() {
		return equivalence;
	}
	
	public void setEquivalence(Expression equivalence) {
		this.equivalence = equivalence;
	}
	
	public Function getRepresentative() {
		return representative;
	}
	
	public void setRepresentative(Function representative) {
		this.representative = representative;
	}
	
	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitPartition(this, arg);
	}

}
