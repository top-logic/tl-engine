/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.util.Collections;
import java.util.List;

/**
 * Abstract {@link JsonValueBinding} implementation handling {@link List} objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractJsonListBinding<T> implements JsonValueBinding<List<T>> {

	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}

	@Override
	public List<T> defaultValue() {
		return Collections.emptyList();
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return value;
	}


}

