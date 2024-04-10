/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.i18n.I18NField;
import com.top_logic.element.i18n.I18NStringTagProvider.I18NFieldActiveLanguageControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * {@link I18NFieldActiveLanguageControlRenderer} rendering {@link I18NStructuredTextField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStructuedTextActiveLanguageControlRenderer extends I18NFieldActiveLanguageControlRenderer {

	/** Instance of this class. */
	public static final I18NStructuedTextActiveLanguageControlRenderer INSTANCE =
		new I18NStructuedTextActiveLanguageControlRenderer();

	@Override
	protected void writeFieldValue(DisplayContext context, TagWriter out, I18NField<?, ?, ?> i18nField)
			throws IOException {
		I18NStructuredText i18nValue = (I18NStructuredText) i18nField.getValue();
		if (i18nValue == null) {
			return;
		}
		LabelProviderService.getInstance().getRenderer(i18nValue).write(context, out, i18nValue);
	}

}