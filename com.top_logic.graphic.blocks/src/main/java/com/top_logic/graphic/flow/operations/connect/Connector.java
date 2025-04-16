/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.connect;

import com.top_logic.graphic.blocks.math.Vec;

/**
 * An anchor point for connections.
 */
public class Connector {

	private double _x;

	private double _y;

	public double getX() {
		return _x;
	}

	public void setX(double x) {
		_x = x;
	}

	public double getY() {
		return _y;
	}

	public void setY(double y) {
		_y = y;
	}

	public Vec getPos() {
		return Vec.vec(_x, _y);
	}

	public void setPos(Vec pos) {
		_x = pos.getX();
		_y = pos.getY();
	}

	public void setPos(double x, double y) {
		_x = x;
		_y = y;
	}

}
