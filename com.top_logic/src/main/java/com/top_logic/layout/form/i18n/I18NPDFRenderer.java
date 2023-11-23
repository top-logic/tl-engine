/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * {@link PDFRenderer} for {@link ResKey}s.
 */
public class I18NPDFRenderer implements PDFRenderer {

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		if (value != null) {
			out.writeText(context.getResources().getString((ResKey) value));
		}
	}

}
