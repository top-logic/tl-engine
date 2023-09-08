/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;

/**
 * The message area with message type icon of a {@link MessageBox}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class MessageArea implements HTMLFragment {
	private final HTMLFragment icon;

	/**
	 * Creates a {@link MessageArea}.
	 *
	 * @param icon The icon view to display in the message area.
	 */
	public MessageArea(HTMLFragment icon) {
		this.icon = icon;
	}

	@Override
	public void write(DisplayContext renderContext, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, MessageBox.CSS_CLASS_MBOX_IMAGE);
		out.endBeginTag();
		icon.write(renderContext, out);
		out.endTag(DIV);
		
		writeMessage(renderContext, out);
	}

	/**
	 * Renders the actual message contents.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param out
	 *        The output to render to.
	 */
	protected abstract void writeMessage(DisplayContext context, TagWriter out) throws IOException;
	
}