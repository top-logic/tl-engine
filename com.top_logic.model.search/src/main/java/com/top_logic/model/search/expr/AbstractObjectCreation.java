/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;

/**
 * Base class for TL-Script constructor functions.
 */
public abstract class AbstractObjectCreation extends GenericMethod {

	/**
	 * Creates a {@link AbstractObjectCreation}.
	 */
	protected AbstractObjectCreation(String name, SearchExpression self, SearchExpression[] arguments) {
		super(name, self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		SearchExpression self = getSelf();
		if (self instanceof Literal) {
			return (TLClass) ((Literal) self).getValue();
		} else {
			// No type can be determined without evaluating the self expression.
			return null;
		}
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

}
