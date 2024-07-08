/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.knowledge.gui.layout.SizeInfo;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;

/**
 * The class {@link DefaultLayoutData} is the default immutable holder for the values provided by {@link LayoutData}. 
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultLayoutData implements LayoutData {

	/**
	 * Constraint with 100% width and height and automatic scrolling.
	 */
	public static final LayoutData DEFAULT_CONSTRAINT = new DefaultLayoutData(Scrolling.AUTO);

	/**
	 * Constraint with 100% width and height and no scrolling.
	 */
	public static final LayoutData NO_SCROLL_CONSTRAINT = new DefaultLayoutData(Scrolling.NO);

	private DisplayDimension  height;
	private DisplayDimension  width;

	private final Scrolling scrollable;

	private final int maxHeight, maxWidth, minHeight, minWidth;

	/**
	 * Creates a {@link DefaultLayoutData}.
	 *
	 * @param info
	 *        The configuration to copy information from.
	 */
	public DefaultLayoutData(SizeInfo info) {
		this(info.getWidth(), 100, info.getHeight(), 100, Scrolling.NO);
	}

	/**
	 * Creates a {@link DefaultLayoutData} from the given arguments.
	 * 
	 * @param aWidth
	 *        holder for {@link #getWidth()} and {@link #getWidthUnit()}
	 * @param aMaxWidth
	 *        value of {@link #getMaxWidth()}
	 * @param aHeight
	 *        holder for {@link #getHeight()} and {@link #getHeightUnit()}
	 * @param aMaxHeight
	 *        value of {@link #getMaxHeight()}
	 * @param aScrollable
	 *        value of {@link #getScrollable()}
	 */
	public DefaultLayoutData(DisplayDimension aWidth, int aMaxWidth, DisplayDimension aHeight, int aMaxHeight,
			Scrolling aScrollable) {
		this(aWidth, 0, aMaxWidth, aHeight, 0, aMaxHeight, aScrollable);
	}

	/**
	 * Creates a {@link DefaultLayoutData} from the given arguments.
	 * 
	 * @param aWidth
	 *        holder for {@link #getWidth()} and {@link #getWidthUnit()}
	 * @param minWidth
	 *        value of {@link #getMinWidth()}
	 * @param aMaxWidth
	 *        value of {@link #getMaxWidth()}
	 * @param aHeight
	 *        holder for {@link #getHeight()} and {@link #getHeightUnit()}
	 * @param minHeight
	 *        value of {@link #getMinHeight()}
	 * @param aMaxHeight
	 *        value of {@link #getMaxHeight()}
	 * @param aScrollable
	 *        value of {@link #getScrollable()}
	 */
	public DefaultLayoutData(DisplayDimension aWidth, int minWidth, int aMaxWidth, DisplayDimension aHeight,
			int minHeight, int aMaxHeight, Scrolling aScrollable) {
		this.width = aWidth;
		this.height = aHeight;
		
		// set the maximal height in percent
		if (aMaxHeight > 100 || aMaxHeight < 0) {
			throw new IllegalArgumentException("'aMaxHeight' needs a size leq 100 and geq 0.");
		}
		this.maxHeight = aMaxHeight;

		// set the maximal width in percent
		if (aMaxWidth > 100 || aMaxWidth < 0) {
			throw new IllegalArgumentException("'aMaxWidth' needs a size leq 100 and geq 0.");
		}
		this.maxWidth = aMaxWidth;

		this.minWidth = minWidth;
		this.minHeight = minHeight;

		this.scrollable = aScrollable;
	}

	/**
	 * Creates a {@link DefaultLayoutData} from the given arguments.
	 * 
	 * @param aWidth
	 *        returned by {@link #getWidth()}
	 * @param aWidthUnit
	 *        returned by {@link #getWidthUnit()}
	 * @param aMaxWidth
	 *        returned by {@link #getMaxWidth()}
	 * @param aHeight
	 *        returned by {@link #getHeight()}
	 * @param aHeightUnit
	 *        returned by {@link #getHeightUnit()}
	 * @param aMaxHeight
	 *        returned by {@link #getMaxHeight()}
	 * @param aScrollable
	 *        returned by {@link #getScrollable()}
	 */
	public DefaultLayoutData(int aWidth, DisplayUnit aWidthUnit, int aMaxWidth, int aHeight, DisplayUnit aHeightUnit, int aMaxHeight, Scrolling aScrollable) {
		this(
			DisplayDimension.dim(aWidth, aWidthUnit),
			aMaxWidth,
			DisplayDimension.dim(aHeight, aHeightUnit),
			aMaxHeight,
			aScrollable);
	}

	/**
	 * Creates a {@link DefaultLayoutData}.
	 * 
	 * @param aWidth
	 *            is a {@link String} coding a size and a unit. it must be of the form
	 *            <code>xx</code>%, <code>xx</code>px or <code>xx</code> where
	 *            <code>xx</code> is the {@link String} representation of an int.
	 * @param aHeight
	 *            is a {@link String} coding a size and a unit. it must be of the form
	 *            <code>xx</code>%, <code>xx</code>px or <code>xx</code> where
	 *            <code>xx</code> is the {@link String} representation of an int.
	 */
	public DefaultLayoutData(String aWidth, int aMaxWidth, String aHeight, int aMaxHeight, Scrolling aScrollable) {
		this(DisplayDimension.parseDimension(aWidth), aMaxWidth, DisplayDimension.parseDimension(aHeight), aMaxHeight,
			aScrollable);
	}

	private DefaultLayoutData(Scrolling scrollable) {
		this(DisplayDimension.HUNDERED_PERCENT, 100, DisplayDimension.HUNDERED_PERCENT, 100, scrollable);
	}

	@Override
	public float getHeight() {
		return this.getHeightValue();
	}

	@Override
	public DisplayDimension getHeightDimension() {
		return height;
	}

	@Override
	public DisplayUnit getHeightUnit() {
		return this.height.getUnit();
	}

	@Override
	public float getWidth() {
		return this.getWidthValue();
	}

	@Override
	public DisplayDimension getWidthDimension() {
		return width;
	}

	@Override
	public DisplayUnit getWidthUnit() {
		return this.width.getUnit();
	}

	@Override
	public LayoutData resized(DisplayDimension newWidth, DisplayDimension newHeight) {
		return new DefaultLayoutData(newWidth, this.getMinWidth(), this.getMaxWidth(), newHeight, this.getMinHeight(),
			this.getMaxHeight(), this.getScrollable());
	}

	@Override
	public LayoutData withScrolling(Scrolling scrolling) {
		if (scrolling == this.getScrollable()) {
			return this;
		}
		return new DefaultLayoutData(this.getWidthDimension(), this.getMinWidth(), this.getMaxWidth(),
			this.getHeightDimension(), this.getMinHeight(), this.getMaxHeight(), scrolling);
	}

	@Override
	public Scrolling getScrollable() {
		return this.scrollable;
	}

	@Override
	public int getMaxHeight() {
		return this.maxHeight;
	}

	@Override
	public int getMaxWidth() {
		return this.maxWidth;
	}

	@Override
	public int getMinWidth() {
		return minWidth;
	}

	@Override
	public int getMinHeight() {
		return minHeight;
	}

	private float getHeightValue() {
		return height.getValue();
	}

	private float getWidthValue() {
		return width.getValue();
	}

	/**
	 * Creates a scrolling {@link LayoutData} with the given width and height in pixels.
	 * 
	 * @param width
	 *        see {@link LayoutData#getWidth()}
	 * @param height
	 *        see {@link LayoutData#getHeight()}
	 */
	public static DefaultLayoutData scrollingLayout(int width, int height) {
		return new DefaultLayoutData(
			DisplayDimension.dim(width, DisplayUnit.PIXEL), 100,
			DisplayDimension.dim(height, DisplayUnit.PIXEL), 100,
			Scrolling.AUTO);
	}

	/**
	 * Creates a {@link LayoutData} with the given width and height which doesn't scroll
	 * 
	 * @param width
	 *        see {@link LayoutData#getWidth()}
	 * @param height
	 *        see {@link LayoutData#getHeight()}
	 */
	public static LayoutData newLayoutData(DisplayDimension width, DisplayDimension height) {
		return new DefaultLayoutData(width, 100, height, 100, Scrolling.NO);
	}
}
