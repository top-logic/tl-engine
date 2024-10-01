/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.basic.link.AbstractLinkRenderer;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.form.FormConstants;

/**
 * {@link Renderer} that creates a link representation compatible with toolbar row.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ToolRowCommandRenderer extends AbstractLinkRenderer {

	/**
	 * Singleton {@link ToolRowCommandRenderer} instance.
	 */
	public static final ToolRowCommandRenderer INSTANCE = new ToolRowCommandRenderer();

	private ToolRowCommandRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeLink(DisplayContext context, TagWriter out, Link button) throws IOException {
		if (button.isVisible()) {
			out.beginBeginTag(SPAN);
			out.writeAttribute(ID_ATTR, button.getID());
			writeCssClasses(context, out, button);
			writeTooltipAttributes(context, out, button);
			out.endBeginTag();
			{
				ThemeImage image = button.getImage();
				if (image != null) {
					XMLTag tag = image.toButton();
					tag.beginBeginTag(context, out);
					out.writeAttribute(CLASS_ATTR, FormConstants.INPUT_IMAGE_CSS_CLASS);
					writeDisabledAttribute(out, button);
					out.writeAttribute(ALT_ATTR, StringServices.nonNull(context.getResources().getStringOptional(button.getAltText())));
					out.writeAttribute(ONCLICK_ATTR, button.getOnclick());
					tag.endEmptyTag(context, out);
				} else {
					out.beginBeginTag(ANCHOR);
					writeDisabledAttribute(out, button);
					out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
					out.writeAttribute(ONCLICK_ATTR, button.getOnclick());
					out.endBeginTag();
					{
						String label = context.getResources().getString(button.getLabel());
						out.writeText(label);
					}
					out.endTag(ANCHOR);
				}
			}
			out.endTag(SPAN);
		}
	}

	@Override
	protected <B> void writeCssClassesContent(DisplayContext context, Appendable out, Link button) throws IOException {
		out.append("trbEntry");
		super.writeCssClassesContent(context, out, button);
	}

	private <B> void writeDisabledAttribute(TagWriter out, Link button) {
		if (button.isDisabled()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
	}

}
