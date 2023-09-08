/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} rounding a {@link Date} value to the calendar day.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GetDay extends UnaryOperation {

	/**
	 * Creates a {@link GetDay}.
	 * 
	 * @param value
	 *        The {@link Date} expression to get the day part of.
	 */
	GetDay(SearchExpression value) {
		super(value);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitGetDay(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		return compute(getArgument().evalWith(definitions, args));
	}

	/**
	 * Performs the actual {@link GetDay} computation with the given values.
	 */
	public final Date compute(Object value) {
		if (!(value instanceof Date)) {
			return null;
		}
		return DateUtil.adjustToDayBegin((Date) value);
	}

}
