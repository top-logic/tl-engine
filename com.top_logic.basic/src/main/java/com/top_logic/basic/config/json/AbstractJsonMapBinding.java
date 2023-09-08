/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.util.Collections;
import java.util.Map;

/**
 * Abstract {@link JsonValueBinding} implementation handling {@link Map} objects.
 * 
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractJsonMapBinding<K, V> implements JsonValueBinding<Map<K, V>> {

	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}

	@Override
	public Map<K, V> defaultValue() {
		return Collections.emptyMap();
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return value;
	}


}

