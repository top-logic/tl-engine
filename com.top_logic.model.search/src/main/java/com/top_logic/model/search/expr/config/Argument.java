/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config;

import com.top_logic.model.search.expr.SearchExpression;

/**
 * An argument to a build-in method call.
 */
public class Argument {

	private final String _name;

	private final SearchExpression _value;

	/**
	 * Creates an {@link Argument}.
	 *
	 * @param name
	 *        an optional name.
	 * @param value
	 *        the expression computing the value.
	 */
	public Argument(String name, SearchExpression value) {
		_name = name;
		_value = value;
	}

	/**
	 * An optional name of a named argument.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * The expression computing the argument value.
	 */
	public SearchExpression getValue() {
		return _value;
	}

}
