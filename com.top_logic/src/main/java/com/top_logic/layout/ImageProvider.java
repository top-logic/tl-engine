/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.ThemeImage;

/**
 * The class {@link ImageProvider} provides {@link ThemeImage images} for
 * objects.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ImageProvider {

	/**
	 * Returns an image for the given object.
	 * 
	 * @param obj
	 *        The object for which a {@link ThemeImage} is requested.
	 * @param flavor
	 *        The image {@link Flavor} to request.
	 */
	ThemeImage getImage(Object obj, Flavor flavor);

	/**
	 * Creates a {@link ImageProvider} returning a constant {@link ThemeImage}.
	 * 
	 * @param icon
	 *        The only {@link ThemeImage} which should be provided by the {@link ImageProvider}.
	 * 
	 * @return The created {@link ImageProvider}.
	 */
	static ImageProvider constantImageProvider(ThemeImage icon) {
		return (any, flavor) -> icon;
	}

}
