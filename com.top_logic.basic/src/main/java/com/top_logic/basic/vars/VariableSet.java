/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.vars;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Variable definitions for a {@link VariableExpander}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class VariableSet {

	/**
	 * Retrieves the value bound to the given variable name.
	 */
	public abstract String getVariable(String key);

	/**
	 * Defines a new variable (or overrides an existing variable definition with the same name).
	 */
	abstract void addVariable(String key, String value);

	/**
	 * View of all defined variables.
	 */
	abstract Map<String, String> getVariables();

	/**
	 * The number of variables defined locally (excluding all inherited variables, see
	 * {@link VariableExpander#derive()}.
	 */
	abstract int getLocalVariableCount();

	/**
	 * The (mutable) set of locall defined variables, see {@link #getLocalVariableCount()}.
	 */
	abstract Set<Entry<String, String>> localVariableSet();

}
