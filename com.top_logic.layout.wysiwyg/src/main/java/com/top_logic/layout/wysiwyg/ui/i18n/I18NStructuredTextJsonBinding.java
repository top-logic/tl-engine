/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.json.schema.model.EnumSchema;
import com.top_logic.basic.json.schema.model.ObjectSchema;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextJsonBinding;

/**
 * {@link JsonValueBinding} for {@link I18NStructuredText} values.
 *
 * <p>
 * An {@link I18NStructuredText} is serialized as a JSON object where property names are locale
 * language tags and values are {@link StructuredText} objects as serialized by
 * {@link StructuredTextJsonBinding}.
 * </p>
 *
 * @see I18NStructuredTextValueBinding
 */
public class I18NStructuredTextJsonBinding implements JsonValueBinding<I18NStructuredText> {

	private final StructuredTextJsonBinding _delegate = new StructuredTextJsonBinding();

	@Override
	public I18NStructuredText loadConfigItem(PropertyDescriptor property, JsonReader in, I18NStructuredText baseValue)
			throws IOException, ConfigurationException {
		Map<Locale, StructuredText> content = new HashMap<>();

		in.beginObject();
		while (in.hasNext()) {
			String localeTag = in.nextName();
			Locale locale = Locale.forLanguageTag(localeTag);
			StructuredText text = _delegate.loadConfigItem(property, in, null);
			content.put(locale, text);
		}
		in.endObject();

		return new I18NStructuredText(content);
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, I18NStructuredText item)
			throws IOException {
		out.beginObject();
		for (Map.Entry<Locale, StructuredText> entry : item.getEntries().entrySet()) {
			out.name(entry.getKey().toLanguageTag());
			_delegate.saveConfigItem(property, out, entry.getValue());
		}
		out.endObject();
	}

	@Override
	public Schema buildSchema(PropertyDescriptor property) {
		ObjectSchema schema = ObjectSchema.create();
		schema.setAdditionalProperties(_delegate.buildSchema(property));

		EnumSchema localeEnum = EnumSchema.create();
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			localeEnum.addEnumLiteral(locale.toLanguageTag());
		}
		schema.setPropertyNames(localeEnum);

		return schema;
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || value instanceof I18NStructuredText;
	}

	@Override
	public I18NStructuredText defaultValue() {
		return null;
	}

	@Override
	public Object normalize(Object value) {
		return value;
	}

}
