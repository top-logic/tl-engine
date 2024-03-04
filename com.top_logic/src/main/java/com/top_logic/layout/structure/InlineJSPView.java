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
 * {@link InlineContentView} that renders a component by a separate JSP.
 * 
 * @see #getPage()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InlineJSPView extends InlineContentView {

	private final String _page;

	/**
	 * Creates a {@link InlineJSPView}.
	 * 
	 * @param businessComponent
	 *        See {@link #getComponent()}.
	 * @param page
	 *        See {@link #getPage()}.
	 */
	public InlineJSPView(LayoutComponent businessComponent, String page) {
		super(businessComponent);

		_page = page;
	}

	/**
	 * The path to the page to render.
	 */
	public String getPage() {
		return _page;
	}

	@Override
	protected void renderComponent(DisplayContext context, TagWriter out)
			throws IOException, ServletException {
		out.flushBuffer();
		context.asServletContext().getRequestDispatcher(_page).include(context.asRequest(), context.asResponse());
	}

}
