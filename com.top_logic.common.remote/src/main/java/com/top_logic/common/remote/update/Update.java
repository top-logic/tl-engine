/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.update;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.json.JsonUtil;

/**
 * An object modification {@link Change}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Update extends Change {

	private final Map<String, Object> _values;

	/**
	 * Creates a {@link Update}.
	 *
	 * @param id
	 *        The object Id of the modified object.
	 * @param values
	 *        The mapping of touched values by their property names.
	 */
	public Update(String id, Map<String, Object> values) {
		super(id);
		_values = values;
	}

	/**
	 * The touched values indexed by threir property names.
	 */
	public Map<String, Object> getValues() {
		return _values;
	}

	@Override
	protected void serializeContent(JsonWriter writer) throws IOException {
		super.serializeContent(writer);
		writer.name("values");
		writer.beginObject();
		for (Entry<String, Object> entry : getValues().entrySet()) {
			String key = entry.getKey();
			writer.name(key);
			JsonUtil.writeValue(writer, entry.getValue());
		}
		writer.endObject();
	}

	static Update readUpdate(JsonReader reader) throws IOException {
		String id = null;
		Map<String, Object> values = new HashMap<>();
		reader.beginObject();
		while (reader.hasNext()) {
			String property = reader.nextName();
			switch (property) {
				case "id":
					id = reader.nextString();
					break;
				case "values":
					JsonUtil.readMap(reader, values);
					break;
				default:
					throw new IllegalArgumentException("Unexpected property: " + property);
			}
		}
		reader.endObject();
		return new Update(id, values);
	}

	@Override
	public String toString() {
		return "Update(id = " + getId() + "; values = " + getValues() + ")";
	}

}
