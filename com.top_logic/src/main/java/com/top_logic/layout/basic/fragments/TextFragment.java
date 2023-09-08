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
 * {@link HTMLFragment} rendering a plain text.
 * 
 * @see EmptyFragment
 * @see TextFragment
 * @see MessageFragment
 * @see RenderedFragment
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextFragment implements HTMLFragment {

	/**
	 * @see Fragments#text(String)
	 */
	static TextFragment createTextFragment(String text) {
		return new TextFragment(text);
	}

	private final String _text;

	/**
	 * Creates a {@link TextFragment}.
	 * 
	 * @param text
	 *        The plain text to be rendered as XML text content (with additional quoting).
	 * 
	 */
	TextFragment(String text) {
		_text = text;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.writeText(_text);
	}

}
