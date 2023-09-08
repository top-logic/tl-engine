/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Handles change of the image of a {@link ButtonUIModel}.
 * 
 * @see ButtonUIModel#IMAGE_PROPERTY
 * @see ButtonUIModel#NOT_EXECUTABLE_IMAGE_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ButtonImageListener extends PropertyListener {

	/**
	 * Handles change of the {@link ButtonUIModel#getImage() image} of the given
	 * {@link ButtonUIModel}.
	 * 
	 * @param sender
	 *        The {@link ButtonUIModel} whose image changed.
	 * @param oldValue
	 *        The old image.
	 * @param newValue
	 *        The new image.
	 * @return Whether this event shall bubble.
	 * 
	 * @see ButtonUIModel#getImage()
	 */
	Bubble handleImageChanged(ButtonUIModel sender, ThemeImage oldValue, ThemeImage newValue);

	/**
	 * Handles change of the {@link ButtonUIModel#getImage() image} of the given
	 * {@link ButtonUIModel}.
	 * 
	 * @param sender
	 *        The {@link ButtonUIModel} whose not executable image changed.
	 * @param oldValue
	 *        The old image.
	 * @param newValue
	 *        The new image.
	 * @return Whether this event shall bubble.
	 * 
	 * @see ButtonUIModel#NOT_EXECUTABLE_IMAGE_PROPERTY
	 * @see ButtonUIModel#getNotExecutableImage()
	 */
	Bubble handleDisabledImageChanged(ButtonUIModel sender, ThemeImage oldValue, ThemeImage newValue);

}

