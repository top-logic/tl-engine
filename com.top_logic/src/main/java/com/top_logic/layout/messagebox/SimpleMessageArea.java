/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * {@link MessageArea} that renders a plain text message.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class SimpleMessageArea extends MessageArea {
	private final String message;

	/**
	 * Creates a {@link SimpleMessageArea}.
	 * 
	 * @param icon
	 *        See {@link MessageArea#MessageArea(HTMLFragment)}.
	 * @param message
	 *        The message to display.
	 */
	public SimpleMessageArea(HTMLFragment icon, String message) {
		super(icon);
		this.message = message;
	}

	@Override
	protected void writeMessage(DisplayContext context, TagWriter out) throws IOException {
		out.writeText(message);
	}
}