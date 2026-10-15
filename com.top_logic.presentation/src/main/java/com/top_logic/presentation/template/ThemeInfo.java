/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.List;
import java.util.Map;

/**
 * The theme of a slide master of a presentation template.
 *
 * @param name
 *        The name of the theme.
 * @param colorScheme
 *        The name of the color scheme.
 * @param colors
 *        The theme colors as <code>RRGGBB</code> by their names in {@link #COLOR_NAMES}.
 * @param majorFont
 *        The latin font for headings.
 * @param minorFont
 *        The latin font for body text.
 */
public record ThemeInfo(String name, String colorScheme, Map<String, String> colors, String majorFont,
		String minorFont) {

	/**
	 * The names of the colors of a color scheme.
	 */
	public static final List<String> COLOR_NAMES = List.of("dk1", "lt1", "dk2", "lt2",
		"accent1", "accent2", "accent3", "accent4", "accent5", "accent6", "hlink", "folHlink");

	/**
	 * The data representation with the keys {@link TemplateData#NAME},
	 * {@link TemplateData#COLOR_SCHEME}, {@link TemplateData#COLORS} and {@link TemplateData#FONTS}
	 * (with {@link TemplateData#MAJOR} and {@link TemplateData#MINOR}).
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		TemplateData.put(result, TemplateData.NAME, name);
		TemplateData.put(result, TemplateData.COLOR_SCHEME, colorScheme);
		Map<String, Object> colorData = TemplateData.map();
		colorData.putAll(colors);
		result.put(TemplateData.COLORS, colorData);
		Map<String, Object> fonts = TemplateData.map();
		TemplateData.put(fonts, TemplateData.MAJOR, majorFont);
		TemplateData.put(fonts, TemplateData.MINOR, minorFont);
		result.put(TemplateData.FONTS, fonts);
		return result;
	}

}
