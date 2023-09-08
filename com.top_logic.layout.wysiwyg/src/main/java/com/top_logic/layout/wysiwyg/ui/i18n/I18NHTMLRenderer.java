/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.wysiwyg.ui.HTMLRenderer;

/**
 * {@link Renderer} displaying {@link I18NStructuredText} in the current users locale.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NHTMLRenderer implements Renderer<I18NStructuredText> {

	/**
	 * Singleton {@link I18NHTMLRenderer} instance.
	 */
	public static final I18NHTMLRenderer INSTANCE = new I18NHTMLRenderer();

	private I18NHTMLRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, I18NStructuredText value) throws IOException {
		if (value == null) {
			return;
		}
		HTMLRenderer.INSTANCE.write(context, out, value.localize());
	}

}
