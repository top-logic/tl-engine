/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.svg;

import com.top_logic.graphic.blocks.math.Vec;

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
	void beginGroup();

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
	 * Closes a <code>g</code> tag.
	 */
	void endGroup();

	/**
	 * Starts a <code>path</code> tag.
	 */
	void beginPath();

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
	void rect(double x, double y, double w, double h, double rx, double ry);

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
	void text(double x, double y, String text);

}
