/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGLength;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTransform;
import org.vectomatic.dom.svg.OMSVGTransformList;
import org.vectomatic.dom.svg.itf.ISVGTransformable;

import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * {@link SvgWriter} directly creating a SVG DOM tree.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SVGBuilder implements SvgWriter {

	private OMSVGDocument _doc;
	private OMSVGSVGElement _root;

	private OMSVGElement _parent;

	private OMSVGElement _current;

	/**
	 * Creates a {@link SVGBuilder}.
	 * 
	 * @param doc
	 *        The {@link OMSVGDocument} to write to.
	 * @param root
	 *        The root {@link OMSVGSVGElement} to append to.
	 */
	public SVGBuilder(OMSVGDocument doc, OMSVGSVGElement root) {
		this(doc, root, root);
	}

	/**
	 * Creates a {@link SVGBuilder}.
	 *
	 * @param doc
	 *        The {@link OMSVGDocument} to write to.
	 * @param root
	 *        The root {@link OMSVGSVGElement} to write to.
	 * @param parent
	 *        The parent {@link OMSVGElement} to append to.
	 */
	public SVGBuilder(OMSVGDocument doc, OMSVGSVGElement root, OMSVGElement parent) {
		_doc = doc;
		_root = root;
		_parent = parent;
		_current = parent;
	}

	@Override
	public void beginSvg() {
		beginGroup();
	}

	@Override
	public void dimensions(String width, String height, double x1, double y1, double x2, double y2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void endSvg() {
		endGroup();
	}

	@Override
	public void beginGroup() {
		OMSVGGElement next = _doc.createSVGGElement();
		_parent.appendChild(next);
		setParent(next);
	}

	@Override
	public void translate(double dx, double dy) {
		OMSVGTransform transform = _root.createSVGTransform();
		transform.setTranslate((float) dx, (float) dy);
		OMSVGTransformList list = ((ISVGTransformable) _current).getTransform().getBaseVal();
		list.clear();
		list.appendItem(transform);
	}

	@Override
	public void endGroup() {
		setParent((OMSVGElement) _parent.getParentNode());
	}

	@Override
	public void beginPath() {
		appendChild(_doc.createSVGPathElement());
	}

	@Override
	public void beginData() {
		// Ignore.
	}

	@Override
	public void moveToRel(double dx, double dy) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegMovetoRel((float) dx, (float) dy));
	}

	@Override
	public void moveToAbs(double x, double y) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegMovetoAbs((float) x, (float) y));
	}

	@Override
	public void lineToRel(double dx, double dy) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegLinetoRel((float) dx, (float) dy));
	}

	@Override
	public void lineToAbs(double x, double y) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegLinetoAbs((float) x, (float) y));
	}

	@Override
	public void lineToHorizontalRel(double dx) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegLinetoHorizontalRel((float) dx));
	}

	@Override
	public void lineToHorizontalAbs(double x) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegLinetoHorizontalAbs((float) x));
	}

	@Override
	public void lineToVerticalRel(double dy) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegLinetoVerticalRel((float) dy));
	}

	@Override
	public void lineToVerticalAbs(double y) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegLinetoVerticalAbs((float) y));
	}

	@Override
	public void curveToRel(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegCurvetoCubicRel((float) dx, (float) dy, (float) dx1,
			(float) dy1, (float) dx2, (float) dy2));
	}

	@Override
	public void curveToAbs(double x1, double y1, double x2, double y2, double x, double y) {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegCurvetoCubicAbs((float) x, (float) y, (float) x1,
			(float) y1, (float) x2, (float) y2));
	}

	@Override
	public void closePath() {
		OMSVGPathElement path = (OMSVGPathElement) _current;
		path.getPathSegList().appendItem(path.createSVGPathSegClosePath());
	}

	@Override
	public void endData() {
		// Ignore.
	}

	@Override
	public void endPath() {
		// Ignore.
	}

	@Override
	public void rect(double x, double y, double w, double h, double rx, double ry) {
		appendChild(_doc.createSVGRectElement((float) x, (float) y, (float) w, (float) h, (float) rx, (float) ry));
	}

	@Override
	public void writeCssClass(String cssClass) {
		_current.setAttribute("class", cssClass);
	}

	@Override
	public void writeId(String id) {
		_current.setAttribute("id", id);
	}

	@Override
	public void text(double x, double y, String text) {
		appendChild(_doc.createSVGTextElement((float) x, (float) y, OMSVGLength.SVG_LENGTHTYPE_PX, text));
	}

	private void setParent(OMSVGElement next) {
		_parent = next;
		_current = next;
	}

	private void appendChild(OMSVGElement next) {
		_parent.appendChild(next);
		_current = next;
	}

	@Override
	public void close() {
		// Ignore.
	}

}
