/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;

/**
 * Control renderer for {@link I18NStringField} fields that renders only the value of the current
 * {@link Locale}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStringActiveLanguageControlRenderer extends DefaultControlRenderer<CompositeControl> {

	/** Instance of this class. */
	public static final I18NStringActiveLanguageControlRenderer INSTANCE =
		new I18NStringActiveLanguageControlRenderer();

	@Override
	protected String getControlTag(CompositeControl control) {
		return SPAN;
	}

	@Override
	protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
			throws IOException {
		Locale currentLocale = context.getResources().getLocale();
		List<? extends HTMLFragment> controls = control.getChildren();
		int i = 0;
		I18NField<?, ?, ?> i18nField = (I18NField<?, ?, ?>) control.getModel();
		for (FormField field : i18nField.getLanguageFields()) {
			Locale fieldLocale = I18NTranslationUtil.getLocaleFromField(field);
			HTMLFragment fieldControl = controls.get(i++);
			if (!I18NTranslationUtil.equalLanguage(currentLocale, fieldLocale)) {
				continue;
			}
			fieldControl.write(context, out);
		}
	}

}