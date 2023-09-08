/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;

/**
 * {@link ValueNamingScheme} for {@link CompareRowObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareRowObjectNamingScheme extends ValueNamingScheme<CompareRowObject> {
	
	private static final String BASE_VALUE_KEY = "oldValue";

	private static final String CHANGE_VALUE_KEY = "newValue";

	@Override
	public Class<CompareRowObject> getModelClass() {
		return CompareRowObject.class;
	}

	@Override
	public Map<String, Object> getName(CompareRowObject model) {
		if (model.changeValue() != null) {
			return Collections.singletonMap(CHANGE_VALUE_KEY, model.changeValue());
		} else {
			return Collections.singletonMap(BASE_VALUE_KEY, model.baseValue());
		}
	}

	@Override
	public boolean matches(Map<String, Object> name, CompareRowObject model) {
		return matchesDefault(name, model);
	}

}
