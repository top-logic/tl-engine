/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.Argument;

/**
 * Default {@link ArgumentDescriptor} implementation.
 */
public class ArgumentDescriptorImpl implements ArgumentDescriptor {

	/**
	 * Generic {@link ArgumentDescriptor} that accepts any number of arguments.
	 */
	public static final ArgumentDescriptor ANY = new ArgumentDescriptorImpl(-1);


	private final int _max;

	/**
	 * Creates a {@link ArgumentDescriptorImpl}.
	 */
	public ArgumentDescriptorImpl(int max) {
		_max = max;
	}

	@Override
	public SearchExpression[] unwrap(String fun, Argument[] args) throws ConfigurationException {
		SearchExpression[] result;
		int maxArgs = getMaxArgCnt();
		boolean varArg = maxArgs < 0;
		if (varArg) {
			result = new SearchExpression[args.length];
		} else {
			result = new SearchExpression[maxArgs];
		}

		int pos = 0;
		boolean named = false;
		for (Argument arg : args) {
			String name = arg.getName();
			if (name == null) {
				if (named) {
					throw AbstractMethodBuilder.error(I18NConstants.ERROR_INVALID_NAMED_ARGUMENT_ORDER);
				}
				result[pos++] = arg.getValue();
			} else {
				if (varArg) {
					throw AbstractMethodBuilder
						.error(I18NConstants.ERROR_NO_NAMED_ARGUMENTS__FUN_NAME.fill(fun, arg.getName()));
				}

				int position = getArgumentIndex(name);
				if (position < 0) {
					Collection<String> argumentNames = getArgumentNames();
					if (argumentNames.isEmpty()) {
						throw AbstractMethodBuilder
							.error(I18NConstants.ERROR_NO_NAMED_ARGUMENTS__FUN_NAME.fill(fun, arg.getName()));
					} else {
						throw AbstractMethodBuilder
							.error(I18NConstants.ERROR_NO_SUCH_NAMED_ARGUMENT__FUN_NAME_AVAIL.fill(fun, name,
								argumentNames));
					}
				}

				if (result[position] != null) {
					throw AbstractMethodBuilder.error(I18NConstants.ERROR_AMBIGUOUS_ARGUMENT__FUN_NAME.fill(fun, name));
				}

				result[position] = arg.getValue();
				named = true;
			}
		}

		for (int n = 0, cnt = result.length; n < cnt; n++) {
			if (result[n] == null) {
				SearchExpression defaultValue = getDefaultValue(n);
				if (defaultValue == null) {
					throw AbstractMethodBuilder
						.error(I18NConstants.ERROR_MANDATORY_ARGUMENT__ARG_EXPR.fill(getArgumentName(n), fun));
				}
				result[n] = defaultValue;
			}
		}

		return result;
	}

	/**
	 * The name of the argument with the given position.
	 * 
	 * @param n
	 *        The index of the argument.
	 */
	protected String getArgumentName(int n) {
		return Integer.toString(n + 1);
	}

	/**
	 * The maximum number of arguments, or <code>-1</code> for an unlimited number of arguments.
	 */
	protected int getMaxArgCnt() {
		return _max;
	}

	/**
	 * The index of the argument with the given name.
	 * 
	 * @param name
	 *        The name of the argument.
	 * 
	 * @return The index of the argument with the given name, or <code>-1</code> if no such argument
	 *         is known.
	 */
	protected int getArgumentIndex(String name) {
		// No named arguments.
		return -1;
	}

	/**
	 * The default value to use for the argument with the given index, or <code>null</code> if this
	 * is a mandatory argument.
	 * 
	 * @param n
	 *        The index of the argument.
	 */
	protected SearchExpression getDefaultValue(int n) {
		return null;
	}

	/**
	 * The list of all argument names known.
	 */
	protected Collection<String> getArgumentNames() {
		return Collections.emptyList();
	}

}
