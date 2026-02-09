/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOException;
import java.util.Locale;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonToken;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for {@link ResKey} values.
 *
 * <p>
 * Literal {@link ResKey}s (with inline translations) are serialized as a JSON object with language
 * tags as keys, e.g. <code>{"de": "Belege", "en": "Invoices"}</code>.
 * </p>
 *
 * <p>
 * Non-literal {@link ResKey}s (resource key references) are serialized as a plain JSON string, e.g.
 * <code>"my.resource.key"</code>.
 * </p>
 */
public class ResKeyJsonBinding implements JsonValueBinding<ResKey> {

	@Override
	public ResKey loadConfigItem(PropertyDescriptor property, JsonReader in, ResKey baseValue)
			throws IOException, ConfigurationException {
		if (in.peek() == JsonToken.BEGIN_OBJECT) {
			return readLiteralKey(in);
		} else {
			String encoded = in.nextString();
			if (encoded == null || encoded.isEmpty()) {
				return null;
			}
			return ResKey.decode(encoded);
		}
	}

	private ResKey readLiteralKey(JsonReader in) throws IOException {
		ResKey.Builder builder = ResKey.builder();
		in.beginObject();
		while (in.hasNext()) {
			String localeName = in.nextName();
			String translation = in.nextString();
			builder.add(new Locale(localeName), translation);
		}
		in.endObject();
		return builder.build();
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, ResKey item) throws IOException {
		if (item.isLiteral()) {
			writeLiteralKey(out, item);
		} else {
			out.value(ResKey.encode(item));
		}
	}

	private void writeLiteralKey(JsonWriter out, ResKey item) throws IOException {
		out.beginObject();
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			String translation = ResKeyUtil.getTranslation(item, locale);
			if (!StringServices.isEmpty(translation)) {
				out.name(locale.toString());
				out.value(translation);
			}
		}
		out.endObject();
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
