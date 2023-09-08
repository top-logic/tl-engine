/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

/**
 * The {@link Condition} that always holds.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class True extends SingletonCondition {

	/**
	 * Singleton {@link True} instance.
	 * 
	 * @see Conditions#ifTrue()
	 */
	static final True INSTANCE = new True();

	private True() {
		super();
	}

	@Override
	public String toString() {
		return "true";
	}

	@Override
	public <R, A> R visit(ConditionVisitor<R, A> v, A arg) {
		return v.visitTrue(this, arg);
	}

}