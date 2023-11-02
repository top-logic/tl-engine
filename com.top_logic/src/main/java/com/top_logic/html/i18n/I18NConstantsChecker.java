/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.i18n;

import java.lang.reflect.Field;

import com.top_logic.basic.Logger;
import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.I18NBundle;

/**
 * {@link com.top_logic.basic.i18n.I18NConstantsChecker} checking also translations for HTML
 * resources in all {@link I18NConstantsBase}.
 * 
 * @see HtmlResKey
 * @see HtmlResKey1
 * @see HtmlResKey2
 * @see HtmlResKey3
 * @see HtmlResKey4
 * @see HtmlResKey5
 * @see HtmlResKeyN
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstantsChecker extends com.top_logic.basic.i18n.I18NConstantsChecker {

	@Override
	protected void checkField(Field field, I18NBundle checkingBundle) {
		Class<?> type = field.getType();
		if (type != HtmlResKey.class &&
				type != HtmlResKey1.class &&
				type != HtmlResKey2.class &&
				type != HtmlResKey3.class &&
				type != HtmlResKey4.class &&
				type != HtmlResKey5.class &&
				type != HtmlResKeyN.class) {
			super.checkField(field, checkingBundle);
			return;
		}
		try {
			/* NOTE: Also if the type is HtmlResKey, HtmlResKey1, HtmlResKey2, HtmlResKey3,
			 * HtmlResKey4, HtmlResKey5, or HtmlResKeyN, the cast to DefaultHtmlResKey is correct,
			 * as actually DefaultHtmlResKey is the single implementation of these interfaces. */
			DefaultHtmlResKey key = (DefaultHtmlResKey) field.get(null);
			// Add key to missing resources file, or log failure, if the application is
			// configured to do so.
			checkingBundle.getString(key.content());
		} catch (IllegalArgumentException | IllegalAccessException ex) {
			Logger.error("Error accessing I18N constant '" + qName(field) + "'.", ex, I18NConstantsChecker.class);
		}
	}

}
