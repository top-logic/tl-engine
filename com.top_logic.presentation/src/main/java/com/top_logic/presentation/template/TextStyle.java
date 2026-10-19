/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.template;

import java.util.Map;

import org.apache.poi.sl.usermodel.Insets2D;

import com.top_logic.presentation.data.PresentationData;

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
	 * The data representation with the keys {@link PresentationData#FONT}, {@link PresentationData#SIZE},
	 * {@link PresentationData#BOLD}, {@link PresentationData#ITALIC}, {@link PresentationData#COLOR},
	 * {@link PresentationData#ALIGN}, {@link PresentationData#CAPS}, {@link PresentationData#ANCHOR},
	 * {@link PresentationData#INSETS}, {@link PresentationData#CHARACTER_SPACING} and
	 * {@link PresentationData#LINE_SPACING}.
	 */
	public Map<String, Object> toData() {
		Map<String, Object> result = PresentationData.map();
		PresentationData.put(result, PresentationData.FONT, font);
		PresentationData.put(result, PresentationData.SIZE, size == null ? null : PresentationData.number(size));
		result.put(PresentationData.BOLD, Boolean.valueOf(bold));
		result.put(PresentationData.ITALIC, Boolean.valueOf(italic));
		PresentationData.put(result, PresentationData.COLOR, color == null ? null : color.toData());
		PresentationData.put(result, PresentationData.ALIGN, align);
		result.put(PresentationData.CAPS, Boolean.valueOf(caps));
		PresentationData.put(result, PresentationData.ANCHOR, anchor);
		if (insets != null) {
			Map<String, Object> margins = PresentationData.map();
			margins.put(PresentationData.LEFT, PresentationData.number(insets.left));
			margins.put(PresentationData.TOP, PresentationData.number(insets.top));
			margins.put(PresentationData.RIGHT, PresentationData.number(insets.right));
			margins.put(PresentationData.BOTTOM, PresentationData.number(insets.bottom));
			result.put(PresentationData.INSETS, margins);
		}
		PresentationData.put(result, PresentationData.CHARACTER_SPACING,
			characterSpacing == null ? null : PresentationData.number(characterSpacing));
		PresentationData.put(result, PresentationData.LINE_SPACING,
			lineSpacing == null ? null : PresentationData.fraction(lineSpacing));
		return result;
	}

}
