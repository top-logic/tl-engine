/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * Access to a model property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Access extends SearchExpression implements AccessLike {
	private SearchExpression _self;

	private TLStructuredTypePart _part;

	/**
	 * Creates an {@link Access}.
	 * 
	 * @param self
	 *        See {@link #getSelf()}.
	 * @param part
	 *        See {@link #getPart()}.
	 */
	protected Access(SearchExpression self, TLStructuredTypePart part) {
		_self = self;
		_part = part;
	}

	/**
	 * The model instance to accesss.
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
	 * The model part to resolve from {@link #getSelf()}.
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

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitAccess(this, arg);
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object base = getSelf().evalWith(definitions, args);

		return evalPotentialFlatMap(definitions, base, getPart());
	}

}
