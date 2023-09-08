/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;

/**
 * A {@link PropertyUpdate} updating the client-side CSS class attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CssClassUpdate extends AbstractCssClassUpdate {

	private final String[] _cssClasses;

	/**
	 * Creates a {@link CssClassUpdate}.
	 * 
	 * @param elementID
	 *        See {@link #getElementID()}.
	 */
	public CssClassUpdate(String elementID, String... cssClasses) {
		super(elementID);
		_cssClasses = cssClasses;
	}

	@Override
	protected void writeCssClassContent(DisplayContext context, Appendable out) throws IOException {
		for (String cssClass : _cssClasses) {
			out.append(cssClass);
		}
	}

}
