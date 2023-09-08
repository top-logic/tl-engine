/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util.layout;

/**
 * Bounds for a graphical component.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Bounds {
	private Position _position;

	private Dimension _dimension;

	/**
	 * Creates {@link Bounds}.
	 */
	public Bounds(Position position, Dimension dimension) {
		_position = position;
		_dimension = dimension;
	}

	/**
	 * @see #Bounds(Position, Dimension)
	 */
	public Bounds(double x, double y, double width, double height) {
		this(new Position(x, y), new Dimension(width, height));
	}

	/**
	 * @see Position
	 */
	public Position getPosition() {
		return _position;
	}

	/**
	 * @see #getPosition()
	 */
	public void setPosition(Position position) {
		_position = position;
	}

	/**
	 * @see Dimension
	 */
	public Dimension getDimension() {
		return _dimension;
	}

	/**
	 * @see #getDimension()
	 */
	public void setDimension(Dimension dimension) {
		_dimension = dimension;
	}
}
