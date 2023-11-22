/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.i18n;

import com.top_logic.basic.config.annotation.Binding;

/**
 * A {@link HtmlResKey} generator accepting exactly two arguments.
 * 
 * @see HtmlResKey
 * @see HtmlResKey1
 * @see HtmlResKey3
 * @see HtmlResKey4
 * @see HtmlResKey5
 * @see HtmlResKeyN
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Binding(DefaultHtmlResKey.ValueBinding.class)
public interface HtmlResKey2 {

	/**
	 * Creates a {@link HtmlResKey} using the given objects as dynamic part of the HTML source.
	 * 
	 * @param arg1
	 *        First dynamic part of the source. May be <code>null</code>.
	 * @param arg2
	 *        Second dynamic part of the source. May be <code>null</code>.
	 * 
	 * @return The {@link HtmlResKey} that encapsulates the given arguments.
	 */
	HtmlResKey fill(Object arg1, Object arg2);

}
