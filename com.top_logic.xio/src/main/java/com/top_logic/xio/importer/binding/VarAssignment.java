/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import com.top_logic.basic.util.Utils;

/**
 * A concrete {@link VariableScope} assignment.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class VarAssignment implements VariableScope {

	private final VariableScope _outer;

	private final String _var;

	private final Object _value;

	/**
	 * Creates a {@link VarAssignment}.
	 */
	public VarAssignment(VariableScope outer, String var, Object value) {
		_outer = outer;
		_var = var;
		_value = value;
	}

	@Override
	public VariableScope drop() {
		return _outer;
	}

	@Override
	public Object get(String name) {
		return Utils.equals(name, _var) ? _value : _outer.get(name);
	}

	@Override
	public boolean containsKey(String name) {
		return Utils.equals(name, _var) ? true : _outer.containsKey(name);
	}

}
