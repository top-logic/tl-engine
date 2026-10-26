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
import com.top_logic.layout.wysiwyg.ui.StructuredTextPDFRenderer;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * {@link Renderer} displaying {@link I18NStructuredText} in the current users locale.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NStructuredTextPDFRenderer implements PDFRenderer {

	/**
	 * Singleton {@link I18NStructuredTextPDFRenderer} instance.
	 */
	public static final I18NStructuredTextPDFRenderer INSTANCE = new I18NStructuredTextPDFRenderer();

	private I18NStructuredTextPDFRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		if (value == null) {
			return;
		}
		StructuredTextPDFRenderer.INSTANCE.write(context, out, contextObject, ((I18NStructuredText) value).localize());
	}

}
