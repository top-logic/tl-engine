/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link SearchExpression} testing the concrete type of an instance.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InstanceOf extends TypeCheck implements BooleanExpression {

	/**
	 * Creates a {@link InstanceOf}.
	 * @param value
	 *        See {@link #getValue()}.
	 * @param type
	 *        See {@link #getCheckType()}.
	 */
	InstanceOf(SearchExpression value, TLStructuredType type) {
		super(value, type);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object value = getValue().evalWith(definitions, args);
		if (!(value instanceof TLObject)) {
			return false;
		}

		TLObject item = asTLObject(value);
		return TLModelUtil.isCompatibleInstance(getCheckType(), item);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitInstanceOf(this, arg);
	}

}
