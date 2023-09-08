/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.util.List;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

/**
 * Representation of a {@link XSLFTextRun}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXTextRun {

	private Color fontColor;

	private Boolean bold;

	private Boolean italic;

	private Boolean strikethrough;

	private Boolean underline;

	private String text;

	private String fontFamily;

	private Double fontSize;

	/**
	 * See {@link XSLFTextRun#getFontColor()}
	 * 
	 * @return the font color
	 */
	public Color getFontColor() {
		return (fontColor);
	}

	/**
	 * See {@link XSLFTextRun#setFontColor(Color)}
	 * 
	 * @param fontColor
	 *        the font color
	 */
	public void setFontColor(Color fontColor) {
		this.fontColor = fontColor;
	}

	/**
	 * See {@link XSLFTextRun#isBold()}
	 * 
	 * @return true if the font is bold
	 */
	public boolean isBold() {
		return bold == null ? false : bold;
	}

	/**
	 * See {@link XSLFTextRun#setBold(boolean)}
	 * 
	 * @param bold
	 *        if true use bold face for font
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
	}

	/**
	 * See {@link XSLFTextRun#isItalic()}
	 * 
	 * @return true if the font is italic
	 */
	public boolean isItalic() {
		return italic == null ? false : italic;
	}

	/**
	 * See {@link XSLFTextRun#setItalic(boolean)}
	 * 
	 * @param italic
	 *        if true use italic for font
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	/**
	 * See {@link XSLFTextRun#isStrikethrough()}
	 * 
	 * @return true if the font is strike through
	 */
	public boolean isStrikethrough() {
		return strikethrough == null ? false : strikethrough;
	}

	/**
	 * See {@link XSLFTextRun#setStrikethrough(boolean)}
	 * 
	 * @param strikethrough
	 *        if true strike through for font
	 */
	public void setStrikethrough(boolean strikethrough) {
		this.strikethrough = strikethrough;
	}

	/**
	 * See {@link XSLFTextRun#isUnderlined()}
	 * 
	 * @return true if the font is underline
	 */
	public boolean isUnderline() {
		return underline == null ? false : underline;
	}

	/**
	 * See {@link XSLFTextRun#setUnderlined(boolean)}
	 * 
	 * @param underline
	 *        if true use underline for font
	 */
	public void setUnderline(boolean underline) {
		this.underline = underline;
	}

	/**
	 * See {@link XSLFTextRun#getRawText()}
	 * 
	 * @return the plain text content
	 */
	public String getText() {
		return (text);
	}

	/**
	 * See {@link XSLFTextRun#setText(String)}
	 * 
	 * @param text
	 *        the plain text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * See {@link XSLFTextRun#getFontFamily()}
	 * 
	 * @return the font family name
	 */
	public String getFontFamily() {
		return (fontFamily);
	}

	/**
	 * See {@link XSLFTextRun#setFontFamily(String)}
	 * 
	 * @param fontFamily
	 *        the font family name
	 */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	/**
	 * See {@link XSLFTextRun#getFontSize()}
	 * 
	 * @return the font size
	 */
	public double getFontSize() {
		return fontSize == null ? -1.0d : fontSize;
	}

	/**
	 * See {@link XSLFTextRun#setFontSize(Double)}
	 * 
	 * @param fontSize
	 *        the font size
	 */
	public void setFontSize(double fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * Create a new PptXTextRun
	 */
	public PptXTextRun() {
		// No action
	}

	/**
	 * Create a new PptXTextRun from a powerpoint run
	 * 
	 * @param aRun
	 *        the run
	 */
	public PptXTextRun(XSLFTextRun aRun) {
		this.setupFromTextRun(aRun);
	}

	/**
	 * Extracts the first text run from the first paragraph of the given shape
	 * 
	 * @param aShape
	 *        the shape. Not <code>null</code>
	 */
	public PptXTextRun(XSLFTextShape aShape) {
		List<XSLFTextParagraph> textParagraphs = aShape.getTextParagraphs();
		if (!textParagraphs.isEmpty()) {
			List<XSLFTextRun> xslfTextRuns = textParagraphs.get(0).getTextRuns();
			if (!xslfTextRuns.isEmpty()) {
				this.setupFromTextRun(xslfTextRuns.get(0));
			}
		}
	}

	private void setupFromTextRun(XSLFTextRun xslfTextRun) {
		this.setBold(xslfTextRun.isBold());
		this.setFontColor(PptXTextParagraph.toColor(xslfTextRun.getFontColor()));
		this.setFontFamily(xslfTextRun.getFontFamily());
		this.setFontSize(xslfTextRun.getFontSize());
		this.setItalic(xslfTextRun.isItalic());
		this.setStrikethrough(xslfTextRun.isStrikethrough());
		this.setUnderline(xslfTextRun.isUnderlined());
		this.setText(xslfTextRun.getRawText());
	}

	/**
	 * Copy the style and content to a powerpoint run
	 * 
	 * @param aRun
	 *        the powerpoint run
	 * @param copyContents
	 *        if true copy the text conent as well
	 */
	public void copy(XSLFTextRun aRun, boolean copyContents) {
		if (bold != null)
			aRun.setBold(this.isBold());
		if (fontColor != null)
			aRun.setFontColor(this.getFontColor());
		if (fontFamily != null)
			aRun.setFontFamily(this.getFontFamily());
		if (fontSize != null)
			aRun.setFontSize(this.getFontSize());
		if (italic != null)
			aRun.setItalic(this.isItalic());
		if (strikethrough != null)
			aRun.setStrikethrough(this.isStrikethrough());
		if (underline != null)
			aRun.setUnderlined(this.isUnderline());

		if (copyContents) {
			aRun.setText(this.getText());
		} else {
			aRun.setText(" "); // TODO needed/wanted?
		}
	}
}
