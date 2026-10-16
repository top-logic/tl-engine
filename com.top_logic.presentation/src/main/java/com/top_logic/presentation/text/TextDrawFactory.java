/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.text;

import java.awt.font.TextLayout;
import java.text.AttributedString;

import org.apache.poi.sl.draw.DrawFactory;
import org.apache.poi.sl.draw.DrawTextFragment;
import org.apache.poi.sl.draw.DrawTextParagraph;
import org.apache.poi.sl.usermodel.TextParagraph;

/**
 * {@link DrawFactory} laying out text as PowerPoint does in points where Apache POI deviates.
 *
 * <p>
 * Paragraphs are laid out by {@link SpacedTextParagraph}, which applies the character spacing of
 * the text runs. A line is as high as the ascent, descent and leading of its font, see
 * {@link FontLeadingFragment}. Text measurement ({@link TextMeasure}) and slide rendering both lay out text
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

	@Override
	public DrawTextFragment getTextFragment(TextLayout layout, AttributedString str) {
		return new FontLeadingFragment(layout, str);
	}

	/**
	 * {@link DrawTextFragment} whose leading is the leading of its font.
	 *
	 * <p>
	 * The height of a line at single line spacing is the sum of the ascent, the descent and the
	 * leading (line gap) of its font, as in PowerPoint and {@code LibreOffice}. POI replaces a leading of
	 * zero, which many fonts declare, by 15% of ascent plus descent, making each line of such a
	 * font 15% higher than in PowerPoint.
	 * </p>
	 */
	public static class FontLeadingFragment extends DrawTextFragment {

		/**
		 * Creates a {@link FontLeadingFragment}.
		 */
		public FontLeadingFragment(TextLayout layout, AttributedString str) {
			super(layout, str);
		}

		@Override
		public float getLeading() {
			return getLayout().getLeading();
		}

	}

}
