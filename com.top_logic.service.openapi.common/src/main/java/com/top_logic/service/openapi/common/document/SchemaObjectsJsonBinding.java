/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.json.AbstractJsonMapBinding;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for formatting (and parsing) a set of {@link SchemaObject}s indexed by
 * {@link SchemaObject#getName()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SchemaObjectsJsonBinding extends AbstractJsonMapBinding<String, SchemaObject> {

	@Override
	public Map<String, SchemaObject> loadConfigItem(PropertyDescriptor property, JsonReader in, Map<String, SchemaObject> baseValue)
			throws IOException, ConfigurationException {
		Map<String, SchemaObject> value = baseValue == null ? new LinkedHashMap<>() : new LinkedHashMap<>(baseValue);
		in.beginObject();
		while (in.hasNext()) {
			String schemaName = in.nextName();
			String schemaString = readActualSchema(in);

			SchemaObject schemaObject = TypedConfiguration.newConfigItem(SchemaObject.class);
			schemaObject.setName(schemaName);
			schemaObject.setSchema(schemaString);
			value.put(schemaObject.getName(), schemaObject);
		}
		in.endObject();

		return value;
	}

	private String readActualSchema(JsonReader in) throws IOException {
		Object schema = JsonUtilities.readValue(in);
		StringBuilder schemaString = new StringBuilder();
		JSON.write(schemaString, DefaultValueAnalyzer.INSTANCE, schema, true);
		return schemaString.toString();
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, Map<String, SchemaObject> item) throws IOException {
		if (item == null) {
			return;
		}
		out.beginObject();
		for (SchemaObject schema : item.values()) {
			out.name(schema.getName());
			Object schemaObject = JsonUtilities.parse(schema.getSchema());
			JsonUtilities.writeValue(out, schemaObject);
		}
		out.endObject();
	}

}
