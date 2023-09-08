/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.DefaultView;

/**
 * {@link View} that renders the {@link MessageBox} content area.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MessageBoxContentView extends DefaultView {

	private final HTMLFragment content;

	/**
	 * Creates a {@link MessageBoxContentView}.
	 * 
	 * @param content
	 *        The content to render within this {@link MessageBoxContentView}.
	 */
	public MessageBoxContentView(HTMLFragment content) {
		this.content = content;
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(CLASS_ATTR, MessageBox.CSS_CLASS_MBOX_CONTENT);
		out.writeAttribute(TL_COMPLEX_WIDGET_ATTR, "true");
		out.endBeginTag();
		{
			writeForm(context, out);
		}
		out.endTag(DIV);
	}

	/**
	 * Make sure to activate first input element.
	 */
	protected void writeForm(DisplayContext context, TagWriter out) throws IOException {
		// Note: The form is required, because selectFirst() chooses the
		// first input in the first form element in the current document.
		out.beginBeginTag(FORM);
		out.writeAttribute(ACTION_ATTR, "#");
		// if no onsubmit exists it seems that the browser tries to reload the main layout.
		out.writeAttribute(ONSUBMIT_ATTR, "return false;");
		out.endBeginTag();
		{
			writeContent(context, out);
		}
		out.endTag(FORM);
		
		// Activate the first element in the form written above.
		out.beginScript();
		out.append("selectFirst();");
		out.endScript();
	}

	/**
	 * Output nested contents.
	 */
	protected void writeContent(DisplayContext context, TagWriter out) throws IOException {
		content.write(context, out);
	}

}