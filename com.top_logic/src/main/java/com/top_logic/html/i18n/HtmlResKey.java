/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.i18n;

import java.util.Locale;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Representation of a resource key for an HTML source code.
 * 
 * @see ResKey
 * @see HtmlResKey1
 * @see HtmlResKey2
 * @see HtmlResKey3
 * @see HtmlResKey4
 * @see HtmlResKey5
 * @see HtmlResKeyN
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Binding(DefaultHtmlResKey.ValueBinding.class)
@Label("HTML resource key")
public interface HtmlResKey {

	/**
	 * {@link HTMLFragment HTML} for the given {@link Locale} represented by this
	 * {@link HtmlResKey}.
	 *
	 * @param locale
	 *        The locale to get content for.
	 * @return An {@link HTMLFragment} rendering the HTML for the given locale.
	 */
	default HTMLFragment getHtml(Locale locale) {
		return getHtml(Resources.getInstance(locale));
	}

	/**
	 * {@link HTMLFragment HTML} stored by the given {@link Resources} represented by this
	 * {@link HtmlResKey}.
	 *
	 * @param resources
	 *        {@link Resources} to get HTML code for.
	 * @return An {@link HTMLFragment} rendering the HTML for the given {@link Resources}.
	 */
	HTMLFragment getHtml(Resources resources);

}
