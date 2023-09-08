/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.percentage;

import java.awt.Paint;
import java.awt.Shape;

/**
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public abstract class AbstractPercentageImageInfo {

	private int shapeNumber;
	private int spaceWidth;
	private Paint shapeForeground;
	private Paint shapeBackground;
	private Paint spacePaint;
	
	/**
	 * Creates a new percentage image info. 
	 */
	public AbstractPercentageImageInfo(int shapeNumber, int spaceWidth, Paint shapeForeground, Paint shapeBackground, Paint spacePaint) {
		super();
		
		this.shapeNumber = shapeNumber;
		this.spaceWidth = spaceWidth;
		this.shapeForeground = shapeForeground;
		this.shapeBackground = shapeBackground;
		this.spacePaint = spacePaint;
	}

	/** 
	 * This method creates a shape with the given position.
	 * 
	 * @param x The x position.
	 * @param y The y position.
	 */
	public abstract Shape createShape(int x, int y);

	/**
	 * Returns the shapeNumber.
	 */
	public int getShapeNumber() {
		return this.shapeNumber;
	}

	/**
	 * Returns the spaceWidth.
	 */
	public int getSpaceWidth() {
		return this.spaceWidth;
	}

	/**
	 * Returns the shapeForeground.
	 */
	public Paint getShapeForeground() {
		return this.shapeForeground;
	}

	/**
	 * Returns the shapeBackground.
	 */
	public Paint getShapeBackground() {
		return this.shapeBackground;
	}

	/**
	 * Returns the spaceColor.
	 */
	public Paint getSpacePaint() {
		return this.spacePaint;
	}
	
}
