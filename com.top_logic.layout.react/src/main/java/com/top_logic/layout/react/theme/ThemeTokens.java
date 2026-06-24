/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Emits the design tokens of the active theme as CSS custom properties for the new React UI.
 *
 * <p>
 * This is the single, intentional integration point between the React UI and the theme system: the
 * UI styles its components against {@code var(--token)} references whose values are the
 * CSS-relevant settings of the active theme. Nothing else of the theme system reaches the React UI -
 * no compiled component styles and no file overlay.
 * </p>
 */
public class ThemeTokens {

	private static final String CSS_TYPE = "text/css";

	/**
	 * Writes a {@code <style>} element defining the active theme's design tokens as {@code :root}
	 * CSS custom properties.
	 *
	 * @param out
	 *        The writer of the HTML {@code <head>}.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeRoot(TagWriter out) throws IOException {
		out.beginBeginTag(HTMLConstants.STYLE_ELEMENT);
		out.writeAttribute(HTMLConstants.TYPE_ATTR, CSS_TYPE);
		out.endBeginTag();

		out.writeContent(":root {");
		ThemeFactory.getTheme().getSettings().getSettings().stream()
			.filter(ThemeSetting::isCssRelevant)
			.filter(setting -> setting.getValue() != null)
			.filter(setting -> !setting.getName().startsWith(ThemeImage.MIME_PREFIX))
			.sorted((s1, s2) -> s1.getLocalName().compareTo(s2.getLocalName()))
			.forEach(setting -> writeVariable(out, setting));
		out.writeContent("}");

		out.endTag(HTMLConstants.STYLE_ELEMENT);
	}

	private static void writeVariable(TagWriter out, ThemeSetting setting) {
		try {
			out.writeContent("--");
			out.writeContent(setting.getLocalName());
			out.writeContent(":");
			out.writeContent(setting.getCssValue());
			out.writeContent(";");
		} catch (IOException ex) {
			throw new RuntimeException("Failed to write theme token: " + setting.getLocalName(), ex);
		}
	}

}
