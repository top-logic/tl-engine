/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

import com.top_logic.graphic.blocks.math.Mat;
import com.top_logic.graphic.blocks.math.Vec;
import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.event.Registration;
import com.top_logic.graphic.blocks.svg.event.SVGClickEvent;
import com.top_logic.graphic.blocks.svg.event.SVGClickHandler;
import com.top_logic.graphic.blocks.svg.event.SVGDropHandler;
import com.top_logic.graphic.flow.data.ImageAlign;
import com.top_logic.graphic.flow.data.ImageScale;

/**
 * API for stream-based construction of SVG.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SvgWriter extends AutoCloseable {

	/**
	 * Internal factor for creating rounded corners that approximate arcs.
	 */
	double K1 = 0.5522847498307933984022516322796;

	/**
	 * Internal factor for creating rounded corners that approximate arcs.
	 */
	double K2 = 1.0 - K1;

	/**
	 * Starts a <code>svg</code> tag.
	 */
	void beginSvg();

	/**
	 * Writes <code>width</code>, <code>height</code>, and <code>viewBox</code> attributes.
	 *
	 */
	void dimensions(String width, String height, double x1, double y1, double x2, double y2);

	/**
	 * Closes a <code>svg</code> tag.
	 */
	void endSvg();

	/**
	 * Starts a <code>g</code> tag.
	 */
	void beginGroup(Object model);

	/**
	 * Starts an anonymous <code>g</code> tag.
	 */
	default void beginGroup() {
		beginGroup(null);
	}

	/**
	 * Writes a <code>translate</code> <code>transform</code> attribute.
	 *
	 * @param dx
	 *        The translation along the X axis.
	 * @param dy
	 *        The translation along the Y axis.
	 */
	void translate(double dx, double dy);

	/**
	 * Writes a <code>transform</code> attribute containing a transformation matrix.
	 */
	default void transform(Mat m) {
		transform(m.a(), m.b(), m.c(), m.d(), m.e(), m.f());
	}

	/**
	 * Writes a <code>transform</code> attribute containing a transformation matrix.
	 * 
	 * <pre>
	 * <code>
	 * [a c e]
	 * [b d f]
	 * [0 0 1]
	 * </code>
	 * </pre>
	 */
	void transform(double a, double b, double c, double d, double e, double f);


	/**
	 * Closes a <code>g</code> tag.
	 */
	void endGroup();

	/**
	 * Starts an anonymous <code>path</code> tag.
	 */
	default void beginPath() {
		beginPath(null);
	}

	/**
	 * Starts a <code>path</code> tag.
	 */
	void beginPath(Object model);

	/**
	 * Starts a <code>data</code> attribute.
	 */
	void beginData();

	/**
	 * Writes a <code>m</code> path segment.
	 */
	default void moveToRel(Vec v) {
		moveToRel(v.getX(), v.getY());
	}

	/**
	 * Writes a <code>m</code> path segment.
	 */
	void moveToRel(double dx, double dy);

	/**
	 * Writes a <code>M</code> path segment.
	 */
	default void moveToAbs(Vec v) {
		moveToAbs(v.getX(), v.getY());
	}

	/**
	 * Writes a <code>M</code> path segment.
	 */
	void moveToAbs(double x, double y);

	/**
	 * Writes a <code>l</code> path segment.
	 */
	default void lineToRel(Vec v) {
		lineToRel(v.getX(), v.getY());
	}

	/**
	 * Writes a <code>l</code> path segment.
	 */
	void lineToRel(double dx, double dy);

	/**
	 * Writes a <code>L</code> path segment.
	 */
	default void lineToAbs(Vec v) {
		lineToAbs(v.getX(), v.getY());
	}

	/**
	 * Writes a <code>L</code> path segment.
	 */
	void lineToAbs(double x, double y);

	/**
	 * Writes a <code>h</code> path segment.
	 */
	void lineToHorizontalRel(double dx);

	/**
	 * Writes a <code>H</code> path segment.
	 */
	void lineToHorizontalAbs(double x);

	/**
	 * Writes a <code>v</code> path segment.
	 */
	void lineToVerticalRel(double dy);

	/**
	 * Writes a <code>V</code> path segment.
	 */
	void lineToVerticalAbs(double y);

	/**
	 * Writes a <code>c</code> path segment.
	 */
	void curveToRel(double dx1, double dy1, double dx2, double dy2, double dx, double dy);

	/**
	 * Writes a <code>C</code> path segment.
	 */
	void curveToAbs(double x1, double y1, double x2, double y2, double x, double y);

	/**
	 * Draws a clockwise approximated rounded corner
	 * 
	 * <xmp>
	 *             rx
	 *    +----x--------------x
	 *    |           .....
	 *    x      .....
	 * ry |   ...
	 *    |..
	 *    x
	 * </xmp>
	 *
	 * @param ccw
	 *        Whether to create a counter-clockwise rounded corner.
	 * @param rx
	 *        The radius on the x axis.
	 * @param ry
	 *        The radius on the y axis.
	 */
	default void roundedCorner(boolean ccw, double rx, double ry) {
		if (rx != 0 && ry != 0) {
			if (rx > 0 ^ ry > 0 ^ ccw) {
				curveToRel(0, K1 * ry, K2 * rx, ry, rx, ry);
			} else {
				curveToRel(K1 * rx, 0, rx, K2 * ry, rx, ry);
			}
		}
	}

	/**
	 * Writes a <code>z</code> path segment.
	 */
	void closePath();

	/**
	 * Closes a <code>data</code> attribute.
	 */
	void endData();

	/**
	 * Closes a <code>path</code> element.
	 */
	void endPath();

	/**
	 * Starts an anonymous {@value SvgConstants#POLYLINE} tag.
	 */
	default void beginPolyline() {
		beginPolyline(null);
	}

	/**
	 * Starts a {@value SvgConstants#POLYLINE} tag.
	 */
	void beginPolyline(Object model);

	/**
	 * Closes a {@value SvgConstants#POLYLINE} tag.
	 */
	void endPolyline();

	/**
	 * Starts an anonymous {@value SvgConstants#POLYGON} tag.
	 */
	default void beginPolygon() {
		beginPolygon(null);
	}

	/**
	 * Starts a {@value SvgConstants#POLYGON} tag.
	 */
	void beginPolygon(Object model);

	/**
	 * Closes a {@value SvgConstants#POLYGON} tag.
	 */
	void endPolygon();

	/**
	 * Writes a <code>points</code> attribute
	 * 
	 * @param points
	 *        Space separated pairs of x,y coordinates.
	 */
	void writePoints(String points);

	@Override
	void close();

	/**
	 * Creates a <code>rect</code> element.
	 */
	default void rect(double x, double y, double w, double h) {
		rect(x, y, w, h, 0.0);
	}

	/**
	 * Creates a <code>rect</code> element.
	 */
	default void rect(double x, double y, double w, double h, double radius) {
		rect(x, y, w, h, radius, radius);
	}

	/**
	 * Creates a <code>rect</code> element.
	 */
	default void rect(double x, double y, double w, double h, double rx, double ry) {
		beginRect(x, y, w, h, rx, ry);
		endRect();
	}

	/**
	 * Opens a <code>rect</code> element.
	 */
	default void beginRect(double x, double y, double w, double h) {
		beginRect(x, y, w, h, 0, 0);
	}

	/**
	 * Opens a <code>rect</code> element.
	 */
	void beginRect(double x, double y, double w, double h, double rx, double ry);

	/**
	 * Ends a <code>rect</code> element.
	 */
	void endRect();

	/**
	 * Writes a <code>id</code> attribute.
	 */
	void writeId(String id);

	/**
	 * Writes a <code>class</code> attribute.
	 */
	void writeCssClass(String cssClass);

	/**
	 * Writes a <code>text</code> element.
	 */
	default void text(double x, double y, String text) {
		beginText(x, y, text);
		endText();
	}

	/**
	 * Starts a <code>text</code> element.
	 */
	void beginText(double x, double y, String text);

	/**
	 * Adds text tyle properties to a text element.
	 */
	void setTextStyle(String fontFamily, String fontSize, String fontWeight);

	/**
	 * Ends a text element.
	 * 
	 * @see #beginText(double, double, String)
	 */
	void endText();

	/**
	 * Sets the <code>fill-opacity</code> style on the current element.
	 */
	void setFillOpacity(double value);

	/**
	 * Sets the <code>stroke-opacity</code> style on the current element.
	 */
	void setStrokeOpacity(double value);

	/**
	 * Sets the <code>stroke-width</code> style on the current element.
	 */
	void setStrokeWidth(double value);

	/**
	 * Sets the <code>fill</code> style on the current element.
	 */
	void setFill(SVGColor color);

	/**
	 * Sets the <code>fill</code> style on the current element.
	 */
	void setFill(String style);

	/**
	 * Sets the <code>stroke</code> style on the current element.
	 */
	void setStroke(SVGColor color);

	/**
	 * Sets the <code>stroke</code> style on the current element.
	 */
	void setStroke(String style);

	/**
	 * Sets the <code>stroke-dasharray</code> style on the current element.
	 */
	void setStrokeDasharray(double... dashes);

	/**
	 * Writes an embedded image.
	 *
	 * @param x
	 *        The X coordinate of the origin.
	 * @param y
	 *        The Y coordinate of the origin.
	 * @param width
	 *        The width of the image area.
	 * @param height
	 *        The width of the image area.
	 * @param href
	 *        The URL from which to load the image data.
	 * @param align
	 *        The alignment of the image in its reserved area.
	 * @param scale
	 *        The scaling strategy.
	 */
	void image(double x, double y, double width, double height, String href, ImageAlign align, ImageScale scale);

	/**
	 * Callback for writing contents that have their own {@link Drawable} representation.
	 * 
	 * <p>
	 * Use this method instead of directly invoking {@link Drawable#draw(SvgWriter)} to allow the
	 * system to short-cut writing contents during local updates.
	 * </p>
	 */
	default void write(Drawable element) {
		element.draw(this);
	}

	/**
	 * Attaches a callback to the created SVG document that is called, if the user clicks on the
	 * currently created element.
	 * 
	 * <p>
	 * This is an optional method that is only supported, for special writers that construct
	 * interactive documents. In all other cases, calls to this method are ignored.
	 * </p>
	 *
	 * @param handler
	 *        The callback that is invoked upon click.
	 * @param sender
	 *        The user object to pass to the invoked callback. See
	 *        {@link SVGClickEvent#getSender()}.
	 */
	default Registration attachOnClick(SVGClickHandler handler, Object sender) {
		// Ignore by default. This is only supported in specialized writers that build interactive
		// DOM trees.
		return Registration.NONE;
	}

	/**
	 * Writes a custom SVG attribute.
	 *
	 * @param name
	 *        The attribute name.
	 * @param value
	 *        The attribute value.
	 */
	void writeAttribute(String name, String value);

	/**
	 * Attatches a callback to the created SVG element that captures a drop.
	 *
	 * @param handler
	 *        The callback that is invoked upon click.
	 * @param sender
	 *        The user object to pass to the invoked callback. See
	 *        {@link SVGClickEvent#getSender()}.
	 */
	default Registration attachOnDrop(SVGDropHandler handler, Object sender) {
		// Ignore by default. This is only supported in specialized writers that build interactive
		// DOM trees.
		return Registration.NONE;
	}

}
