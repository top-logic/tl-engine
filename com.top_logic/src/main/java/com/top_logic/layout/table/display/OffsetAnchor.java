/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

/**
 * Holder for an offset to an anchor.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OffsetAnchor {

	private int _pixelOffset;

	/**
	 * Creates a new {@link OffsetAnchor}.
	 * 
	 * @param pixelOffset
	 *        Scrolling offset to anchor point (e.g. left column border or top row border)
	 */
	OffsetAnchor(int pixelOffset) {
		_pixelOffset = pixelOffset;
	}

	/**
	 * Offset to the anchor in px.
	 */
	public int getIndexPixelOffset() {
		return _pixelOffset;
	}
}

