/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.i18n;

import com.top_logic.basic.config.annotation.Binding;

/**
 * A {@link HtmlResKey} generator with variable number of arguments.
 * 
 * @see HtmlResKey
 * @see HtmlResKey1
 * @see HtmlResKey2
 * @see HtmlResKey3
 * @see HtmlResKey4
 * @see HtmlResKey5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Binding(DefaultHtmlResKey.ValueBinding.class)
public interface HtmlResKeyN {

	/**
	 * Creates a {@link HtmlResKey} using the given objects as dynamic part of the HTML source.
	 * 
	 * @param arguments
	 *        Dynamic part of the source.
	 * @return The {@link HtmlResKey} that encapsulates the given arguments.
	 */
	HtmlResKey fill(Object... arguments);

}
