/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;

/**
 * The interface {@link LayoutData} defines constraints for a {@link LayoutControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutData {
	
	/**
	 * This method returns the width of this constraint.
	 */
	public float getWidth();

	/**
	 * This method returns the width of this constraint as {@link DisplayDimension}.
	 */
	DisplayDimension getWidthDimension();

	/**
	 * This method returns the height of this constraint.
	 */
	public float getHeight();

	/**
	 * This method returns the height of this constraint as {@link DisplayDimension}.
	 */
	DisplayDimension getHeightDimension();

	/**
	 * This method returns the unit in which the width and/or minimum width is given.
	 */
	public DisplayUnit getWidthUnit();

	/**
	 * This method returns the unit in which the height and/or minimum height is given.
	 */
	public DisplayUnit getHeightUnit();

	/**
	 * An updated version of this instance with adjusted {@link #getWidth()} and
	 * {@link #getHeight()}.
	 * 
	 * @param newWidth
	 *        The new value of {@link #getWidth()}
	 * @param newHeight
	 *        The new value of {@link #getHeight()}.
	 * @return The instance with the new values. It is implementation-dependent, whether this is a
	 *         copy or this instance with internally updated values.
	 */
	public LayoutData resized(DisplayDimension newWidth, DisplayDimension newHeight);

	/**
	 * This method returns how much percent of the possible width will be used by children of the
	 * corresponding {@link LayoutControl} whose size is given in {@link DisplayUnit#PIXEL}.
	 * 
	 * @return a size &lt;= 100 and &gt;= 0
	 */
	public int getMaxWidth();


	/**
	 * This method returns how much percent of the possible height will be used by children of the
	 * corresponding {@link LayoutControl} whose size is given in {@link DisplayUnit#PIXEL}.
	 * 
	 * @return a size &lt;= 100 and &gt;= 0
	 */
	public int getMaxHeight();

	/**
	 * the minimum width of this constraint, using the {@link DisplayUnit}, retrievable via
	 *         {@link #getWidthUnit()}
	 */
	public int getMinWidth();

	/**
	 * the minimum height of this constraint, using the {@link DisplayUnit}, retrievable via
	 *         {@link #getWidthUnit()}
	 */
	public int getMinHeight();

	/**
	 * When the {@link LayoutControl} should display a scroll-bar.
	 */
	public Scrolling getScrollable();

}
