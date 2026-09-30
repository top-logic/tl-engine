/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.percentage;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.io.IOException;
import java.text.NumberFormat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;

import com.top_logic.base.chart.component.JFreeChartComponent;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.tag.AbstractTag;

/**
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class PercentageTag extends AbstractTag {

	public static final Paint DEFAULT_FOREGROUND = new GradientPaint(0, 0, new Color(170, 120, 80), 0, 0, Color.WHITE);
	public static final Paint DEFAULT_BACKGROUND = new GradientPaint(0, 0, Color.LIGHT_GRAY, 0, 0, Color.WHITE);

	private double percentage;
	private int shapeWidth = initShapeWith();
	private int shapeHeight = initShapeHeight();
	private int shapeNumber = initShapeNumber();
	private int spaceWidth = initSpaceWidth();
	private Paint shapeForegroundPaint = initShapeForegroundPaint();
	private Paint shapeBackgroundPaint = initShapeBackgroundPaint();
	private Paint spacePaint = initSpacePaint();
	private boolean visible = initVisible();

	@Override
	protected int endElement() throws IOException, JspException {
		if (!isVisible()) {
			return SKIP_BODY;
		}

		double percantage = getPercentageValue();
		String percentAsString = NumberFormat.getPercentInstance().format(percantage);
		String imagePath = new PercentageImageCreator().createImageReturnPath(percantage, new RectanglePercentageInfo(shapeWidth, shapeHeight, shapeNumber, spaceWidth, shapeForegroundPaint, shapeBackgroundPaint, spacePaint));

		TagWriter out = out();
		out.beginBeginTag(TABLE);
		out.endBeginTag();
		out.beginTag(TR);
		out.beginBeginTag(TD);
		out.writeAttribute(CLASS_ATTR, "tblRight");
		writeStyle(out);
		out.endBeginTag();
		out.writeContent(percentAsString);
		out.endTag(TD);
		out.beginTag(TD);
		out.beginBeginTag(IMG);
		out.writeAttribute(SRC_ATTR, (((HttpServletRequest) pageContext.getRequest()).getContextPath() + "/" + imagePath));
		out.endEmptyTag();
		out.endTag(TD);
		out.endTag(TR);
		out.endTag(TABLE);

		return SKIP_BODY;
	}

	private void writeStyle(TagWriter out) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		out.append("width: 35px;");
		out.endAttribute();
	}

	/**
	 * This method returns the percentage value. This method is a hook for sub
	 * classes.
	 */
	protected double getPercentageValue() {
		return this.percentage;
	}

	private Paint initSpacePaint() {
		return JFreeChartComponent.getThemeBackgroundColor();
	}

	private Paint initShapeBackgroundPaint() {
		return DEFAULT_BACKGROUND;
	}

	private Paint initShapeForegroundPaint() {
		return DEFAULT_FOREGROUND;
	}

	private int initSpaceWidth() {
		return 1;
	}

	private int initShapeNumber() {
		return 10;
	}

	private int initShapeHeight() {
		return 13;
	}

	private int initShapeWith() {
		return 10;
	}

	private boolean initVisible() {
		return true;
	}

	@Override
	protected int startElement() throws JspException, IOException {
		return EVAL_PAGE;
	}

	@Override
	protected void teardown() {
		super.teardown();

		percentage = 0;
		shapeWidth = initShapeWith();
		shapeHeight = initShapeHeight();
		shapeNumber = initShapeNumber();
		spaceWidth = initSpaceWidth();
		shapeForegroundPaint = initShapeForegroundPaint();
		shapeBackgroundPaint = initShapeBackgroundPaint();
		spacePaint = initSpacePaint();
		visible = initVisible();
	}

	/**
	 * @param percentage The percentage to set.
	 */
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

	/**
	 * @param shapeWidth The shapeWidth to set.
	 */
	public void setShapeWidth(int shapeWidth) {
		this.shapeWidth = shapeWidth;
	}

	/**
	 * @param shapeHeight The shapeHeight to set.
	 */
	public void setShapeHeight(int shapeHeight) {
		this.shapeHeight = shapeHeight;
	}

	/**
	 * @param shapeNumber The shapeNumber to set.
	 */
	public void setShapeNumber(int shapeNumber) {
		this.shapeNumber = shapeNumber;
	}

	/**
	 * @param spaceWidth The spaceWidth to set.
	 */
	public void setSpaceWidth(int spaceWidth) {
		this.spaceWidth = spaceWidth;
	}

	/**
	 * @param shapeForegroundPaint The shapeForegroundPaint to set.
	 */
	public void setShapeForegroundPaint(Paint shapeForegroundPaint) {
		if (shapeForegroundPaint instanceof Color) {
			this.shapeForegroundPaint = new GradientPaint(0, 0, (Color) shapeForegroundPaint, 0, 0, Color.WHITE);
		} else {
			this.shapeForegroundPaint = shapeForegroundPaint;
		}
	}

	/**
	 * @param shapeBackgroundPaint The shapeBackgroundPaint to set.
	 */
	public void setShapeBackgroundPaint(Paint shapeBackgroundPaint) {
		if (shapeBackgroundPaint instanceof Color) {
			this.shapeBackgroundPaint = new GradientPaint(0, 0, (Color) shapeBackgroundPaint, 0, 0, Color.WHITE);
		} else {
			this.shapeBackgroundPaint = shapeBackgroundPaint;
		}
	}

	/**
	 * @param spacePaint The spacePaint to set.
	 */
	public void setSpacePaint(Paint spacePaint) {
		this.spacePaint = spacePaint;
	}

	/**
	 * This method returns if the tag is visible.
	 */
	public boolean isVisible() {
		return this.visible;
	}

	/** See {@link #isVisible()}. */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}



}
