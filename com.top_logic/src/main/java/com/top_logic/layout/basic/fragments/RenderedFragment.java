/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * {@link HTMLFragment} writing a pre-computed XML string.
 * 
 * @see EmptyFragment
 * @see TextFragment
 * @see MessageFragment
 * @see RenderedFragment
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RenderedFragment implements HTMLFragment {

	/**
	 * @see Fragments#htmlSource(String)
	 */
	static RenderedFragment createRenderedFragment(String renderedContent) {
		return new RenderedFragment(renderedContent);
	}

	private final String _renderedContent;

	/**
	 * Creates a {@link RenderedFragment}.
	 * 
	 * @param renderedContent
	 *        The XML fragment content to render without further quoting.
	 */
	RenderedFragment(String renderedContent) {
		_renderedContent = renderedContent;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.writeContent(_renderedContent);
	}

}
