/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.i18n;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.json.JsonValueBinding;
import com.top_logic.basic.config.json.ResKeyJsonBinding;
import com.top_logic.basic.json.schema.model.Schema;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;

/**
 * {@link JsonValueBinding} for {@link HtmlResKey} values.
 *
 * <p>
 * Delegates to {@link ResKeyJsonBinding} for the actual serialization, wrapping and unwrapping the
 * {@link ResKey} in a {@link DefaultHtmlResKey}.
 * </p>
 */
public class HtmlResKeyJsonBinding implements JsonValueBinding<HtmlResKey> {

	private final ResKeyJsonBinding _delegate = new ResKeyJsonBinding();

	@Override
	public HtmlResKey loadConfigItem(PropertyDescriptor property, JsonReader in, HtmlResKey baseValue)
			throws IOException, ConfigurationException {
		ResKey loadedKey = _delegate.loadConfigItem(property, in, content(baseValue));
		if (loadedKey == null) {
			return null;
		}
		return new DefaultHtmlResKey(loadedKey);
	}

	@Override
	public void saveConfigItem(PropertyDescriptor property, JsonWriter out, HtmlResKey item) throws IOException {
		_delegate.saveConfigItem(property, out, content(item));
	}

	@Override
	public Schema buildSchema(PropertyDescriptor property) {
		return _delegate.buildSchema(property);
	}

	private static ResKey content(HtmlResKey htmlResKey) {
		if (htmlResKey == null) {
			return null;
		}
		return ((DefaultHtmlResKey) htmlResKey).content();
	}

	@Override
	public boolean isLegalValue(Object value) {
		return value == null || value instanceof HtmlResKey;
	}

	@Override
	public HtmlResKey defaultValue() {
		return null;
	}

	@Override
	public Object normalize(Object value) {
		return value;
	}

}
