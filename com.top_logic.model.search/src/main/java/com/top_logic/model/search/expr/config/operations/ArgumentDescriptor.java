/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.Argument;

/**
 * Description of arguments of a {@link MethodBuilder}.
 * 
 * @see MethodBuilder#build(com.top_logic.model.search.expr.config.dom.Expr.Method, Argument[])
 */
public interface ArgumentDescriptor {

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
