/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.vars;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link VariableSet} that uses another {@link VariableSet} with fallback definitions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class FallbackVariableSet extends DefaultVariableSet {

	private final VariableSet _fallback;

	/**
	 * Creates a {@link FallbackVariableSet}.
	 *
	 */
	public FallbackVariableSet(VariableSet fallback) {
		_fallback = fallback;
	}

	@Override
	public String getVariable(String key) {
		String result = super.getVariable(key);
		if (result != null) {
			return result;
		}

		return _fallback.getVariable(key);
	}

	@Override
	Map<String, String> getVariables() {
		HashMap<String, String> result = new HashMap<>(_fallback.getVariables());
		result.putAll(super.getVariables());
		return Collections.unmodifiableMap(result);
	}
}
