/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collections;
import java.util.Set;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.visit.Visitor;
import com.top_logic.model.security.ModelAccessRights;

/**
 * {@link SearchExpression} looking up objects referring to a given target object through a given
 * reference.
 * 
 * @see #getReference()
 * @see #getTarget()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Referers extends SearchExpressionWithSecurity implements WithFlatMapSemantics<TLReference> {

	private TLReference _reference;

	private SearchExpression _target;

	Referers(SearchExpression target, TLReference reference, boolean usesSecurity) {
		super(usesSecurity);
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
		return lookupReferrers(usesSecurity(), targetValue, reference);

	}

	/**
	 * The objects referring to the given target through the given reference.
	 *
	 * <p>
	 * An object that is no longer {@link TLObject#tValid() valid} (a persistent object deleted in
	 * the meantime) counts as a missing value and has no referrers.
	 * </p>
	 *
	 * @param usesSecurity
	 *        Whether the read access to the target must be checked.
	 * @param targetValue
	 *        The object whose referrers are looked up.
	 * @param reference
	 *        The reference pointing to the target.
	 * @return The referrers, <code>null</code> if the target is not a {@link TLObject}.
	 */
	static Set<? extends TLObject> lookupReferrers(boolean usesSecurity, Object targetValue, TLReference reference) {
		if (!(targetValue instanceof TLObject)) {
			return null;
		}
		TLObject self = (TLObject) targetValue;
		if (!self.tValid()) {
			return Collections.emptySet();
		}
		if (usesSecurity && !ModelAccessRights.getInstance().isReadAllowed(self)) {
			// No read access to the base object - cannot look up its referrers.
			return Collections.emptySet();
		}
		// Only discoverable referrers are delivered (R4 of the Discover specification).
		@SuppressWarnings("unchecked")
		Set<? extends TLObject> result =
			(Set<? extends TLObject>) SearchExpression.discoverable(self.tReferers(reference), usesSecurity);
		return result;
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visitReferers(this, arg);
	}

}
