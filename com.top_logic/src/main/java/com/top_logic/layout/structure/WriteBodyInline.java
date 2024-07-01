/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.io.IOException;

import jakarta.servlet.ServletException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link InlineContentView} calling the legacy write method
 * {@link LayoutComponent#writeBody(jakarta.servlet.ServletContext, jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, TagWriter) }.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WriteBodyInline extends InlineContentView {

	/**
	 * Creates a {@link WriteBodyInline}.
	 * 
	 * @param businessComponent
	 *        See {@link #getComponent()}.
	 */
	public WriteBodyInline(LayoutComponent businessComponent) {
		super(businessComponent);
	}

	@Override
	protected void renderComponent(DisplayContext context, TagWriter out) throws IOException,
			ServletException {
		getComponent().writeBody(context.asServletContext(), context.asRequest(), context.asResponse(), out);
	}

}
