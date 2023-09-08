/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;

/**
 * {@link SearchExpression} looking up objects referring to a given target object through a given
 * reference.
 * 
 * @see #getReference()
 * @see #getTarget()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Referers extends SearchExpression implements WithFlatMapSemantics<TLReference> {

	private TLReference _reference;

	private SearchExpression _target;

	Referers(SearchExpression target, TLReference reference) {
		_reference = reference;
		_target = target;
	}

	/**
	 * The reference whose contents is searched for the {@link #getTarget()} object.
	 */
	public TLReference getReference() {
		return _reference;
	}

	/**
	 * @see #getReference()
	 */
	public void setReference(TLReference reference) {
		_reference = reference;
	}

	/**
	 * The object to be searched in the given {@link #getReference()}.
	 */
	public SearchExpression getTarget() {
		return _target;
	}

	/**
	 * @see #getTarget()
	 */
	public void setTarget(SearchExpression target) {
		_target = target;
	}

	@Override
	public Object internalEval(EvalContext definitions, Args args) {
		Object targetValue = _target.evalWith(definitions, args);
		return evalPotentialFlatMap(definitions, targetValue, _reference);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object targetValue, TLReference reference) {
		if (!(targetValue instanceof TLObject)) {
			return null;
		}
		TLObject self = (TLObject) targetValue;
		return self.tReferers(reference);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitReferers(this, arg);
	}

}
