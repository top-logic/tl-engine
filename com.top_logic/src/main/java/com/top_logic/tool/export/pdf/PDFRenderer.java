/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export.pdf;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;

/**
 * Renderer for PDF.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface PDFRenderer {

	/**
	 * Writes HTML to export as PDF.
	 * 
	 * <p>
	 * The given value is exported as PDF. It is not possible to renderer {@link Control}s.
	 * </p>
	 * 
	 * @param out
	 *        {@link TagWriter} to write HTML to export to PDF to.
	 * @param contextObject
	 *        Context object which holds the given value.
	 * @param value
	 *        The value object to export as PDF. May be <code>null</code>.
	 */
	void write(DisplayContext context, TagWriter out, Object contextObject, Object value) throws IOException;

}

