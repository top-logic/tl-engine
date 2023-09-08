/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import com.top_logic.basic.StringServices;

/**
 * Or combination of {@link Condition}s.
 * 
 * @see Conditions#or(Condition...)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Or extends CombinedCondition {

	/**
	 * Creates a {@link Or}.
	 * 
	 * @see Conditions#or(Condition...)
	 */
	Or(Condition... conditions) {
		super(conditions);
	}

	@Override
	public String toString() {
		return "or(" + StringServices.join(conditions(), ',') + ")";
	}

	@Override
	public <R, A> R visit(ConditionVisitor<R, A> v, A arg) {
		return v.visitOr(this, arg);
	}

}