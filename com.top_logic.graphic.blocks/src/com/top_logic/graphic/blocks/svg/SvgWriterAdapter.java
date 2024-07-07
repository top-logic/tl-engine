/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.svg;

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

}
