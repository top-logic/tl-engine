/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.ppt;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.TextShape.TextAutofit;
import org.apache.poi.sl.usermodel.TextShape.TextDirection;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XSLFSimpleShape;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

/**
 * Represents a {@link XSLFTextShape}
 * 
 * @author <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class PptXTextShape extends PptXSimpleShape {
	// fields defined by POI
	private Double bottomInset;

	private Double leftInset;

	private Double rightInset;

	private Double topInset;

	private String text;

	private TextAutofit textAutofit;

	private TextDirection textDirection;

	private VerticalAlignment verticalAlignment;

	private Boolean wordWrap;

	private Placeholder placeholder;

	private final List<PptXTextParagraph> textParagraphs = new ArrayList<>();

	// own fields
	private Object data; // e.g. a file with a picture

	/**
	 * See {@link XSLFTextShape#addNewTextParagraph()}
	 * 
	 * @return the created paragraph
	 */
	public PptXTextParagraph addNewTextParagraph() {
		PptXTextParagraph thePara = new PptXTextParagraph();
		this.textParagraphs.add(thePara);
		return thePara;
	}

	/**
	 * See {@link XSLFTextShape#addNewTextParagraph()} by copying an existing one
	 * 
	 * @param aPara
	 *        the powerpoint paragraph
	 * @return the created paragraph
	 */
	public PptXTextParagraph addNewTextParagraph(XSLFTextParagraph aPara) {
		PptXTextParagraph thePara = new PptXTextParagraph(aPara);
		this.textParagraphs.add(thePara);
		return thePara;
	}

	/**
	 * See {@link XSLFTextShape#getTextParagraphs()}
	 * 
	 * @return the current text paragraphs
	 */
	public List<PptXTextParagraph> getTextParagraphs() {
		return new ArrayList<>(this.textParagraphs);
	}

	/**
	 * Set the data to be placed in a real cell when replacing tokens
	 * 
	 * @param aData
	 *        the data, i.e. a text or an image file
	 */
	public void setData(Object aData) {
		this.data = aData;
	}

	/**
	 * Get the data to be placed in a real cell when replacing tokens
	 * 
	 * @return the data, i.e. a text or an image file
	 */
	public Object getData() {
		return this.data;
	}

	/**
	 * See {@link XSLFTextShape#setPlaceholder(Placeholder)}
	 * 
	 * @param placeholder
	 *        the place holder
	 */
	public void setPlaceholder(Placeholder placeholder) {
		this.placeholder = placeholder;
	}

	/**
	 * See {@link XSLFTextShape#setBottomInset(double)}
	 * 
	 * @param bottomInset
	 *        the margin
	 */
	public void setBottomInset(double bottomInset) {
		this.bottomInset = bottomInset;
	}

	/**
	 * See {@link XSLFTextShape#setLeftInset(double)}
	 * 
	 * @param leftInset
	 *        the margin
	 */
	public void setLeftInset(double leftInset) {
		this.leftInset = leftInset;
	}

	/**
	 * See {@link XSLFTextShape#setRightInset(double)}
	 * 
	 * @param rightInset
	 *        the margin
	 */
	public void setRightInset(double rightInset) {
		this.rightInset = rightInset;
	}

	/**
	 * See {@link XSLFTextShape#setTopInset(double)}
	 * 
	 * @param topInset
	 *        the margin
	 */
	public void setTopInset(double topInset) {
		this.topInset = topInset;
	}

	/**
	 * See {@link XSLFTextShape#setText(String)}
	 * 
	 * @param text
	 *        the plain text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * See {@link XSLFTextShape#setTextAutofit(TextAutofit)}.
	 * 
	 * @param textAutofit
	 *        the text auto fit
	 */
	public void setTextAutofit(TextAutofit textAutofit) {
		this.textAutofit = textAutofit;
	}

	/**
	 * See {@link XSLFTextShape#setTextDirection(TextDirection)}
	 * 
	 * @param textDirection
	 *        the text direction
	 */
	public void setTextDirection(TextDirection textDirection) {
		this.textDirection = textDirection;
	}

	/**
	 * See {@link XSLFTextShape#setVerticalAlignment(VerticalAlignment)}
	 * 
	 * @param verticalAlignment
	 *        the vertical alignment
	 */
	public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	/**
	 * See {@link XSLFTextShape#setWordWrap(boolean)}
	 * 
	 * @param wordWrap
	 *        wrap words if true
	 */
	public void setWordWrap(boolean wordWrap) {
		this.wordWrap = wordWrap;
	}

	/**
	 * See {@link XSLFTextShape#getBottomInset()}
	 * 
	 * @return the margin
	 */
	public double getBottomInset() {
		return bottomInset == null ? 3.6d : bottomInset;
	}

	/**
	 * See {@link XSLFTextShape#getLeftInset()}
	 * 
	 * @return the margin
	 */
	public double getLeftInset() {
		return leftInset == null ? 7.2d : leftInset;
	}

	/**
	 * See {@link XSLFTextShape#getRightInset()}
	 * 
	 * @return the margin
	 */
	public double getRightInset() {
		return rightInset == null ? 7.2d : rightInset;
	}

	/**
	 * See {@link XSLFTextShape#getTopInset()}
	 * 
	 * @return the margin
	 */
	public double getTopInset() {
		return topInset == null ? 3.6d : topInset;
	}
	
	/**
	 * See {@link XSLFTextShape#getText()}
	 * 
	 * @return the plain text
	 */
	public String getText() {
		return text;
	}

	/**
	 * See {@link XSLFTextShape#getTextAutofit()}
	 * 
	 * @return the auto fit
	 */
	public TextAutofit getTextAutofit() {
		return textAutofit;
	}

	/**
	 * See {@link XSLFTextShape#getTextDirection()}
	 * 
	 * @return the text direction
	 */
	public TextDirection getTextDirection() {
		return textDirection;
	}

	/**
	 * See {@link XSLFTextShape#getTextType()}
	 * 
	 * @return the place holder
	 */
	public Placeholder getTextType() {
		return placeholder;
	}

	/**
	 * See {@link XSLFTextShape#getVerticalAlignment()}
	 * 
	 * @return the vertical alignment
	 */
	public VerticalAlignment getVerticalAlignment() {
		return verticalAlignment;
	}

	/**
	 * See {@link XSLFTextShape#getBottomInset()}
	 * 
	 * @return the margin
	 */
	public boolean getWordWrap() {
		return wordWrap == null ? true : wordWrap;
	}
	
	/**
	 * Create a new PptXTextShape from a powerpoint shape
	 * 
	 * @param aShape
	 *        the powerpoint shape
	 * @param copyContents
	 *        if true copy contents
	 */
	public PptXTextShape(XSLFTextShape aShape, boolean copyContents) {
		super(aShape);

		this.setBottomInset(aShape.getBottomInset());
		this.setLeftInset(aShape.getLeftInset());
		this.setRightInset(aShape.getRightInset());
		this.setTopInset(aShape.getTopInset());

		this.setPlaceholder(aShape.getTextType());
		this.setText(aShape.getText());
		this.setTextAutofit(aShape.getTextAutofit());
		this.setTextDirection(aShape.getTextDirection());
		this.setVerticalAlignment(aShape.getVerticalAlignment());
		this.setWordWrap(aShape.getWordWrap());

		if (copyContents) {
			for (XSLFTextParagraph thePara : aShape.getTextParagraphs()) {
				this.addNewTextParagraph(thePara);
			}
		}
	}

	/**
	 * Create a new PptXTextShape with a shape name and text value
	 * 
	 * @param aName
	 *        the shape name
	 * @param aValue
	 *        the plain text value
	 */
	public PptXTextShape(String aName, String aValue) {
		this(aName);

		this.setText(aValue);
	}

	/**
	 * Create a new PptXTextShape with a shape name
	 * 
	 * @param aName
	 *        the shape name
	 */
	public PptXTextShape(String aName) {
		super(aName);
	}

	@Override
	public void copyStyle(XSLFSimpleShape aShape) {
		super.copyStyle(aShape);

		if (aShape instanceof XSLFTextShape) {
			// TODO inset handling is buggy in POI. Seems to deliver a standard value for predefined
			// cells
			if (bottomInset != null)
				((XSLFTextShape) aShape).setBottomInset(this.getBottomInset());
			if (leftInset != null)
				((XSLFTextShape) aShape).setLeftInset(this.getLeftInset());
			if (rightInset != null)
				((XSLFTextShape) aShape).setRightInset(this.getRightInset());
			if (topInset != null)
				((XSLFTextShape) aShape).setTopInset(this.getTopInset());

			if (placeholder != null)
				((XSLFTextShape) aShape).setPlaceholder(this.getTextType());
			if (textAutofit != null)
				((XSLFTextShape) aShape).setTextAutofit(this.getTextAutofit());
			if (textDirection != null)
				((XSLFTextShape) aShape).setTextDirection(this.getTextDirection());
			if (verticalAlignment != null)
				((XSLFTextShape) aShape).setVerticalAlignment(this.getVerticalAlignment());
			if (wordWrap != null)
				((XSLFTextShape) aShape).setWordWrap(this.getWordWrap());
		}
	}

	/**
	 * Copy the style and contents from a powerpoint shape
	 * 
	 * @param aShape
	 *        the powerpoint shape
	 * @param ensureParagraphAndRun
	 *        if true ensure that there is a paragraph and a text run and the styles are copied
	 * @param ensureStructure
	 *        if true copy the whole paragraph and text run structure
	 * @param copyContents
	 *        if true copy the text contents into the text runs as well
	 */
	public void copy(XSLFTextShape aShape, boolean ensureParagraphAndRun, boolean ensureStructure,
			boolean copyContents) {

		// Note: Before applying styles to the given shape (see below), first, at least one text
		// paragraph must be created. Otherwise the internal POI data structure goes out of synch
		// with the underlying XML structure. The resulting XML then has two paragraphs, while there
		// is only one paragraph in the POI representation.
		if (ensureParagraphAndRun || ensureStructure) {
			List<XSLFTextParagraph> textParas = aShape.getTextParagraphs();
			int theParaNoShape = textParas.size();
			int theParaNo = 0;
			for (PptXTextParagraph thePara : this.getTextParagraphs()) {
				XSLFTextParagraph addNewTextPara =
					(theParaNo < theParaNoShape) ? textParas.get(theParaNo) : aShape.addNewTextParagraph();
				thePara.copy(addNewTextPara, ensureParagraphAndRun, ensureStructure, copyContents);
				theParaNo++;
				if (!ensureStructure) {
					break;
				}
			}
		}

		this.copyStyle(aShape);
	}

}
