/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.Collection;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.Argument;

/**
 * Describes arguments of a <i>TLScript</i> function.
 * 
 * @see MethodBuilder#build(com.top_logic.model.search.expr.config.dom.Expr.Method, Argument[])
 */
public interface ArgumentDescriptor {

	/**
	 * The maximum number of arguments, or <code>-1</code> for an unlimited number of arguments.
	 */
	int getMaxArgCnt();

	/**
	 * The name of the argument with the given position.
	 * 
	 * @param n
	 *        The index of the argument.
	 */
	String getArgumentName(int n);

	/**
	 * The index of the argument with the given name.
	 * 
	 * @param name
	 *        The name of the argument.
	 * 
	 * @return The index of the argument with the given name, or <code>-1</code> if no such argument
	 *         is known.
	 */
	int getArgumentIndex(String name);

	/**
	 * The default value to use for the argument with the given index, or <code>null</code> if this
	 * is a mandatory argument.
	 * 
	 * @param n
	 *        The index of the argument.
	 */
	SearchExpression getDefaultValue(int n);

	/**
	 * The list of all argument names known.
	 */
	Collection<String> getArgumentNames();

	/**
	 * Unwraps arguments making positional arguments from potentially named arguments.
	 * 
	 * @param fun
	 *        the name of the function (for error reporting).
	 * @param args
	 *        The arguments from the syntax tree.
	 */
	SearchExpression[] unwrap(String fun, Argument[] args) throws ConfigurationException;

	/**
	 * Creates a builder for creating an {@link ArgumentDescriptor} supporting named arguments.
	 */
	static ArgumentDescriptorBuilder builder() {
		return new ArgumentDescriptorBuilderImpl();
	}

}
