/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

/**
 * Currently visible row index, to which the client side's viewport is scrolled to.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RowIndexAnchor extends OffsetAnchor {
	
	/**
	 * Constant for no defined {@link RowIndexAnchor}.
	 */
	public static final RowIndexAnchor NONE = RowIndexAnchor.create(-1);

	private int index;

	/**
	 * Creates a {@link RowIndexAnchor}.
	 * 
	 * @param visibleIndex
	 *        - column index or row index
	 * @param indexPixelOffset
	 *        - scrolling offset to index anchor point (e.g. left column border or top row border)
	 */
	public static RowIndexAnchor create(int visibleIndex, int indexPixelOffset) {
		return new RowIndexAnchor(visibleIndex, indexPixelOffset);
	}

	/**
	 * Creates a {@link RowIndexAnchor} without offset.
	 * 
	 * @param visibleIndex
	 *        - column index or row index
	 */
	public static RowIndexAnchor create(int visibleIndex) {
		return create(visibleIndex, 0);
	}

	private RowIndexAnchor(int visibleIndex, int indexPixelOffset) {
		super(indexPixelOffset);
		this.index = visibleIndex;
	}

	public int getIndex() {
		return index;
	}

}