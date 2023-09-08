/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tooltip;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * A tool-tip that displays plain text.
 */
public final class PlainToolTip implements ToolTip {

	private final String _caption;

	private final String _tooltip;

	/**
	 * Creates a {@link PlainToolTip}.
	 */
	public PlainToolTip(String tooltip) {
		this(tooltip, null);
	}

	/**
	 * Creates a {@link PlainToolTip}.
	 */
	public PlainToolTip(String tooltip, String caption) {
		_tooltip = tooltip;
		_caption = caption;
	}

	@Override
	public boolean hasCaption() {
		return !(_caption == null || _caption.isBlank());
	}

	@Override
	public void writeCaption(DisplayContext context, TagWriter out) throws IOException {
		out.write(_caption);
	}

	@Override
	public void writeContent(DisplayContext context, TagWriter out) throws IOException {
		out.write(_tooltip);
	}
}