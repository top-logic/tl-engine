/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.style;

import java.awt.Color;

import com.top_logic.basic.config.Decision;

/**
 * {@link FontProperties} value holder with optional {@link #isShared() copy-on-write semantics}.
 * 
 * @see FontStyleImpl The default implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FontStyle extends FontProperties {

	/**
	 * The {@link #isDefault() default} style.
	 */
	FontStyle NONE = new FontStyleImpl().freeze();

	/**
	 * Whether this {@link FontStyle} has no individual settings.
	 */
	boolean isDefault();

	/**
	 * Whether this instance cannot be modified.
	 * 
	 * <p>
	 * A shared instance creates a new {@link FontStyle} version upon write. All setters return
	 * either a reference to this, or a new instance, if the target of the setter is shared. When
	 * calling a setter method, the result must be taken as new style value.
	 * </p>
	 * 
	 * @see #freeze()
	 */
	boolean isShared();

	/**
	 * Creates an explicit (non-shared) copy of this style.
	 * 
	 * @see #isShared()
	 */
	FontStyle copy();

	/**
	 * Prevents further modification of this instance.
	 * 
	 * <p>
	 * After a call to this method, {@link #isShared()} returns <code>true</code>.
	 * </p>
	 * 
	 * @return A reference to this instance for call chaining.
	 */
	FontStyle freeze();

	/**
	 * @see #getFontName()
	 * @see #isShared()
	 */
	FontStyle setFontName(String fontName);

	/**
	 * @see #getFontSize()
	 * @see #isShared()
	 */
	FontStyle setFontSize(Double fontSize);

	/**
	 * @see #getTextColor()
	 * @see #isShared()
	 */
	FontStyle setTextColor(Color textColor);

	/**
	 * @see #isBold()
	 * @see #isShared()
	 */
	FontStyle setBold();

	/**
	 * @see #isBold()
	 * @see #isShared()
	 */
	FontStyle setBold(Decision bold);

	/**
	 * @see #isItalic()
	 * @see #isShared()
	 */
	FontStyle setItalic();

	/**
	 * @see #isItalic()
	 * @see #isShared()
	 */
	FontStyle setItalic(Decision italic);

	/**
	 * @see #getUnderline()
	 * @see #isShared()
	 */
	FontStyle setUnderline(Underline underline);

	/**
	 * @see #isStrikeout()
	 * @see #isShared()
	 */
	FontStyle setStrikeout();

	/**
	 * @see #isStrikeout()
	 * @see #isShared()
	 */
	FontStyle setStrikeout(Decision strikeout);

	/**
	 * @see #getTextOffset()
	 * @see #isShared()
	 */
	FontStyle setTextOffset(TextOffset textOffset);

}
