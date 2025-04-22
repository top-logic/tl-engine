/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGImageElement;
import org.vectomatic.dom.svg.OMSVGLength;
import org.vectomatic.dom.svg.OMSVGPathElement;
import org.vectomatic.dom.svg.OMSVGRectElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTransform;
import org.vectomatic.dom.svg.OMSVGTransformList;
import org.vectomatic.dom.svg.itf.ISVGTransformable;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.top_logic.graphic.blocks.svg.SVGColor;
import com.top_logic.graphic.blocks.svg.SvgUtil;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.event.MouseButton;
import com.top_logic.graphic.blocks.svg.event.Registration;
import com.top_logic.graphic.blocks.svg.event.SVGClickEvent;
import com.top_logic.graphic.blocks.svg.event.SVGClickHandler;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;

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

	/**
	 * The currently created document.
	 */
	protected final OMSVGDocument getDoc() {
		return _doc;
	}

	@Override
	public void beginSvg() {
		if (_parent != _root) {
			beginGroup();
		}
	}

	@Override
	public void dimensions(String width, String height, double x1, double y1, double x2, double y2) {
		if (_parent == _root) {
			_root.setAttribute(WIDTH_ATTR, width);
			_root.setAttribute(HEIGHT_ATTR, height);
			_root.setAttribute(VIEW_BOX_ATTR, x1 + " " + y1 + " " + x2 + " " + y2);
		}
	}

	@Override
	public void endSvg() {
		if (_parent != _root) {
			endGroup();
		}
	}

	@Override
	public void beginGroup(Object model) {
		OMSVGGElement next = _doc.createSVGGElement();
		_parent.appendChild(next);
		setParent(next);

		created(next, model);
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
	public void beginPath(Object model) {
		OMSVGPathElement path = _doc.createSVGPathElement();
		appendChild(path);

		created(path, model);
	}

	@Override
	public void setFillOpacity(double value) {
		_current.setAttribute(FILL_OPACITY_ATTR, Double.toString(value));
	}

	@Override
	public void setStrokeOpacity(double value) {
		_current.setAttribute(STROKE_OPACITY_ATTR, Double.toString(value));
	}

	@Override
	public void setStrokeWidth(double value) {
		_current.setAttribute(STROKE_WIDTH_ATTR, Double.toString(value));
	}

	@Override
	public void setFill(SVGColor color) {
		setFill(SvgUtil.html(color));
	}

	@Override
	public void setFill(String style) {
		_current.setAttribute(FILL_ATTR, style);
	}

	@Override
	public void setStroke(SVGColor color) {
		setStroke(SvgUtil.html(color));
	}

	@Override
	public void setStroke(String style) {
		_current.setAttribute(STROKE_ATTR, style);
	}

	@Override
	public void setStrokeDasharray(double... dashes) {
		_current.setAttribute(STROKE_DASHARRAY_ATTR, SvgUtil.valueList(dashes));
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
	public void beginRect(double x, double y, double w, double h, double rx, double ry) {
		OMSVGRectElement rect = _doc.createSVGRectElement((float) x, (float) y, (float) w, (float) h, (float) rx, (float) ry);
		appendChild(rect);
		_current = rect;
	}

	@Override
	public void endRect() {
		_current = null;
	}

	@Override
	public void writeCssClass(String cssClass) {
		_current.setAttribute(CLASS_ATTR, cssClass);
	}

	@Override
	public void writeId(String id) {
		_current.setAttribute(ID_ATTR, id);
	}

	@Override
	public void text(double x, double y, String text) {
		appendChild(_doc.createSVGTextElement((float) x, (float) y, OMSVGLength.SVG_LENGTHTYPE_PX, text));
	}

	@Override
	public void image(double x, double y, double width, double height, String href, ImageAlign align,
			ImageScale scale) {
		OMSVGImageElement img = _doc.createSVGImageElement((float) x, (float) y, (float) width, (float) height, href);
		img.setAttribute(PRESERVE_ASPECT_RATIO_ATTR, align + " " + scale);
		appendChild(img);
	}

	@Override
	public Registration attachOnClick(SVGClickHandler handler, Object sender) {
		HandlerRegistration registration = _current.addDomHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				handler.onClick(new SVGClickEvent() {
					@Override
					public Object getSender() {
						return sender;
					}

					@Override
					public boolean getButton(MouseButton button) {
						int nativeButton = event.getNativeButton();
						switch (button) {
							case LEFT:
								return (nativeButton & NativeEvent.BUTTON_LEFT) != 0;
							case RIGHT:
								return (nativeButton & NativeEvent.BUTTON_RIGHT) != 0;
							case MIDDLE:
								return (nativeButton & NativeEvent.BUTTON_MIDDLE) != 0;
						}
						return false;
					}

					@Override
					public boolean isMetaKey() {
						return event.getNativeEvent().getMetaKey();
					}

					@Override
					public boolean isShiftKey() {
						return event.getNativeEvent().getShiftKey();
					}

					@Override
					public boolean isAltKey() {
						return event.getNativeEvent().getAltKey();
					}

					@Override
					public boolean isCtrlKey() {
						return event.getNativeEvent().getCtrlKey();
					}

					@Override
					public void stopPropagation() {
						event.getNativeEvent().stopPropagation();
					}
				});
			}
		}, ClickEvent.getType());

		return () -> registration.removeHandler();
	}

	private void created(OMSVGElement svgElement, Object model) {
		if (model != null) {
			linkModel(svgElement, model);
		}
	}

	/**
	 * Hook that is called, whenever a SVG element is created that is associated to some model
	 * object.
	 * 
	 * @param svgElement
	 *        The created element.
	 * @param model
	 *        the associated model object.
	 */
	protected void linkModel(OMSVGElement svgElement, Object model) {
		// Hook for subclasses.
	}

	/**
	 * Updates the parent and current pointers to the given newly created element.
	 */
	protected final void setParent(OMSVGElement next) {
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
