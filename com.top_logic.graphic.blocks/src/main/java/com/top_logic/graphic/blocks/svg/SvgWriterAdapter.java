/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

import java.awt.Color;

import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;

/**
 * Adapter to an {@link SvgWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SvgWriterAdapter implements SvgWriter {

	private final SvgWriter _impl;

	/**
	 * Creates a {@link SvgWriterAdapter}.
	 *
	 */
	public SvgWriterAdapter(SvgWriter impl) {
		_impl = impl;
	}

	@Override
	public void beginSvg() {
		_impl.beginSvg();
	}

	@Override
	public void endSvg() {
		_impl.endSvg();
	}

	@Override
	public void beginGroup() {
		_impl.beginGroup();
	}

	@Override
	public void translate(double dx, double dy) {
		_impl.translate(dx, dy);
	}

	@Override
	public void endGroup() {
		_impl.endGroup();
	}

	@Override
	public void beginPath() {
		_impl.beginPath();
	}

	@Override
	public void beginData() {
		_impl.beginData();
	}

	@Override
	public void moveToRel(double dx, double dy) {
		_impl.moveToRel(dx, dy);
	}

	@Override
	public void moveToAbs(double dx, double dy) {
		_impl.moveToAbs(dx, dy);
	}

	@Override
	public void lineToRel(double dx, double dy) {
		_impl.lineToRel(dx, dy);
	}

	@Override
	public void lineToAbs(double dx, double dy) {
		_impl.lineToAbs(dx, dy);
	}

	@Override
	public void lineToHorizontalRel(double dx) {
		_impl.lineToHorizontalRel(dx);
	}

	@Override
	public void lineToHorizontalAbs(double dx) {
		_impl.lineToHorizontalAbs(dx);
	}

	@Override
	public void lineToVerticalRel(double dy) {
		_impl.lineToVerticalRel(dy);
	}

	@Override
	public void lineToVerticalAbs(double dy) {
		_impl.lineToVerticalAbs(dy);
	}

	@Override
	public void curveToRel(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		_impl.curveToRel(dx1, dy1, dx2, dy2, dx, dy);
	}

	@Override
	public void curveToAbs(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		_impl.curveToAbs(dx1, dy1, dx2, dy2, dx, dy);
	}

	@Override
	public void closePath() {
		_impl.closePath();
	}

	@Override
	public void endData() {
		_impl.endData();
	}

	@Override
	public void endPath() {
		_impl.endPath();
	}

	@Override
	public void close() {
		// The adapter is for temporary use, the underlying writer may be used after closing the
		// adapter.
		//
		// _impl.close();
	}

	@Override
	public void dimensions(String width, String height, double x1, double y1, double x2, double y2) {
		_impl.dimensions(width, height, x1, y1, x2, y2);
	}

	@Override
	public void rect(double x, double y, double w, double h) {
		_impl.rect(x, y, w, h);
	}

	@Override
	public void rect(double x, double y, double w, double h, double radius) {
		_impl.rect(x, y, w, h, radius);
	}

	@Override
	public void rect(double x, double y, double w, double h, double rx, double ry) {
		_impl.rect(x, y, w, h, rx, ry);
	}

	@Override
	public void image(double x, double y, double width, double height, String href, ImageAlign align,
			ImageScale scale) {
		_impl.image(x, y, width, height, href, align, scale);
	}

	@Override
	public void writeCssClass(String cssClass) {
		_impl.writeCssClass(cssClass);
	}

	@Override
	public void writeId(String id) {
		_impl.writeId(id);
	}

	@Override
	public void text(double x, double y, String text) {
		_impl.text(x, y, text);
	}

	@Override
	public void setFillOpacity(double value) {
		_impl.setFillOpacity(value);
	}

	@Override
	public void setStrokeOpacity(double value) {
		_impl.setStrokeOpacity(value);
	}

	@Override
	public void setStrokeWidth(double value) {
		_impl.setStrokeWidth(value);
	}

	@Override
	public void setFill(Color color) {
		_impl.setFill(color);
	}

	@Override
	public void setFill(String style) {
		_impl.setFill(style);
	}

	@Override
	public void setStroke(Color color) {
		_impl.setStroke(color);
	}

	@Override
	public void setStroke(String style) {
		_impl.setStroke(style);
	}

	@Override
	public void setStrokeDasharray(double... dashes) {
		_impl.setStrokeDasharray(dashes);
	}

}
