/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * A border decoration drawn around an element.
 */
public class Border extends Decoration {

	private String _strokeStyle = "black";

	private double _thickness = 1;

	private boolean _top = true;

	private boolean _left = true;

	private boolean _bottom = true;

	private boolean _right = true;

	private double[] _dashes = null;

	/**
	 * The stroke width to draw.
	 */
	public double getThickness() {
		return _thickness;
	}

	/**
	 * @see #getThickness()
	 */
	public Border setThickness(double thickness) {
		_thickness = thickness;
		return this;
	}

	/**
	 * The border stroke style.
	 */
	public String getStrokeStyle() {
		return _strokeStyle;
	}

	/**
	 * @see #getStrokeStyle()
	 */
	public Border setStrokeStyle(String style) {
		_strokeStyle = style;
		return this;
	}

	/**
	 * The dash pattern of the border.
	 */
	public double[] getDashes() {
		return _dashes;
	}

	/**
	 * @see #getDashes()
	 */
	public Border setDashes(double[] dashes) {
		_dashes = dashes;
		return this;
	}

	/**
	 * Whether the top border is painted.
	 */
	public boolean isTop() {
		return _top;
	}

	/**
	 * @see #isTop()
	 */
	public Border setTop(boolean top) {
		_top = top;
		return this;
	}

	/**
	 * Whether the right border is painted.
	 */
	public boolean isRight() {
		return _right;
	}

	/**
	 * @see #isRight()
	 */
	public Border setRight(boolean right) {
		_right = right;
		return this;
	}

	/**
	 * Whether the bottom border is painted.
	 */
	public boolean isBottom() {
		return _bottom;
	}

	/**
	 * @see #isBottom()
	 */
	public Border setBottom(boolean bottom) {
		_bottom = bottom;
		return this;
	}

	/**
	 * Whether the left border is painted.
	 */
	public boolean isLeft() {
		return _left;
	}

	/**
	 * @see #isLeft()
	 */
	public Border setLeft(boolean left) {
		_left = left;
		return this;
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		super.computeIntrinsicSize(context, offsetX, offsetY);

		double width = getContent().getWidth();
		double height = getContent().getHeight();
		if (_top) {
			height += _thickness;
		}
		if (_left) {
			width += _thickness;
		}
		if (_bottom) {
			height += _thickness;
		}
		if (_right) {
			width += _thickness;
		}

		setWidth(width);
		setHeight(height);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		double contentX = offsetX;
		double contentY = offsetY;
		double contentWidth = width;
		double contentHeight = height;

		if (_top) {
			contentY += _thickness;
			contentHeight -= _thickness;
		}
		if (_left) {
			contentX += _thickness;
			contentWidth -= _thickness;
		}
		if (_bottom) {
			contentHeight -= _thickness;
		}
		if (_right) {
			contentWidth -= _thickness;
		}

		getContent().distributeSize(context, contentX, contentY, contentWidth, contentHeight);

		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void draw(SvgWriter out) {
		super.draw(out);

		double radius = _thickness / 2;

		out.beginPath();
		out.setStrokeWidth(_thickness);
		out.setStroke(_strokeStyle);
		if (_dashes != null) {
			out.setStrokeDasharray(_dashes);
		}
		out.setFill("none");
		out.beginData();
		out.moveToAbs(getX() + (_left ? radius : 0), getY() + radius);
		if (_top) {
			out.lineToAbs(getX() + getWidth() - (_right ? radius : 0), getY() + radius);
		} else {
			out.moveToAbs(getX() + getWidth() - radius, getY());
		}
		if (_right) {
			out.lineToAbs(getX() + getWidth() - radius, getY() + getHeight() - (_bottom ? radius : 0));
		} else {
			out.moveToAbs(getX() + getWidth(), getY() + getHeight() - radius);
		}
		if (_bottom) {
			out.lineToAbs(getX() + (_left ? radius : 0), getY() + getHeight() - radius);
		} else {
			out.moveToAbs(getX() + radius, getY() + getHeight());
		}
		if (_left) {
			if (_top && _right && _bottom) {
				out.closePath();
			} else {
				out.lineToAbs(getX() + radius, getY());
			}
		}
		out.endData();
		out.endPath();
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created

	}

}
