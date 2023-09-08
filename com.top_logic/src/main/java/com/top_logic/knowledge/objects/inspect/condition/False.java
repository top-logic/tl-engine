/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

/**
 * The {@link Condition} that never holds.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class False extends SingletonCondition {

	/**
	 * Singleton {@link False} instance.
	 * 
	 * @see Conditions#ifFalse()
	 */
	static final False INSTANCE = new False();

	private False() {
		super();
	}

	@Override
	public String toString() {
		return "false";
	}

	@Override
	public <R, A> R visit(ConditionVisitor<R, A> v, A arg) {
		return v.visitFalse(this, arg);
	}

}