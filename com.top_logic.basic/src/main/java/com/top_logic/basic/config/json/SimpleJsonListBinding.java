/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} with simple serialization for {@link List}s, i.e. default serialization
 * for {@link Collection}, {@link Map}, {@link Number}, {@link Boolean}, {@link String}, and
 * <code>null</code> values in the given {@link List}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimpleJsonListBinding extends AbstractJsonListBinding<Object> {

	@Override
	public List<Object> loadConfigItem(PropertyDescriptor property, JsonReader in, List<Object> baseValue)
			throws IOException, ConfigurationException {
		Object readElement = JsonUtilities.readValue(in);
		if (readElement == null) {
			return baseValue != null ? baseValue : defaultValue();
		} else {
			List<?> readMap = (List<?>) readElement;
			List<Object> result = baseValue != null ? new ArrayList<>(baseValue) : new ArrayList<>();
			result.addAll(readMap);
			return result;
		}
	}
	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, List<Object> item) throws IOException {
		JsonUtilities.writeValue(out, item);
	}

}

