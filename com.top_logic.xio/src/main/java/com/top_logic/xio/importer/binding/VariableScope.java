/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

/**
 * A variable assignment context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface VariableScope {

	/**
	 * Assigns a variable with a value.
	 *
	 * @param var
	 *        The variable to assign.
	 * @param value
	 *        The value.
	 * @return The new {@link VariableScope} context that contains the assignment.
	 */
	default VariableScope assign(String var, Object value) {
		return new VarAssignment(this, var, value);
	}

	/**
	 * Removes an {@link #assign(String, Object) assignment}.
	 * 
	 * @return The {@link VariableScope} without the removed assignment.
	 */
	VariableScope drop();

	/**
	 * Resolves the value of the variable with the given name.
	 */
	Object get(String name);

	/**
	 * Checks, whether this context has a variable assignment with the given name.
	 */
	boolean containsKey(String name);

}
