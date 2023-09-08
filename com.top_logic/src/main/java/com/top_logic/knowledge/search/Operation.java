/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

/**
 * Application of an {@link Operator}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Operation extends Expression {
	
	/**
	 * @see #getOperator()
	 */
	private final Operator operator;
	
	/*package protected*/ Operation(Operator operator) {
		this.operator = operator;
	}
	
	/**
	 * The operator applied.
	 */
	public Operator getOperator() {
		return operator;
	}
	
	/**
	 * The number of operation arguments. 
	 */
	public abstract int getArgumentCount();

	/**
	 * The operation argument with the given index.
	 * 
	 * @param index
	 *        The argument index.
	 * @return the argument with the given index.
	 */
	public abstract Expression getArgument(int index);
	
}
