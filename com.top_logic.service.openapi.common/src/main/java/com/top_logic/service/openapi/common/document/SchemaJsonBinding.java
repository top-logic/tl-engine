/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.basic.shared.io.StringW;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} expecting that a given {@link String} is a serialized Json element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SchemaJsonBinding implements JsonValueBinding<String> {

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || value instanceof CharSequence;
	}

	@Override
	public String defaultValue() {
		return "";
	}

	@Override
	public Object normalize(Object value) {
		return value;
	}

	@Override
	public String loadConfigItem(PropertyDescriptor property, JsonReader in, String baseValue) throws IOException, ConfigurationException {
		StringW out = new StringW();
		try (JsonWriter w = new JsonWriter(out)) {
			JsonUtilities.copyNextJsonElement(in, w);
		}
		return out.toString();
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, String item) throws IOException {
		try (JsonReader in = new JsonReader(new StringR(item))) {
			JsonUtilities.copyNextJsonElement(in, out);
		}
	}

}
