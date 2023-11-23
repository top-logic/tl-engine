/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import java.io.IOException;
import java.util.Locale;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;

/**
 * Control renderer for {@link I18NStringField} fields that renders only the value of the current
 * {@link Locale}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStringActiveLanguageControlRenderer extends I18NFieldActiveLanguageControlRenderer {

	/** Instance of this class. */
	public static final I18NStringActiveLanguageControlRenderer INSTANCE =
		new I18NStringActiveLanguageControlRenderer();

	@Override
	protected String getControlTag(CompositeControl control) {
		return SPAN;
	}

	@Override
	protected void writeFieldValue(DisplayContext context, TagWriter out, I18NField<?, ?, ?> i18nField)
			throws IOException {
		boolean multiline = ((I18NStringField) i18nField).isMultiline();
		InternationalizationEditor.writeResKey(context, out, (ResKey) i18nField.getValue(), multiline);
	}

}