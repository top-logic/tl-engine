/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DefaultView;

/**
 * {@link View} that writes a given text.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TextView extends DefaultView {

	private final CharSequence _text;

	/**
	 * Creates a new {@link TextView}.
	 * 
	 * @param text
	 *        The text to write.
	 */
	public TextView(CharSequence text) {
		_text = text;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.writeText(_text);
	}
	
	@Override
	public String toString() {
		return "TextView [text=" + _text + "]";
	}

}


