/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import java.io.IOException;
import java.util.Locale;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.format.WikiWrite;

/**
 * Control renderer for {@link I18NStringField} fields that renders only the value of the current
 * {@link Locale}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStringActiveLanguageControlRenderer extends I18NFieldActiveLanguageControlRenderer {

	/** Instance of this class. */
	public static final I18NStringActiveLanguageControlRenderer INSTANCE =
		new I18NStringActiveLanguageControlRenderer(false);

	/** Instance of this class which renders the value multiline. */
	public static final I18NStringActiveLanguageControlRenderer MULTILINE_INSTANCE =
		new I18NStringActiveLanguageControlRenderer(true);

	private boolean _multiline;

	/**
	 * Creates a {@link I18NStringActiveLanguageControlRenderer}.
	 *
	 * @param multiline
	 *        Whether the field value must be rendered multiline.
	 */
	protected I18NStringActiveLanguageControlRenderer(boolean multiline) {
		_multiline = multiline;
	}

	@Override
	protected String getControlTag(CompositeControl control) {
		return SPAN;
	}

	@Override
	protected void writeFieldValue(DisplayContext context, TagWriter out, I18NField<?, ?, ?> i18nField)
			throws IOException {
		writeResKey(context, out, (ResKey) i18nField.getValue());
	}

	/**
	 * Renders the translation for the current language of the given {@link ResKey}.
	 */
	private void writeResKey(DisplayContext context, TagWriter out, ResKey resKey)
			throws IOException {
		if (resKey == null) {
			return;
		}
		String translation = context.getResources().getString(resKey, null);
		if (StringServices.isEmpty(translation)) {
			return;
		}
		if (_multiline) {
			WikiWrite.wikiWrite(out, translation);
		} else {
			out.writeText(translation);
		}
	}

}