/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;

/**
 * Represents a {@link XSLFTextParagraph}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXTextParagraph {

	private boolean bullet;

	private String bulletCharacter;

	private String bulletFont;

	private Color bulletFontColor;

	private Double bulletFontSize;

	private Double indent;

	private Double leftMargin;

	private int level;

	private Double lineSpacing;

	private Double spaceAfter;

	private Double spaceBefore;

	private TextAlign textAlign;

	private List<PptXTextRun> textRuns;

	/**
	 * Create a new PptXTextParagraph
	 */
	public PptXTextParagraph() {
		this.textRuns = new ArrayList<>();
	}

	/**
	 * Create a new PptXTextParagraph from a powerpoint text paragraph
	 * 
	 * @param aPara
	 *        the powerpoint text paragraph
	 */
	public PptXTextParagraph(XSLFTextParagraph aPara) {
		this();

		this.setBullet(aPara.isBullet());
		if (this.isBullet()) {
			this.setBulletCharacter(aPara.getBulletCharacter());
			this.setBulletFontColor(toColor(aPara.getBulletFontColor()));
			this.setBulletFont(aPara.getBulletFont());
			this.setBulletFontSize(aPara.getBulletFontSize());
		}
		this.setIndent(aPara.getIndent());
		this.setLeftMargin(aPara.getLeftMargin());
		this.setLevel(aPara.getIndentLevel());
		this.setLineSpacing(aPara.getLineSpacing());
		this.setSpaceAfter(aPara.getSpaceAfter());
		this.setSpaceBefore(aPara.getSpaceBefore());
		this.setTextAlign(aPara.getTextAlign());
		for (XSLFTextRun theRun : aPara.getTextRuns()) {
			this.addNewTextRun(theRun);
		}
	}

	static Color toColor(PaintStyle style) {
		if (style instanceof SolidPaint) {
			return ((SolidPaint) style).getSolidColor().getColor();
		}
		return null;
	}

	/**
	 * Copy style and maybe content to a powerpoint text paragraph
	 * 
	 * @param aParagr
	 *        the powerpoint text paragraph
	 * @param ensureRun
	 *        if true ensure that there is at least one text run in the powerpoint text paragraph
	 * @param ensureStructure
	 *        if true ensure that all text runs exist in the powerpoint text paragraph
	 * @param copyContents
	 *        if true copy contents into powerpoint text paragraph&apos;s text runs
	 */
	public void copy(XSLFTextParagraph aParagr, boolean ensureRun, boolean ensureStructure, boolean copyContents) {
		aParagr.setBullet(this.isBullet());
		if (bulletCharacter != null)
			aParagr.setBulletCharacter(this.getBulletCharacter());
		if (bulletFontColor != null)
			aParagr.setBulletFontColor(this.getBulletFontColor());
		if (bulletFont != null)
			aParagr.setBulletFont(this.getBulletFont());
		if (bulletFontSize != null)
			aParagr.setBulletFontSize(this.getBulletFontSize());
		if (indent != null)
			aParagr.setIndent(this.getIndent());
		if (leftMargin != null)
			aParagr.setLeftMargin(this.getLeftMargin());
		aParagr.setIndentLevel(this.getLevel());
		if (lineSpacing != null)
			aParagr.setLineSpacing(this.getLineSpacing());
		if (spaceAfter != null)
			aParagr.setSpaceAfter(this.getSpaceAfter());
		if (spaceBefore != null)
			aParagr.setSpaceBefore(this.getSpaceBefore());
		if (textAlign != null)
			aParagr.setTextAlign(this.getTextAlign());

		if (ensureRun || ensureStructure) {
			List<XSLFTextRun> textRunsPara = aParagr.getTextRuns();
			int theRunNoPara = textRunsPara.size();
			int theRunNo = 0;
			for (PptXTextRun theRun : this.getTextRuns()) {
				XSLFTextRun addNewTextRun =
					(theRunNo < theRunNoPara) ? textRunsPara.get(theRunNo) : aParagr.addNewTextRun();
				theRun.copy(addNewTextRun, copyContents);
				theRunNo++;
				if (!ensureStructure) {
					break;
				}
			}
		}
	}

	/**
	 * See {@link XSLFTextParagraph#addNewTextRun()}
	 * 
	 * @return the new text run
	 */
	public PptXTextRun addNewTextRun() {
		PptXTextRun theRun = new PptXTextRun();
		this.textRuns.add(theRun);
		return theRun;
	}

	/**
	 * See {@link XSLFTextParagraph#addNewTextRun()}
	 * 
	 * @return the new text run created as a copy of a given powerpoint text run
	 */
	public PptXTextRun addNewTextRun(XSLFTextRun aRun) {
		PptXTextRun theRun = new PptXTextRun(aRun);
		this.textRuns.add(theRun);
		return theRun;
	}

	/**
	 * See {@link XSLFTextParagraph#getTextRuns()}
	 * 
	 * @return the text runs
	 */
	public List<PptXTextRun> getTextRuns() {
		return new ArrayList<>(textRuns);
	}

	/**
	 * See {@link XSLFTextParagraph#isBullet()}
	 * 
	 * @return true if the paragraph has a bullet
	 */
	public boolean isBullet() {
		return (bullet);
	}

	/**
	 * See {@link XSLFTextParagraph#setBullet(boolean)}
	 * 
	 * @param bullet
	 *        if true set a bullet in front of the text
	 */
	public void setBullet(boolean bullet) {
		this.bullet = bullet;
	}

	/**
	 * See {@link XSLFTextParagraph#getBulletCharacter()} Note: only available if
	 * {@link #isBullet()} is true.
	 * 
	 * @return the bullet char
	 */
	public String getBulletCharacter() {
		return (bulletCharacter);
	}

	/**
	 * See {@link XSLFTextParagraph#setBulletCharacter(String)} Note: only available if
	 * {@link #isBullet()} is true.
	 * 
	 * @param bulletCharacter
	 *        the char
	 */
	public void setBulletCharacter(String bulletCharacter) {
		this.bulletCharacter = bulletCharacter;
	}

	/**
	 * See {@link XSLFTextParagraph#getBulletFont()} Note: only available if {@link #isBullet()} is
	 * true.
	 * 
	 * @return the bullet font name
	 */
	public String getBulletFont() {
		return (bulletFont);
	}

	/**
	 * See {@link XSLFTextParagraph#setBulletFont(String)} Note: only available if
	 * {@link #isBullet()} is true.
	 * 
	 * @param bulletFont
	 *        the font name
	 */
	public void setBulletFont(String bulletFont) {
		this.bulletFont = bulletFont;
	}

	/**
	 * See {@link XSLFTextParagraph#getBulletFontColor()} Note: only available if
	 * {@link #isBullet()} is true.
	 * 
	 * @return the bullet font color
	 */
	public Color getBulletFontColor() {
		return (bulletFontColor);
	}

	/**
	 * See {@link XSLFTextParagraph#setBulletFontColor(Color)} Note: only available if
	 * {@link #isBullet()} is true.
	 * 
	 * @param bulletColor
	 *        the font color
	 */
	public void setBulletFontColor(Color bulletColor) {
		this.bulletFontColor = bulletColor;
	}

	/**
	 * See {@link XSLFTextParagraph#getBulletFontSize()} Note: only available if {@link #isBullet()}
	 * is true.
	 * 
	 * @return the bullet font size
	 */
	public Double getBulletFontSize() {
		return bulletFontSize;
	}

	/**
	 * See {@link XSLFTextParagraph#setBulletFontSize(double)} Note: only available if
	 * {@link #isBullet()} is true.
	 * 
	 * @param bulletSize
	 *        the font size
	 */
	public void setBulletFontSize(double bulletSize) {
		this.bulletFontSize = bulletSize;
	}

	/**
	 * See {@link XSLFTextParagraph#getIndent()}
	 * 
	 * @return the indent
	 */
	public Double getIndent() {
		return indent;
	}

	/**
	 * See {@link XSLFTextParagraph#setIndent(Double)}
	 * 
	 * @param indent
	 *        the indent
	 */
	public void setIndent(Double indent) {
		this.indent = indent;
	}

	/**
	 * See {@link XSLFTextParagraph#getLeftMargin()}
	 * 
	 * @return the left margin
	 */
	public Double getLeftMargin() {
		return leftMargin;
	}

	/**
	 * See {@link XSLFTextParagraph#setLeftMargin(Double)}
	 * 
	 * @param leftMargin
	 *        the margin
	 */
	public void setLeftMargin(Double leftMargin) {
		this.leftMargin = leftMargin;
	}

	/**
	 * See {@link XSLFTextParagraph#getIndentLevel()}
	 * 
	 * @return the level
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * See {@link XSLFTextParagraph#setIndentLevel(int)}
	 * 
	 * @param level
	 *        the level
	 */
	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * See {@link XSLFTextParagraph#getLineSpacing()}
	 * 
	 * @return the line spacing
	 */
	public Double getLineSpacing() {
		return lineSpacing;
	}

	/**
	 * See {@link XSLFTextParagraph#setLineSpacing(Double)}
	 * 
	 * @param lineSpacing
	 *        the line spacing
	 */
	public void setLineSpacing(Double lineSpacing) {
		this.lineSpacing = lineSpacing;
	}

	/**
	 * See {@link XSLFTextParagraph#getSpaceAfter()}
	 * 
	 * @return the space after
	 */
	public Double getSpaceAfter() {
		return spaceAfter;
	}

	/**
	 * See {@link XSLFTextParagraph#setSpaceAfter(Double)}
	 * 
	 * @param spaceAfter
	 *        the space after
	 */
	public void setSpaceAfter(Double spaceAfter) {
		this.spaceAfter = spaceAfter;
	}

	/**
	 * See {@link XSLFTextParagraph#getSpaceBefore()}
	 * 
	 * @return the space before
	 */
	public Double getSpaceBefore() {
		return spaceBefore;
	}

	/**
	 * See {@link XSLFTextParagraph#setSpaceBefore(Double)}
	 * 
	 * @param spaceBefore
	 *        the space before
	 */
	public void setSpaceBefore(Double spaceBefore) {
		this.spaceBefore = spaceBefore;
	}

	/**
	 * See {@link XSLFTextParagraph#getTextAlign()}
	 * 
	 * @return the text align mode
	 */
	public TextAlign getTextAlign() {
		return (textAlign);
	}

	/**
	 * See {@link XSLFTextParagraph#setTextAlign(TextAlign)}
	 * 
	 * @param textAlign
	 *        the text align mode
	 */
	public void setTextAlign(TextAlign textAlign) {
		this.textAlign = textAlign;
	}
}
