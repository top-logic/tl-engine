/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * PDF renderer for {@link StructuredText}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructuredTextPDFRenderer implements PDFRenderer {

	/**
	 * Singleton {@link StructuredTextPDFRenderer} instance.
	 */
	public static final StructuredTextPDFRenderer INSTANCE = new StructuredTextPDFRenderer();

	private StructuredTextPDFRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		if (value == null) {
			return;
		}

		out.writeContent(StructuredTextUtil.getCodeWithInlinedImages((StructuredText) value));
	}

}
