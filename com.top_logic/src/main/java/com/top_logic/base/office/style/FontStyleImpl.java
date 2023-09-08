/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.style;

import java.awt.Color;

import com.top_logic.basic.config.Decision;

/**
 * {@link FontStyle} default implementation.
 * 
 * @see FontStyle#NONE
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class FontStyleImpl implements FontStyle {

	private boolean _shared = false;

	private String _fontName = null;

	private Double _fontSize = null;

	private Color _textColor = null;

	private Decision _bold = Decision.DEFAULT;

	private Decision _italic = Decision.DEFAULT;

	private Decision _strikeout = Decision.DEFAULT;

	private Underline _underline = Underline.DEFAULT;

	private TextOffset _textOffset = TextOffset.DEFAULT;

	/**
	 * Creates a {@link FontStyleImpl}.
	 * 
	 * <p>
	 * Use {@link FontStyle#NONE} as base style for creating new styles.
	 * </p>
	 */
	FontStyleImpl() {
		super();
	}

	@Override
	public String getFontName() {
		return _fontName;
	}

	@Override
	public FontStyle setFontName(String fontName) {
		return onWrite().applyFontName(fontName);
	}

	private FontStyle applyFontName(String fontName) {
		_fontName = fontName;
		return this;
	}

	@Override
	public Double getFontSize() {
		return _fontSize;
	}

	@Override
	public FontStyle setFontSize(Double fontSize) {
		return onWrite().applyFontSize(fontSize);
	}

	private FontStyle applyFontSize(Double fontSize) {
		_fontSize = fontSize;
		return this;
	}

	@Override
	public TextOffset getTextOffset() {
		return _textOffset;
	}

	@Override
	public FontStyle setTextOffset(TextOffset textOffset) {
		return onWrite().applyTextOffset(textOffset);
	}

	private FontStyle applyTextOffset(TextOffset textOffset) {
		_textOffset = textOffset;
		return this;
	}

	@Override
	public Color getTextColor() {
		return _textColor;
	}

	@Override
	public FontStyle setTextColor(Color textColor) {
		return onWrite().applyTextColor(textColor);
	}

	private FontStyle applyTextColor(Color color) {
		_textColor = color;
		return this;
	}

	@Override
	public Decision isBold() {
		return _bold;
	}

	@Override
	public FontStyle setBold() {
		return setBold(Decision.TRUE);
	}

	@Override
	public FontStyle setBold(Decision bold) {
		return onWrite().applyBold(bold);
	}

	private FontStyle applyBold(Decision bold) {
		_bold = bold;
		return this;
	}

	@Override
	public Decision isItalic() {
		return _italic;
	}

	@Override
	public FontStyle setItalic() {
		return setItalic(Decision.TRUE);
	}

	@Override
	public FontStyle setItalic(Decision italic) {
		return onWrite().applyItalic(italic);
	}

	private FontStyle applyItalic(Decision italic) {
		_italic = italic;
		return this;
	}

	@Override
	public Underline getUnderline() {
		return _underline;
	}

	@Override
	public FontStyle setUnderline(Underline underline) {
		return onWrite().applyUnderline(underline);
	}

	private FontStyle applyUnderline(Underline decision) {
		_underline = decision;
		return this;
	}

	@Override
	public Decision isStrikeout() {
		return _strikeout;
	}

	@Override
	public FontStyle setStrikeout() {
		return setStrikeout(Decision.TRUE);
	}

	@Override
	public FontStyle setStrikeout(Decision strikeout) {
		return onWrite().applyStrikeout(strikeout);
	}

	private FontStyle applyStrikeout(Decision decision) {
		_strikeout = decision;
		return this;
	}

	private FontStyleImpl onWrite() {
		if (_shared) {
			return copy();
		} else {
			return this;
		}
	}

	@Override
	public FontStyleImpl copy() {
		FontStyleImpl copy = new FontStyleImpl();
		copy.applyFontName(_fontName);
		copy.applyFontSize(_fontSize);
		copy.applyTextColor(_textColor);
		copy.applyBold(_bold);
		copy.applyItalic(_italic);
		copy.applyStrikeout(_strikeout);
		copy.applyUnderline(_underline);
		return copy;
	}

	@Override
	public FontStyle freeze() {
		_shared = true;
		return this;
	}

	@Override
	public boolean isShared() {
		return _shared;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_bold == null) ? 0 : _bold.hashCode());
		result = prime * result + ((_textColor == null) ? 0 : _textColor.hashCode());
		result = prime * result + ((_fontName == null) ? 0 : _fontName.hashCode());
		result = prime * result + ((_fontSize == null) ? 0 : _fontSize.hashCode());
		result = prime * result + ((_italic == null) ? 0 : _italic.hashCode());
		result = prime * result + ((_strikeout == null) ? 0 : _strikeout.hashCode());
		result = prime * result + ((_textOffset == null) ? 0 : _textOffset.hashCode());
		result = prime * result + ((_underline == null) ? 0 : _underline.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FontStyleImpl other = (FontStyleImpl) obj;
		if (_bold != other._bold)
			return false;
		if (_textColor == null) {
			if (other._textColor != null)
				return false;
		} else if (!_textColor.equals(other._textColor))
			return false;
		if (_fontName == null) {
			if (other._fontName != null)
				return false;
		} else if (!_fontName.equals(other._fontName))
			return false;
		if (_fontSize == null) {
			if (other._fontSize != null)
				return false;
		} else if (!_fontSize.equals(other._fontSize))
			return false;
		if (_italic != other._italic)
			return false;
		if (_strikeout != other._strikeout)
			return false;
		if (_textOffset != other._textOffset)
			return false;
		if (_underline != other._underline)
			return false;
		return true;
	}

	@Override
	public boolean isDefault() {
		return getTextColor() == null &&
			_fontName == null &&
			_fontSize == null &&
			_bold == Decision.DEFAULT &&
			_underline == Underline.DEFAULT &&
			_italic == Decision.DEFAULT &&
			_strikeout == Decision.DEFAULT &&
			_textOffset == TextOffset.DEFAULT;
	}
}
