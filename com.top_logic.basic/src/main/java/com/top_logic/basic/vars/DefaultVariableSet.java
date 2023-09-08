/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.vars;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Default {@link VariableSet} implementation based on a {@link HashMap}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class DefaultVariableSet extends VariableSet {

	private Map<String, String> _data = new HashMap<>();

	@Override
	public String getVariable(String key) {
		return _data.get(key);
	}

	@Override
	void addVariable(String key, String value) {
		_data.put(key, value);
	}

	@Override
	Map<String, String> getVariables() {
		return Collections.unmodifiableMap(_data);
	}

	@Override
	int getLocalVariableCount() {
		return _data.size();
	}

	@Override
	Set<Entry<String, String>> localVariableSet() {
		return _data.entrySet();
	}

}
