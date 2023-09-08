/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Updating the value of a model property as side-effect.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Update extends SearchExpression {

	private SearchExpression _self;

	private TLStructuredTypePart _part;

	private SearchExpression _value;

	Update(SearchExpression self, TLStructuredTypePart part, SearchExpression value) {
		_self = self;
		_part = part;
		_value = value;
	}

	/**
	 * The object on which the {@link #getPart()} is updated.
	 */
	public SearchExpression getSelf() {
		return _self;
	}

	/**
	 * @see #getSelf()
	 */
	public void setSelf(SearchExpression self) {
		_self = self;
	}

	/**
	 * The updated attribute.
	 */
	public TLStructuredTypePart getPart() {
		return _part;
	}

	/**
	 * @see #getPart()
	 */
	public void setPart(TLStructuredTypePart part) {
		_part = part;
	}

	/**
	 * The expression delivering the value to which the {@link #getPart()} of {@link #getSelf()} is
	 * updated.
	 */
	public SearchExpression getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(SearchExpression value) {
		_value = value;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		TLObject self = asTLObjectNonNull(getSelf().evalWith(definitions, args));
		Object value = getValue().evalWith(definitions, args);
		self.tUpdate(getPart(), value);
		return null;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitUpdate(this, arg);
	}

}
