/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.awt.Dimension;

import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the dimension of an object.
 * 
 * @see ImageModel#DIMENSION_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DimensionListener extends PropertyListener {

	/**
	 * Handles change of the dimension of the given object.
	 * 
	 * @param sender
	 *        The object whose dimension changed.
	 * @param oldDimension
	 *        The old dimension.
	 * @param newDimension
	 *        The new dimension.
	 * 
	 * @see ImageModel#getDimension()
	 */
	void handleDimensionChanged(Object sender, Dimension oldDimension, Dimension newDimension);

}

