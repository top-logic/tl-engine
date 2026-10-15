/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.Map;

import org.apache.poi.sl.usermodel.Insets2D;

/**
 * The effective style of the first text level of a text shape in a presentation template.
 *
 * <p>
 * The style is the one shown by PowerPoint: Properties not set at the text itself are inherited
 * from the shape, the layout placeholder, the master placeholder, the master text styles and the
 * theme fonts.
 * </p>
 *
 * @param font
 *        The font family, theme font references resolved.
 * @param size
 *        The font size in points, <code>null</code> if not specified.
 * @param bold
 *        Whether the text is bold.
 * @param italic
 *        Whether the text is italic.
 * @param color
 *        The text color, <code>null</code> if not specified.
 * @param align
 *        The horizontal alignment (<code>left</code>, <code>center</code>, <code>right</code>,
 *        <code>justify</code>, ...).
 * @param caps
 *        Whether the text is shown in capital letters.
 * @param anchor
 *        The vertical alignment of the text in its shape (<code>top</code>, <code>middle</code>,
 *        <code>bottom</code>).
 * @param insets
 *        The inner margins of the shape in points.
 * @param characterSpacing
 *        Additional space between characters in points, <code>null</code> if not specified.
 * @param lineSpacing
 *        The line spacing as multiple of the single line height, <code>null</code> if not specified
 *        as percentage.
 */
public record TextStyle(String font, Double size, boolean bold, boolean italic, ColorInfo color, String align,
		boolean caps, String anchor, Insets2D insets, Double characterSpacing, Double lineSpacing) {

	/**
	 * The data representation with the keys {@link TemplateData#FONT}, {@link TemplateData#SIZE},
	 * {@link TemplateData#BOLD}, {@link TemplateData#ITALIC}, {@link TemplateData#COLOR},
	 * {@link TemplateData#ALIGN}, {@link TemplateData#CAPS}, {@link TemplateData#ANCHOR},
	 * {@link TemplateData#INSETS}, {@link TemplateData#CHARACTER_SPACING} and
	 * {@link TemplateData#LINE_SPACING}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = TemplateData.map();
		TemplateData.put(result, TemplateData.FONT, font);
		TemplateData.put(result, TemplateData.SIZE, size == null ? null : TemplateData.number(size));
		result.put(TemplateData.BOLD, Boolean.valueOf(bold));
		result.put(TemplateData.ITALIC, Boolean.valueOf(italic));
		TemplateData.put(result, TemplateData.COLOR, color == null ? null : color.toData());
		TemplateData.put(result, TemplateData.ALIGN, align);
		result.put(TemplateData.CAPS, Boolean.valueOf(caps));
		TemplateData.put(result, TemplateData.ANCHOR, anchor);
		if (insets != null) {
			Map<String, Object> margins = TemplateData.map();
			margins.put(TemplateData.LEFT, TemplateData.number(insets.left));
			margins.put(TemplateData.TOP, TemplateData.number(insets.top));
			margins.put(TemplateData.RIGHT, TemplateData.number(insets.right));
			margins.put(TemplateData.BOTTOM, TemplateData.number(insets.bottom));
			result.put(TemplateData.INSETS, margins);
		}
		TemplateData.put(result, TemplateData.CHARACTER_SPACING,
			characterSpacing == null ? null : TemplateData.number(characterSpacing));
		TemplateData.put(result, TemplateData.LINE_SPACING,
			lineSpacing == null ? null : TemplateData.fraction(lineSpacing));
		return result;
	}

}
