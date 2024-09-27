/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.io.IOException;

import com.lowagie.text.html.HtmlEncoder;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Render a special image for an exception.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExceptionRenderer implements Renderer<String> {

	/**
	 * Singleton {@link ExceptionRenderer} instance.
	 */
	public static final ExceptionRenderer INSTANCE = new ExceptionRenderer();

	private ExceptionRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext aContext, TagWriter anOut, String aValue) throws IOException {
		if (StringServices.isEmpty(aValue)) {
			return;
		}
		ResKey tooltip = ResKey.text(HtmlEncoder.encode(aValue));

		Icons.EXCEPTION.writeWithTooltip(aContext, anOut, tooltip);
	}
}

