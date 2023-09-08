/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import com.top_logic.basic.StringServices;

/**
 * And combination of {@link Condition}s.
 * 
 * @see Conditions#and(Condition...)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class And extends CombinedCondition {

	/**
	 * Creates a {@link And}.
	 * 
	 * @param conditions
	 *        Conditions to combine.
	 * 
	 * @see Conditions#and(Condition...)
	 */
	And(Condition... conditions) {
		super(conditions);
	}

	@Override
	public String toString() {
		return "and(" + StringServices.join(conditions(), ',') + ")";
	}

	@Override
	public <R, A> R visit(ConditionVisitor<R, A> v, A arg) {
		return v.visitAnd(this, arg);
	}

}