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
	
	/** The set of objects to partition. */
	public SetExpression getSource() {
		return source;
	}
	
	/** Setter for {@link #getSource()}. */
	public void setSource(SetExpression source) {
		this.source = source;
	}
	
	/** The expression whose value defines the equivalence classes of the partition. */
	public Expression getEquivalence() {
		return equivalence;
	}
	
	/** Setter for {@link #getEquivalence()}. */
	public void setEquivalence(Expression equivalence) {
		this.equivalence = equivalence;
	}
	
	/** The function computing the representative of each equivalence class. */
	public Function getRepresentative() {
		return representative;
	}
	
	/** Setter for {@link #getRepresentative()}. */
	public void setRepresentative(Function representative) {
		this.representative = representative;
	}
	
	@Override
	public <R,A> R visitSetExpr(SetExpressionVisitor<R,A> v, A arg) {
		return v.visitPartition(this, arg);
	}

}
