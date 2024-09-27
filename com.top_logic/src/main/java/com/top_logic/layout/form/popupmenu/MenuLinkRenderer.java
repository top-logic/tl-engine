/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.popupmenu;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.link.Link;
import com.top_logic.layout.basic.link.LinkRenderer;
import com.top_logic.layout.form.control.ButtonRenderer;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link LinkRenderer} for menu entries.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MenuLinkRenderer implements LinkRenderer {

	/**
	 * Singleton {@link MenuLinkRenderer} instance.
	 */
	public static final MenuLinkRenderer INSTANCE = new MenuLinkRenderer();

	private static final String MENU_ENTRY_CELL_CLASS = "menuEntryCell";

	private static final String MENU_ENTRY_DISABLED_CLASS = "menuEntryDisabled";

	private static final String MENU_ENTRY_ENABLED_CLASS = "menuEntryEnabled";

	private static final String MENU_ENTRY_IMAGE_CLASS = "menuEntryImage";

	private MenuLinkRenderer() {
		// Singleton constructor.
	}

	@Override
	public void writeLink(DisplayContext context, TagWriter out, Link button) throws IOException {
		out.beginBeginTag(ANCHOR);
		out.writeAttribute(HREF_ATTR, HREF_EMPTY_LINK);
		out.writeAttribute(ID_ATTR, button.getID());
		if (button.isVisible()) {
			out.beginCssClasses();
			button.writeCssClassesContent(out);
			out.endCssClasses();

			String tooltip = ButtonRenderer.lookupTooltipLabelFallback(button, true, true);
			OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributes(context, out, ResKey.text(tooltip),
				button.getTooltipCaption());

			if (!button.isDisabled()) {
				out.writeAttribute(ONCLICK_ATTR, button.getOnclick());
			}
			out.endBeginTag();
			writeImage(out, button, context);
			writeLabel(context, out, button);
		} else {
			out.writeAttribute(STYLE_ATTR, "display: none;");
			out.endBeginTag();
		}
		out.endTag(ANCHOR);
	}

	private <B> void writeLabel(DisplayContext context, TagWriter out, Link button) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, MENU_ENTRY_CELL_CLASS);
		out.endBeginTag();
		out.writeText(context.getResources().getString(button.getLabel()));
		out.endTag(SPAN);
	}

	private <B> void writeImage(TagWriter out, Link button, DisplayContext context) throws IOException {
		ThemeImage buttonImage = lookUpImage(button, button.isDisabled());
		out.beginBeginTag(SPAN);
		out.writeAttribute(CLASS_ATTR, MENU_ENTRY_CELL_CLASS);
		out.endBeginTag();
		if (buttonImage != null) {
			buttonImage.writeWithCss(context, out, MENU_ENTRY_IMAGE_CLASS);
		}
		out.endTag(SPAN);
	}

	private <B> ThemeImage lookUpImage(Link button, boolean isDisabled) {
		ThemeImage image = isDisabled ? disabledImage(button) : button.getImage();
		if (image == null) {
			return Icons.BUTTON_EMPTY;
		}
		return image;
	}

	private ThemeImage disabledImage(Link button) {
		ThemeImage result = button.getDisabledImage();
		if (result == null) {
			return button.getImage();
		}
		return result;
	}

	static String getCssClass(Link self) {
		return self.isDisabled() ? MENU_ENTRY_DISABLED_CLASS : MENU_ENTRY_ENABLED_CLASS;
	}

}
