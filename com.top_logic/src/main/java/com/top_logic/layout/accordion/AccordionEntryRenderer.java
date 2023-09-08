/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accordion;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.basic.link.AbstractLinkRenderer;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link Renderer} creating buttons in the style of entries of the {@link AccordionControl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AccordionEntryRenderer extends AbstractLinkRenderer {

	/**
	 * Singleton {@link AccordionEntryRenderer} instance.
	 */
	public static final AbstractLinkRenderer INSTANCE = new AccordionEntryRenderer();

	/**
	 * Creates a {@link AccordionEntryRenderer}.
	 */
	protected AccordionEntryRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeLink(DisplayContext context, TagWriter out, Link button) throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, button.getID());
		boolean visible = button.isVisible();
		if (visible) {
			writeTooltipAttributes(context, out, button);
			writeCssClasses(context, out, button);
		} else {
			out.writeAttribute(STYLE_ATTR, "display:none;");
		}
		out.endBeginTag();
		if (visible) {
			doWriteLink(context, out, button);
		}
		out.endTag(DIV);
	}

	@Override
	protected <B> void writeCssClassesContent(DisplayContext context, Appendable out, Link button) throws IOException {
		out.append(AccordionControl.NODE_CSS_CLASS);
		super.writeCssClassesContent(context, out, button);
	}

	/**
	 * Writes the direct content of the root-element marked {@link AccordionControl#NODE_CSS_CLASS}.
	 */
	protected void doWriteLink(DisplayContext context, TagWriter out, Link button) throws IOException {
		out.beginBeginTag(ANCHOR);
		out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
		if (button.isDisabled()) {
			out.writeAttribute(DISABLED_ATTR, DISABLED_DISABLED_VALUE);
		}
		out.beginCssClasses();
		{
			out.append(AccordionControl.ENTRY_CSS_CLASS);
		}
		out.endCssClasses();
		out.writeAttribute(ONCLICK_ATTR, button.getOnclick());
		out.endBeginTag();
		{
			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "accEntryContent");
			out.endBeginTag();
			{
				writeLinkContent(context, out, button);
			}
			out.endTag(SPAN);
		}
		out.endTag(ANCHOR);
	}

	/**
	 * Writes the content of the link element marked {@link AccordionControl#ENTRY_CSS_CLASS}.
	 */
	protected <B> void writeLinkContent(DisplayContext context, TagWriter out, Link button) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, "accLabel");
		out.endBeginTag();
		{
			writeImage(context, out, button);

			out.beginBeginTag(SPAN);
			out.writeAttribute(CLASS_ATTR, "accText");
			out.endBeginTag();
			{
				out.writeText(button.getLabel());
			}
			out.endTag(SPAN);
		}
		out.endTag(SPAN);
	}

	/**
	 * Writes the type image element.
	 */
	protected <B> void writeImage(DisplayContext context, TagWriter out, Link button)
			throws IOException {
		ThemeImage image = button.getImage();
		if (image == null) {
			return;
		}

		XMLTag icon = image.toIcon();
		icon.beginBeginTag(context, out);
		out.beginCssClasses();
		{
			out.append(AccordionControl.ICON_CSS_CLASS);
			out.append(FormConstants.INPUT_IMAGE_CSS_CLASS);
			out.append(button.isDisabled() ? FormConstants.DISABLED_CSS_CLASS : null);
		}
		out.endCssClasses();
		out.writeAttribute(ALT_ATTR, button.getAltText());
		out.writeAttribute(TITLE_ATTR, "");
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, button.getLabel());
		icon.endEmptyTag(context, out);
	}

}
