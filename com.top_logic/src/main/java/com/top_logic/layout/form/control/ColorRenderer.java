/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.awt.Color;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * {@link Renderer} for {@link Color} values.
 */
public class ColorRenderer implements Renderer<Color> {

	/**
	 * Singleton {@link ColorRenderer} instance.
	 */
	public static final ColorRenderer INSTANCE = new ColorRenderer();

	private ColorRenderer() {
		// Singleton constructor.
	}

	@Override
	public void write(DisplayContext context, TagWriter out, Color value) throws IOException {
		String bgColor = ColorChooserControl.toHtmlColor(value);
		out.beginBeginTag(SPAN);
		out.beginAttribute(STYLE_ATTR);
		out.append("background-color:");
		out.append(bgColor);
		out.append("; cursor:default;");
		out.endAttribute();
		out.endBeginTag();

		out.writeText(NBSP);
		out.writeText(NBSP);
		out.writeText(NBSP);
		out.writeText(NBSP);
		out.writeText(NBSP);

		out.endTag(SPAN);
	}

}
