/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.text;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph;

/**
 * {@link DrawFactory} laying out text as PowerPoint does in points where Apache POI deviates.
 *
 * <p>
 * Paragraphs are laid out by {@link SpacedTextParagraph}, which applies the character spacing of
 * the text runs. Text measurement ({@link TextMeasure}) and slide rendering both lay out text
 * through this factory, so that measured and rendered line breaks agree.
 * </p>
 */
public class TextDrawFactory extends DrawFactory {

	/**
	 * Shared instance, the factory has no state.
	 */
	public static final TextDrawFactory INSTANCE = new TextDrawFactory();

	@Override
	public DrawTextParagraph getDrawable(TextParagraph<?, ?, ?> paragraph) {
		return new SpacedTextParagraph(paragraph);
	}

}
