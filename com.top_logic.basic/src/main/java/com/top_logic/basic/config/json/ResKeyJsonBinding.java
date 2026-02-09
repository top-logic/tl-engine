/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for {@link ResKey} values.
 *
 * <p>
 * Serializes all {@link ResKey} types (including literal keys with translations) using
 * {@link ResKey#encode(ResKey)} / {@link ResKey#decode(String)}.
 * </p>
 */
public class ResKeyJsonBinding implements JsonValueBinding<ResKey> {

	@Override
	public ResKey loadConfigItem(PropertyDescriptor property, JsonReader in, ResKey baseValue)
			throws IOException, ConfigurationException {
		String encoded = in.nextString();
		if (encoded == null || encoded.isEmpty()) {
			return null;
		}
		return ResKey.decode(encoded);
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, ResKey item) throws IOException {
		out.value(ResKey.encode(item));
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || value instanceof ResKey;
	}

	@Override
	public ResKey defaultValue() {
		return null;
	}

	@Override
	public Object normalize(Object value) {
		return value;
	}

}
