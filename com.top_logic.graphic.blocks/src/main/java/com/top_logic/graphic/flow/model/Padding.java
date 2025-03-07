/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * Inserts some padding around another element.
 */
public class Padding extends Decoration {

	private double _top;

	private double _left;

	private double _right;

	private double _bottom;

	/**
	 * The top padding.
	 */
	public double getTop() {
		return _top;
	}

	/**
	 * @see #getTop()
	 */
	public Padding setTop(double top) {
		_top = top;
		return this;
	}

	/**
	 * The left padding.
	 */
	public double getLeft() {
		return _left;
	}

	/**
	 * @see #getLeft()
	 */
	public Padding setLeft(double left) {
		_left = left;
		return this;
	}

	/**
	 * The right padding.
	 */
	public double getRight() {
		return _right;
	}

	/**
	 * @see #getRight()
	 */
	public Padding setRight(double right) {
		_right = right;
		return this;
	}

	/**
	 * The bottom padding.
	 */
	public double getBottom() {
		return _bottom;
	}

	/**
	 * @see #getBottom()
	 */
	public Padding setBottom(double bottom) {
		_bottom = bottom;
		return this;
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		setX(offsetX);
		setY(offsetY);

		double x = offsetX + _left;
		double y = offsetY + _top;

		DrawElement content = getContent();
		content.computeIntrinsicSize(context, x, y);

		setWidth(_left + content.getWidth() + _right);
		setHeight(_top + content.getHeight() + _bottom);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);

		getContent().distributeSize(context, offsetX + _left, offsetY + _top, width - _left - _right,
			height - _top - _bottom);

		setWidth(width);
		setHeight(height);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created

	}

}
