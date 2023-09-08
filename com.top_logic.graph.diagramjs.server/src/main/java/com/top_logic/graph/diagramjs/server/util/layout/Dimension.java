/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.layout;

/**
 * Dimension for a graphical component.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Dimension {
	private double _width;

	private double _height;

	/**
	 * Creates a {@link Dimension} object.
	 * 
	 * @param width
	 *        Width of the component.
	 * @param height
	 *        Height of the component.
	 */
	public Dimension(double width, double height) {
		_width = width;
		_height = height;
	}

	/**
	 * Width of the component.
	 */
	public double getWidth() {
		return _width;
	}

	/**
	 * @see #getWidth()
	 */
	public void setWidth(double width) {
		_width = width;
	}

	/**
	 * Height of the component.
	 */
	public double getHeight() {
		return _height;
	}

	/**
	 * @see #getHeight()
	 */
	public void setHeight(double height) {
		_height = height;
	}
}
