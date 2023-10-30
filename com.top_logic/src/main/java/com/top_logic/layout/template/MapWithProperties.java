/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

/**
 * Properties represented with an underlying internal map.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class MapWithProperties extends HashMap<String, Object> implements WithProperties {

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		Object result = get(propertyName);
		if (result != null || containsKey(propertyName)) {
			return result;
		}
		return WithProperties.super.getPropertyValue(propertyName);
	}

	@Override
	public Optional<Collection<String>> getAvailableProperties() {
		return Optional.of(keySet());
	}

	@Override
	public RuntimeException errorNoSuchProperty(String propertyName) {
		throw new IllegalArgumentException(
			"No such property '" + propertyName + "', available properties are " + getAvailableProperties() + ".");
	}
}
