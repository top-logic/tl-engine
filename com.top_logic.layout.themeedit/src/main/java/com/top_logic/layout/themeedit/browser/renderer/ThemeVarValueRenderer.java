/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.renderer;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.awt.Color;
import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.ThemeImageRenderer;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.themeedit.browser.resource.ThemeResourceImage;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * {@link Renderer} with special handling for {@link Color} and {@link ThemeImage}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeVarValueRenderer implements Renderer<Object> {

	/**
	 * Singleton {@link ThemeVarValueRenderer} instance.
	 */
	public static final ThemeVarValueRenderer INSTANCE = new ThemeVarValueRenderer();

	private ThemeVarValueRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Object value) throws IOException {
		if (value instanceof Color) {
			writeColor(context, out, value);
		} else if (value instanceof ThemeImage) {
			ThemeImageRenderer.INSTANCE.write(context, out, (ThemeImage) value);
		} else if (value instanceof ThemeResourceImage) {
			ThemeImage themeImage = ((ThemeResourceImage) value).getImage();
			ResKey tooltip = ((ThemeResourceImage) value).getTooltip();

			ThemeUtil.writeThemeImage(context, out, themeImage, tooltip);
		} else {
			LabelProviderService.getInstance().getRenderer(value).write(context, out, value);
		}
	}

	private void writeColor(DisplayContext context, TagWriter out, Object value) throws IOException {
		ResKey colorSpec = ResKey.text(ColorFormat.formatColor((Color) value));
		out.beginBeginTag(SPAN);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, colorSpec);
		out.writeAttribute(CLASS_ATTR, "colorDisplay");
		out.writeAttribute(STYLE_ATTR, "background-color: " + colorSpec);
		out.endBeginTag();
		out.endTag(SPAN);
	}
}