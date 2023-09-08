/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.vars;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Algorithm expanding variables in strings with the syntax "text%VAR%text".
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VariableExpander implements IVariableExpander {

	private boolean _immutable = false;

	private final VariableSet _variables;

	/**
	 * Creates a {@link VariableExpander}.
	 *
	 */
	public VariableExpander() {
		this(new DefaultVariableSet());
	}

	/**
	 * Creates a {@link VariableExpander}.
	 */
	private VariableExpander(DefaultVariableSet variables) {
		_variables = variables;
	}

	@Override
	public String expand(String s) {
		if (s == null) {
			return null;
		}

		int length = s.length();
		int start = s.indexOf('%');
		if (start < 0 || start == length - 1) {
			// Nothing to do at all, e.g. "50%".
			return s;
		}

		StringBuilder result = new StringBuilder();

		result.append(s, 0, start);
		while (true) {
			int stop = s.indexOf('%', start + 1);
			if (stop < 0) {
				// Invalid syntax, keep for compatibility and to allow settings like "50%".
				result.append(s, start, length);
				break;
			}

			String key = s.substring(start + 1, stop);
			String value = _variables.getVariable(key);
			if (value == null) {
				// Undefined alias, consider start marker as plain text, continue with last found
				// marker.
				result.append(s, start, stop);
				start = stop;
				continue;
			}

			// The value of the variable.
			result.append(value);

			int next = stop + 1;
			start = s.indexOf('%', next);
			if (start < 0) {
				// Tail of plain text.
				result.append(s, next, length);
				break;
			} else {
				// Plain text between expressions.
				result.append(s, next, start);
			}
		}
		return result.toString();
	}

	/**
	 * Resolves a single variable.
	 */
	public String getValue(String name) {
		return _variables.getVariable(name);
	}

	/**
	 * Retrive the complete variable map.
	 */
	public Map<String, String> getVariables() {
		return _variables.getVariables();
	}

	/**
	 * Defines a single variable.
	 */
	public void addVariable(String key, String value) {
		checkImmutable();
		if (value == null) {
			throw new IllegalArgumentException("Variable value must not be null.");
		}
		_variables.addVariable(key, value);
	}

	private void checkImmutable() {
		if (_immutable) {
			throw new IllegalStateException("Can no loger be modified.");
		}
	}

	/**
	 * Finalizes the construction of this {@link VariableExpander} by resolving recursive variable
	 * definitions.
	 * 
	 * @throws IllegalStateException
	 *         If an endless recursion is detected.
	 */
	public void resolveRecursion() {
		int max = _variables.getLocalVariableCount() - 1;
		for (Iterator<Entry<String, String>> it = _variables.localVariableSet().iterator(); it.hasNext();) {
			Entry<String, String> entry = it.next();
			String oldValue = entry.getValue();

			String newValue = oldValue;
			int cnt = 0;
			while (true) {
				String nextValue = expand(newValue);
				if (nextValue.equals(newValue)) {
					break;
				}
				if ((cnt++) >= max) {
					throw new IllegalStateException("Self recursive variable definition: " + entry.getKey() + " -> " + oldValue + " -> " + newValue + " -> ...");
				}
				newValue = nextValue;
			}

			entry.setValue(newValue);
		}
		_immutable = true;
	}

	/**
	 * Creates a {@link VariableExpander} that contains all variable bindings of this
	 * {@link VariableExpander} but is again mutable.
	 */
	public VariableExpander derive() {
		if (!_immutable) {
			throw new IllegalStateException("Can only derive a finalized variable expander.");
		}
		return new VariableExpander(new FallbackVariableSet(_variables));
	}

}
