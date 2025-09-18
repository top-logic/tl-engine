/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.server.svg;

import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.blocks.svg.SVGColor;
import com.top_logic.graphic.blocks.svg.SvgUtil;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;

/**
 * {@link SvgWriter} creating XML output through a {@link TagWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SvgTagWriter implements SvgWriter {

	private final TagWriter _out;

	private boolean _tagOpen;

	private boolean _inData;

	private boolean _firstData;

	private String _textContent;

	/**
	 * Creates a {@link SvgTagWriter}.
	 *
	 */
	public SvgTagWriter(TagWriter out) {
		_out = out;
	}

	@Override
	public void beginSvg() {
		beginBeginTag(SVG);
		_out.writeAttribute("xmlns", SVG_NS);
		_out.writeAttribute(VERSION_ATTR, "1.1");
	}

	@Override
	public void dimensions(String width, String height, double x1, double y1, double x2, double y2) {
		_out.writeAttribute(WIDTH_ATTR, width);
		_out.writeAttribute(HEIGHT_ATTR, height);
		_out.writeAttribute(VIEW_BOX_ATTR, x1 + " " + y1 + " " + x2 + " " + y2);
	}

	@Override
	public void endSvg() {
		endTag(SVG);
	}

	@Override
	public void beginGroup(Object model) {
		beginBeginTag(G);
	}

	@Override
	public void translate(double dx, double dy) {
		_out.beginAttribute(TRANSFORM_ATTR);
		append(TRANSLATE_OP);
		append('(');
		append(dx);
		append(',');
		append(dy);
		append(')');
		_out.endAttribute();
	}

	@Override
	public void writeCssClass(String cssClass) {
		_out.writeAttribute(CLASS_ATTR, cssClass);
	}

	@Override
	public void writeId(String id) {
		_out.writeAttribute(ID_ATTR, id);
	}

	@Override
	public void endGroup() {
		endTag(G);
	}

	@Override
	public void beginPath(Object model) {
		beginBeginTag(PATH);
	}

	@Override
	public void beginData() {
		_out.beginAttribute(D_ATTR);
		_inData = true;
		_firstData = true;
	}

	@Override
	public void moveToRel(double dx, double dy) {
		beforeDataEntry();
		append('m');
		append(' ');
		append(dx);
		append(',');
		append(dy);
	}

	@Override
	public void moveToAbs(double x, double y) {
		beforeDataEntry();
		append('M');
		append(' ');
		append(x);
		append(',');
		append(y);
	}

	@Override
	public void lineToRel(double dx, double dy) {
		beforeDataEntry();
		append('l');
		append(' ');
		append(dx);
		append(',');
		append(dy);
	}

	@Override
	public void lineToAbs(double x, double y) {
		beforeDataEntry();
		append('L');
		append(' ');
		append(x);
		append(',');
		append(y);
	}

	@Override
	public void lineToHorizontalRel(double dx) {
		beforeDataEntry();
		append('h');
		append(' ');
		append(dx);
	}

	@Override
	public void lineToHorizontalAbs(double x) {
		beforeDataEntry();
		append('H');
		append(' ');
		append(x);
	}

	@Override
	public void lineToVerticalRel(double dy) {
		beforeDataEntry();
		append('v');
		append(' ');
		append(dy);
	}

	@Override
	public void lineToVerticalAbs(double y) {
		beforeDataEntry();
		append('V');
		append(' ');
		append(y);
	}

	@Override
	public void curveToRel(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		beforeDataEntry();
		append('c');
		append(' ');
		append(dx1);
		append(',');
		append(dy1);
		append(' ');
		append(dx2);
		append(',');
		append(dy2);
		append(' ');
		append(dx);
		append(',');
		append(dy);
	}

	@Override
	public void curveToAbs(double x1, double y1, double x2, double y2, double x, double y) {
		beforeDataEntry();
		append('C');
		append(' ');
		append(x1);
		append(',');
		append(y1);
		append(' ');
		append(x2);
		append(',');
		append(y2);
		append(' ');
		append(x);
		append(',');
		append(y);
	}

	@Override
	public void closePath() {
		beforeDataEntry();
		append('z');
	}

	@Override
	public void beginRect(double x, double y, double w, double h) {
		beginBeginTag(RECT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		writeAttribute(WIDTH_ATTR, w);
		writeAttribute(HEIGHT_ATTR, h);
	}

	@Override
	public void beginRect(double x, double y, double w, double h, double rx, double ry) {
		beginRect(x, y, w, h);
		writeAttribute(RX_ATTR, rx);
		writeAttribute(RY_ATTR, ry);
	}

	@Override
	public void endRect() {
		endEmptyTag();
	}

	private void writeAttribute(String name, double value) {
		writeAttribute(name, Double.toString(value));
	}

	private void writeAttribute(String name, String value) {
		_out.writeAttribute(name, value);
	}

	@Override
	public void endData() {
		_inData = false;
		_out.endAttribute();
	}

	@Override
	public void endPath() {
		endTag(PATH);
	}

	@Override
	public void beginText(double x, double y, String text) {
		beginBeginTag(TEXT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		_textContent = text;
	}
	
	@Override
	public void setTextStyle(String fontFamily, String fontSize, String fontWeight) {
		writeAttribute(FONT_FAMILY_ATTR, fontFamily);
		writeAttribute(FONT_SIZE_ATTR, fontSize);
		writeAttribute(FONT_WEIGHT_ATTR, fontWeight);
	}

	@Override
	public void endText() {
		endBeginTag();
		_out.writeText(_textContent);
		endTag(TEXT);

		_textContent = null;
	}

	@Override
	public void image(double x, double y, double width, double height, String href, ImageAlign align,
			ImageScale scale) {
		beginBeginTag(IMAGE);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		writeAttribute(WIDTH_ATTR, width);
		writeAttribute(HEIGHT_ATTR, height);
		writeAttribute(HREF_ATTR, href);
		writeAttribute(PRESERVE_ASPECT_RATIO_ATTR, align + " " + scale);
		endEmptyTag();
	}

	private void beginBeginTag(String tagName) {
		endBeginTag();
		_out.beginBeginTag(tagName);
		_tagOpen = true;
	}

	private void endBeginTag() {
		if (_tagOpen) {
			_out.endBeginTag();
			_tagOpen = false;
		}
	}

	private void endEmptyTag() {
		if (_tagOpen) {
			_out.endEmptyTag();
			_tagOpen = false;
		}
	}

	private void endTag(String tagName) {
		endBeginTag();
		_out.endTag(tagName);
	}

	private void beforeDataEntry() {
		if (!_inData) {
			throw new IllegalStateException("Dot within path data");
		}
		if (_firstData) {
			_firstData = false;
		} else {
			append(' ');
		}
	}

	private void append(String s) {
		try {
			_out.append(s);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private void append(char c) {
		try {
			_out.append(c);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private void append(double value) {
		append(Double.toString(value));
	}

	@Override
	public void close() {
		// Ignore.
	}

	@Override
	public void setFillOpacity(double value) {
		_out.writeAttribute(FILL_OPACITY_ATTR, Double.toString(value));
	}

	@Override
	public void setStrokeOpacity(double value) {
		_out.writeAttribute(STROKE_OPACITY_ATTR, Double.toString(value));
	}

	@Override
	public void setStrokeWidth(double value) {
		_out.writeAttribute(STROKE_WIDTH_ATTR, Double.toString(value));
	}

	@Override
	public void setFill(SVGColor color) {
		setFill(SvgUtil.html(color));
	}

	@Override
	public void setFill(String style) {
		_out.writeAttribute(FILL_ATTR, style);
	}

	@Override
	public void setStroke(SVGColor color) {
		setStroke(SvgUtil.html(color));
	}

	@Override
	public void setStroke(String style) {
		_out.writeAttribute(STROKE_ATTR, style);
	}

	@Override
	public void setStrokeDasharray(double... dashes) {
		_out.writeAttribute(STROKE_DASHARRAY_ATTR, SvgUtil.valueList(dashes));
	}
}
