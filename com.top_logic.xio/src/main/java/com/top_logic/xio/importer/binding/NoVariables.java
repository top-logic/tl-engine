/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

/**
 * {@link VariableScope} with no assignments.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class NoVariables implements VariableScope {

	/**
	 * Singleton {@link NoVariables} instance.
	 */
	public static final NoVariables INSTANCE = new NoVariables();

	private NoVariables() {
		// Singleton constructor.
	}

	@Override
	public VariableScope drop() {
		return this;
	}

	@Override
	public Object get(String name) {
		return null;
	}

	@Override
	public boolean containsKey(String name) {
		return false;
	}

}
