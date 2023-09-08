/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.compile.transform;

import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.Update;
import com.top_logic.model.search.expr.visit.DefaultDescendingVisitor;

/**
 * Decides, whether the visited {@link SearchExpression} may have side-effects.
 * 
 * @see Update
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HasSideEffects extends DefaultDescendingVisitor<Boolean, Void> {

	/**
	 * Singleton {@link HasSideEffects} instance.
	 */
	public static final HasSideEffects INSTANCE = new HasSideEffects();

	private HasSideEffects() {
		// Singleton constructor.
	}

	@Override
	public Boolean visitUpdate(Update expr, Void arg) {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitGenericMethod(GenericMethod expr, Void arg) {
		return Boolean.valueOf(!expr.isSideEffectFree());
	}

	@Override
	protected Boolean combine(Boolean result1, Boolean result2) {
		if (result1 == null) {
			return result2;
		}
		if (result2 == null) {
			return result1;
		}
		return Boolean.valueOf(result1.booleanValue() || result2.booleanValue());
	}

	@Override
	protected Boolean none() {
		return Boolean.FALSE;
	}

}
