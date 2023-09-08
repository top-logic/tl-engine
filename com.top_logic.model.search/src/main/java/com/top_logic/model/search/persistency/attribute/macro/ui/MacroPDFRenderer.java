/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro.ui;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.tool.export.pdf.PDFRenderer;

/**
 * {@link PDFRenderer} for macro {@link SearchExpression}.
 * 
 * @see MacroDisplayControl
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MacroPDFRenderer implements PDFRenderer {

	@Override
	public void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException {
		if (!(value instanceof SearchExpression)) {
			return;
		}
		out.beginTag(HTMLConstants.DIV);
		{
			MacroDisplayControl.writeEvaluatedMacro(context, out, contextObject, (SearchExpression) value);
		}
		out.endTag(HTMLConstants.DIV);

	}

}

