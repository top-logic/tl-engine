/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;

/**
 * {@link ValueNamingScheme} that matches an object of a certain type that only
 * can occur exactly once in a given context.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SingletonValueNamingScheme<M> extends ValueNamingScheme<M> {

	@Override
	public Map<String, Object> getName(M model) {
		// There can only be a single new object in a certain context.
		return Collections.emptyMap();
	}

	@Override
	public boolean matches(Map<String, Object> name, M model) {
		return true;
	}

}
