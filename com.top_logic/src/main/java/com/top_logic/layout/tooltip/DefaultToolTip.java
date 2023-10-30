/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tooltip;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * A {@link ToolTip} composed of two fragments, caption and content.
 */
public final class DefaultToolTip implements ToolTip {
	private final HTMLFragment _content;

	private final HTMLFragment _caption;

	/**
	 * Creates a {@link DefaultToolTip}.
	 */
	public DefaultToolTip(HTMLFragment content) {
		this(content, null);
	}

	/** 
	 * Creates a {@link DefaultToolTip}.
	 */
	public DefaultToolTip(HTMLFragment content, HTMLFragment caption) {
		_content = content;
		_caption = caption;
	}

	@Override
	public void writeContent(DisplayContext context, TagWriter out) throws IOException {
		_content.write(context, out);
	}

	@Override
	public boolean hasCaption() {
		return _caption != null;
	}

	@Override
	public void writeCaption(DisplayContext context, TagWriter out) throws IOException {
		_caption.write(context, out);
	}

}