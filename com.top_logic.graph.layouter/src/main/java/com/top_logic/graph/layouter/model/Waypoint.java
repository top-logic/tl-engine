/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model;

import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;

/**
 * Two dimensional waypoint to describe a part of an edge.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Waypoint {
	private double _x;

	private double _y;

	/**
	 * Creates a waypoint with the given coordinates.
	 */
	public Waypoint(double x, double y) {
		setX(x);
		setY(y);
	}

	/**
	 * Creates a waypoint for the node with his coordinates.
	 */
	public Waypoint(LayoutNode node) {
		_x = node.getX();
		_y = node.getY();
	}

	/**
	 * The horizontal coordinate of this waypoint.
	 */
	public double getX() {
		return _x;
	}

	/**
	 * Set a new horizontal coordinate for this waypoint.
	 */
	public void setX(double x) {
		_x = x;
	}

	/**
	 * The vertical coordinate of this waypoint.
	 */
	public double getY() {
		return _y;
	}

	/**
	 * Set a new vertical coordinate for this waypoint.
	 */
	public void setY(double y) {
		_y = y;
	}

	@Override
	public String toString() {
		return "(" + _x + ", " + _y + ")";
	}
}
