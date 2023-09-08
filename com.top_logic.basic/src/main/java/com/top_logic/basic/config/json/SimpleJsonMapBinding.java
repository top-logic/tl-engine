/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} with simple serialization for {@link Map}s, i.e. default serialization
 * for {@link Collection}, {@link Map}, {@link Number}, {@link Boolean}, {@link String}, and
 * <code>null</code> values in the given {@link Map}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleJsonMapBinding extends AbstractJsonMapBinding<String, Object> {

	@Override
	public Map<String, Object> loadConfigItem(PropertyDescriptor property, JsonReader in, Map<String, Object> baseValue)
			throws IOException, ConfigurationException {
		Object readElement = JsonUtilities.readValue(in);
		if (readElement == null) {
			return baseValue != null ? baseValue : defaultValue();
		} else {
			@SuppressWarnings("unchecked")
			Map<String, ?> readMap = (Map<String, ?>) readElement;
			Map<String, Object> result = baseValue != null ? new HashMap<>(baseValue) : new HashMap<>();
			result.putAll(readMap);
			return result;
		}
	}
	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, Map<String, Object> item) throws IOException {
		JsonUtilities.writeValue(out, item);
	}

}

