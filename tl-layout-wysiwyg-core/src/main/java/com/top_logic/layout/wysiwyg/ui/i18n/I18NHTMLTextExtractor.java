/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.wysiwyg.ui.HTMLTextExtractor;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link LabelProvider} extracting the text content of the localized version of a given
 * {@link I18NStructuredText} object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NHTMLTextExtractor extends HTMLTextExtractor {

	/**
	 * Singleton {@link I18NHTMLTextExtractor} instance.
	 */
	@SuppressWarnings("hiding")
	public static final I18NHTMLTextExtractor INSTANCE = new I18NHTMLTextExtractor();

	private I18NHTMLTextExtractor() {
		// Singleton constructor.
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof I18NStructuredText) {
			I18NStructuredText i18nStructuredText = (I18NStructuredText) object;
			StructuredText localization = i18nStructuredText.localize();
			if (localization == null) {
				return null;
			}
			return extractText(localization);
		} else {
			return super.getLabel(object);
		}
	}

}