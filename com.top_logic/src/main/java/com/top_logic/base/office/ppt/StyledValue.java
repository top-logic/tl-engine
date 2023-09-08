/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.io.File;

import com.top_logic.base.office.style.FontProperties;
import com.top_logic.base.office.style.FontStyle;
import com.top_logic.base.office.style.TextOffset;
import com.top_logic.base.office.style.Underline;
import com.top_logic.basic.config.Decision;

/**
 * Definition of an object that can have a background color and a value.
 * 
 * <p>
 * Used with {@link POIPowerpointXUtil} the values of the {@link StyledValue} might be e.g. a
 * {@link String}, {@link HyperlinkDefinition} or a (picture){@link File}.
 * </p>
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class StyledValue implements FontProperties {

	/**
	 * The value of the object.
	 */
	private Object _value;

	private FontStyle _fontStyle = FontStyle.NONE;

	/** The background color for the object */
	private Color _backgroundColor;

	/** See {@link #isTextWrap()}. */
	private Boolean _wrapText;

	/**
	 * Creates a new {@link StyledValue}.
	 */
	public StyledValue() {
	}

	/**
	 * All font-related style values.
	 */
	public FontStyle getFontStyle() {
		return _fontStyle;
	}

	/**
	 * @see #getFontStyle()
	 */
	public void setFontStyle(FontStyle fontStyle) {
		_fontStyle = fontStyle;
	}

	@Override
	public String getFontName() {
		return _fontStyle.getFontName();
	}

	/**
	 * @see #getFontName()
	 */
	public void setFontName(String fontName) {
		_fontStyle = _fontStyle.setFontName(fontName);
	}

	@Override
	public Double getFontSize() {
		return _fontStyle.getFontSize();
	}

	/**
	 * @see #getFontSize()
	 */
	public void setFontSize(Double fontSize) {
		_fontStyle = _fontStyle.setFontSize(fontSize);
	}

	@Override
	public TextOffset getTextOffset() {
		return _fontStyle.getTextOffset();
	}

	/**
	 * @see #getTextOffset()
	 */
	public void setTextOffset(TextOffset typeOffset) {
		_fontStyle = _fontStyle.setTextOffset(typeOffset);
	}

	/**
	 * Setter for {@link StyledValue#_backgroundColor}
	 */
	public void setBackgroundColor(Color color) {
		_backgroundColor = color;
	}
	
	/**
	 * Getter for {@link StyledValue#_backgroundColor}
	 */
	public Color getBackgroundColor(){
		return _backgroundColor;
	}
	
	/**
	 * Setter for {@link StyledValue#_value}
	 */
	public void setValue(Object value) {
		_value = value;
	}

	/**
	 * Getter for {@link StyledValue#_value}
	 */
	public Object getValue() {
		return _value;
	}

	@Override
	public Color getTextColor() {
		return _fontStyle.getTextColor();
	}

	/**
	 * Setter for {@link #getTextColor()}
	 */
	public void setTextColor(Color textColor) {
		_fontStyle = _fontStyle.setTextColor(textColor);
	}

	@Override
	public Decision isBold() {
		return _fontStyle.isBold();
	}

	/**
	 * @see #isBold()
	 */
	public void setBold() {
		_fontStyle = _fontStyle.setBold();
	}

	/**
	 * @see #isBold()
	 */
	public void setBold(Decision bold) {
		_fontStyle = _fontStyle.setBold(bold);
	}

	@Override
	public Decision isItalic() {
		return _fontStyle.isItalic();
	}

	/**
	 * @see #isItalic()
	 */
	public void setItalic() {
		_fontStyle = _fontStyle.setItalic();
	}

	/**
	 * @see #isItalic()
	 */
	public void setItalic(Decision italic) {
		_fontStyle = _fontStyle.setItalic(italic);
	}

	@Override
	public Underline getUnderline() {
		return _fontStyle.getUnderline();
	}

	/**
	 * @see #getUnderline()
	 */
	public void setUnderline(Underline underline) {
		_fontStyle = _fontStyle.setUnderline(underline);
	}

	@Override
	public Decision isStrikeout() {
		return _fontStyle.isStrikeout();
	}

	/**
	 * @see #isStrikeout()
	 */
	public void setStrikeout() {
		_fontStyle = _fontStyle.setStrikeout();
	}

	/**
	 * @see #isStrikeout()
	 */
	public void setStrikeout(Decision strikeout) {
		_fontStyle = _fontStyle.setStrikeout(strikeout);
	}

	/**
	 * Returns true or false depending if this {@link StyledValue} has activated or deactivated the
	 * text wrapping.
	 */
	public Boolean isTextWrap() {
		return _wrapText;
	}

	/**
	 * @see #isTextWrap()
	 */
	public void setTextWrap(Boolean wrap) {
		_wrapText = wrap;
	}

	/**
	 * Clears all style information so that the template style is used.
	 */
	public void clearStyle() {
		_fontStyle = FontStyle.NONE;
		_backgroundColor = null;
		_wrapText = null;
	}

	/**
	 * Whether this excel cell has an individual font style.
	 * 
	 * @see FontStyle#isDefault()
	 */
	public boolean hasIndividualFontStyle() {
		return !_fontStyle.isDefault();
	}

	/**
	 * Whether some non-default style has been set.
	 */
	public boolean hasIndividualStyle() {
		return getBackgroundColor() != null || isTextWrap() != null || hasIndividualFontStyle();
	}

}
