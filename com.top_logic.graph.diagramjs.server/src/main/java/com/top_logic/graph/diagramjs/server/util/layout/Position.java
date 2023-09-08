/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.layout;

/**
 * Position for a graphical component.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Position {
	private double _x;

	private double _y;

	/**
	 * Creates a {@link Position} object.
	 * 
	 * @param x
	 *        Horizontal coordinate.
	 * @param y
	 *        Vertical coordinate.
	 */
	public Position(double x, double y) {
		_x = x;
		_y = y;
	}

	/**
	 * Horizontal coordinate.
	 */
	public double getX() {
		return _x;
	}

	/**
	 * @see #getX()
	 */
	public void setX(double x) {
		_x = x;
	}

	/**
	 * Vertical coordinate.
	 */
	public double getY() {
		return _y;
	}

	/**
	 * @see #getY()
	 */
	public void setY(double y) {
		_y = y;
	}
}
