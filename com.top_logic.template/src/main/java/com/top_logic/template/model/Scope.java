/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

import java.util.HashMap;

/**
 * Binding of symbols to names.
 * 
 * @param <S> The symbol type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class Scope<S> {

	private final HashMap<String, S> data = new HashMap<>();

	public S declare(String variable, S type) {
		assert variable != null;
		assert type != null;
		assert !data.containsKey(variable) : "Variable '" + variable + "' is already defined. Type: '"
			+ data.get(variable) + "' New Type: '" + type + "'";
		
		return data.put(variable, type);
	}

	public S remove(String variable) {
		return data.remove(variable);
	}

	public boolean isDeclared(String variable) {
		return data.containsKey(variable);
	}

	public S lookup(String variable) {
		return data.get(variable);
	}
	
	public void clear() {
		data.clear();
	}

}
