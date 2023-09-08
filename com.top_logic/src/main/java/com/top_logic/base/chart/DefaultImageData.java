/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.chart;

import java.awt.Dimension;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Default {@link ImageData} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultImageData implements ImageData {

	private final Dimension _dimension;

	private final BinaryData _bytes;

	private final int _headerHeight;

	private final int _descriptionWidth;

	/**
	 * Creates a {@link DefaultImageData}.
	 * 
	 * @param dimension
	 *        See {@link #getDimension()}.
	 * @param bytes
	 *        See {@link #getBytes()}.
	 */
	public DefaultImageData(Dimension dimension, BinaryData bytes) {
		this(dimension, bytes, 0, 0);
	}

	/**
	 * Creates a {@link DefaultImageData}.
	 * 
	 * @param dimension
	 *        See {@link #getDimension()}.
	 * @param bytes
	 *        See {@link #getBytes()}.
	 * @param headerHeight
	 *        See {@link #getHeaderHeight()}.
	 * @param descriptionWidth
	 *        See {@link #getDescriptionWidth()}.
	 */
	public DefaultImageData(Dimension dimension, BinaryData bytes, int headerHeight, int descriptionWidth) {
		_dimension = dimension;
		_bytes = bytes;
		_headerHeight = headerHeight;
		_descriptionWidth = descriptionWidth;
	}


	@Override
	public Dimension getDimension() {
		return _dimension;
	}

	@Override
	public BinaryData getBytes() {
		return _bytes;
	}

	@Override
	public int getHeaderHeight() {
		return _headerHeight;
	}

	@Override
	public int getDescriptionWidth() {
		return _descriptionWidth;
	}

}
