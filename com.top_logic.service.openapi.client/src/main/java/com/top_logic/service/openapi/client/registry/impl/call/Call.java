/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.impl.call;

import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;

/**
 * Arguments passed to a TL-Script function call.
 * 
 * @see CallBuilder#buildRequest(org.apache.hc.core5.http.ClassicHttpRequest, Call)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Call {

	/**
	 * Value of the function argument with the given index.
	 * 
	 * <p>
	 * The argument with index zero is the "self" value.
	 * </p>
	 */
	Object getArgument(int index);

	/**
	 * All arguments to the function invokation.
	 */
	Object[] getArguments();

	/**
	 * Creates a {@link Call} from the signature of
	 * {@link GenericMethod#eval(Object, Object[], EvalContext)}.
	 */
	static Call newInstance(Object self, Object[] arguments) {
		return new Call() {
			@Override
			public Object getArgument(int index) {
				return index == 0 ? self : arguments[index - 1];
			}

			@Override
			public Object[] getArguments() {
				Object[] result = new Object[arguments.length + 1];
				result[0] = self;
				System.arraycopy(arguments, 0, result, 1, arguments.length);
				return result;
			}
		};
	}

	/**
	 * Creates a {@link Call} from a complete argument array.
	 */
	static Call newInstance(Object[] allArguments) {
		return new Call() {
			@Override
			public Object[] getArguments() {
				return allArguments;
			}

			@Override
			public Object getArgument(int index) {
				return allArguments[index];
			}
		};
	}

}
