/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template.model;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.Theme;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.template.WithProperties;

/**
 * Template for a {@link Theme}-defined image.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Image extends AbstractFragment {

	private final ThemeImage _key;

	Image(ThemeImage key, HTMLTemplateFragment contents) {
		super(contents);

		_key = key;
	}

	/**
	 * The {@link Theme} image key.
	 */
	public ThemeImage getKey() {
		return _key;
	}

	@Override
	public void write(DisplayContext displayContext, TagWriter out, WithProperties properties) throws IOException {
		XMLTag tag = getKey().toIcon();
		try {
			tag.beginBeginTag(displayContext, out);
			getContents().write(displayContext, out, properties);
			tag.endEmptyTag(displayContext, out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

}
