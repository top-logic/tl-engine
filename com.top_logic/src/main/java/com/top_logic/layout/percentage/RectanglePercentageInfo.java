/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.percentage;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 * The {@link RectanglePercentageInfo} contains the information to draw a rectangle
 * based percentage bar (40% = [x][x][x][x][ ][ ][ ][ ][ ]...). 
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class RectanglePercentageInfo extends AbstractPercentageImageInfo {

	private int width;
	private int height;

	/**
	 * Creates a new {@link RectanglePercentageInfo}. 
	 */
	public RectanglePercentageInfo(int width, int height, int shapeNumber, int spaceWidth, Paint shapeForeground, Paint shapeBackground, Paint spaceColor) {
		super(shapeNumber, spaceWidth, shapeForeground, shapeBackground, spaceColor);

		this.width = width;
		this.height = height;
	}

	@Override
	public Shape createShape(int x, int y) {
		return new Rectangle2D.Double(x, y, getWidth(), getHeight());
	}

	/**
	 * Returns the width.
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Returns the height.
	 */
	public int getHeight() {
		return this.height;
	}

}
