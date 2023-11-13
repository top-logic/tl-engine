/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Arrays;
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
	 * @param arguments
	 *        The arguments to set on the {@link ResKey}, see {@link #getArguments()}.
	 */
	ResKeyArguments(SearchExpression[] arguments) {
		super(METHOD_NAME, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return SearchExpressionFactory.reskeyArguments(arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		// No type.
		return null;
	}

	@Override
	public Object eval(Object[] arguments) {
		if (!(arguments[0] instanceof ResKey)) {
			// Null, literal string?
			return arguments[0];
		}
		return ResKey.message((ResKey) arguments[0], Arrays.copyOfRange(arguments, 1, arguments.length));
	}

}
