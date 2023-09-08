/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLType;

/**
 * Adds dynamic arguments to a {@link ResKey}.
 * 
 * @see ResKey#internalFill(Object...)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ResKeyArguments extends SimpleGenericMethod {

	/**
	 * The method name that invokes {@link ResKeyArguments} from a search expression.
	 */
	public static final String METHOD_NAME = "fill";

	/**
	 * Creates a {@link ResKeyArguments}.
	 * 
	 * @param self
	 *        See {@link #getSelf()}.
	 * @param arguments
	 *        The arguments to set on the {@link ResKey}, see {@link #getArguments()}.
	 */
	ResKeyArguments(SearchExpression self, SearchExpression[] arguments) {
		super(METHOD_NAME, self, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression self, SearchExpression[] arguments) {
		return new ResKeyArguments(self, arguments);
	}

	@Override
	public TLType getType(TLType selfType, List<TLType> argumentTypes) {
		// No type.
		return null;
	}

	@Override
	public Object eval(Object self, Object[] arguments) {
		if (!(self instanceof ResKey)) {
			// Null, literal string?
			return self;
		}
		return ResKey.message((ResKey) self, arguments);
	}

}
