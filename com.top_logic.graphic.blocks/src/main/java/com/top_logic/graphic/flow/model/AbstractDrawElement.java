/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

/**
 * 
 */
public abstract class AbstractDrawElement implements DrawElement {

	private double _x;

	private double _y;

	private double _width;

	private double _height;

	@Override
	public double getX() {
		return _x;
	}

	@Override
	public double getY() {
		return _y;
	}

	@Override
	public double getWidth() {
		return _width;
	}

	@Override
	public double getHeight() {
		return _height;
	}

	public void setX(double x) {
		_x = x;
	}

	public void setY(double y) {
		_y = y;
	}

	protected void setWidth(double width) {
		_width = width;
	}

	protected void setHeight(double height) {
		_height = height;
	}

}
