/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.server.svg;

import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.blocks.svg.SvgWriter;

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
	public void beginGroup() {
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
	public void beginPath() {
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
	public void rect(double x, double y, double w, double h) {
		beginBeginTag(RECT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		writeAttribute(WIDTH_ATTR, w);
		writeAttribute(HEIGHT_ATTR, h);
		endEmptyTag();
	}

	@Override
	public void rect(double x, double y, double w, double h, double rx, double ry) {
		beginBeginTag(RECT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		writeAttribute(WIDTH_ATTR, w);
		writeAttribute(HEIGHT_ATTR, h);
		writeAttribute(RX_ATTR, rx);
		writeAttribute(RY_ATTR, ry);
		endEmptyTag();
	}

	private void writeAttribute(String name, double value) {
		_out.writeAttribute(name, Double.toString(value));
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
	public void text(double x, double y, String text) {
		beginBeginTag(TEXT);
		writeAttribute(X_ATTR, x);
		writeAttribute(Y_ATTR, y);
		endBeginTag();
		_out.writeText(text);
		endTag(TEXT);
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
}
