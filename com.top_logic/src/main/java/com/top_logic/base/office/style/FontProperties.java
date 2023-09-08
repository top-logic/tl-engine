/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.style;

import java.awt.Color;

import org.apache.poi.ss.usermodel.Font;

import com.top_logic.basic.config.Decision;

/**
 * Properties that can be set on a {@link Font}.
 * 
 * @see FontStyle
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FontProperties {

	/**
	 * The font name of the cell or <code>null</code> if no individual font name is set. In this
	 * case the font name of the template is used.
	 */
	String getFontName();

	/**
	 * The font size of the cell or <code>null</code> if no individual font size is set. In this
	 * case the font size of the template is used.
	 */
	Double getFontSize();

	/**
	 * The {@link TextOffset} to use.
	 */
	TextOffset getTextOffset();

	/**
	 * The text color used for the object
	 * 
	 * @return May be <code>null</code>, if the default setting should be used.
	 */
	Color getTextColor();

	/**
	 * Whether the font is bold.
	 */
	Decision isBold();

	/**
	 * Whether the font is italic.
	 */
	Decision isItalic();

	/**
	 * Whether the font is strike out.
	 */
	Decision isStrikeout();

	/**
	 * The {@link Underline} style.
	 */
	Underline getUnderline();

}
