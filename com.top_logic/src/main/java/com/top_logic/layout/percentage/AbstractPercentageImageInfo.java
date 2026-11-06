/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.percentage;

import java.awt.Paint;
import java.awt.Shape;

/**
 * Description of an image displaying a percentage as a row of shapes, of which a fraction
 * corresponding to the percentage is filled.
 * 
 * @see PercentageImageCreator
 * 
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
	 * Returns the number of shapes in the image.
	 */
	public int getShapeNumber() {
		return this.shapeNumber;
	}

	/**
	 * Returns the width of the space between two shapes.
	 */
	public int getSpaceWidth() {
		return this.spaceWidth;
	}

	/**
	 * Returns the {@link Paint} of filled shapes.
	 */
	public Paint getShapeForeground() {
		return this.shapeForeground;
	}

	/**
	 * Returns the {@link Paint} of unfilled shapes.
	 */
	public Paint getShapeBackground() {
		return this.shapeBackground;
	}

	/**
	 * Returns the {@link Paint} of the space between two shapes.
	 */
	public Paint getSpacePaint() {
		return this.spacePaint;
	}
	
}
