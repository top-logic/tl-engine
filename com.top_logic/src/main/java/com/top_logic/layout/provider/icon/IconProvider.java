/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.icon;

import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Provider computing the icon for an object.
 * 
 * @see ResourceProvider#getImage(Object, Flavor)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface IconProvider {

	/**
	 * {@link IconProvider} delivering no icons for all arguments.
	 */
	IconProvider NONE = (x, f) -> ThemeImage.none();

	/**
	 * Compute the icon for displaying the given object.
	 * 
	 * @param object
	 *        The object to display.
	 * 
	 * @return The icon to represent the given object with.
	 * 
	 * @see #getIcon(Object, Flavor)
	 */
	default ThemeImage getIcon(Object object) {
		return getIcon(object, Flavor.DEFAULT);
	}

	/**
	 * Compute the icon for displaying the given object.
	 * 
	 * @param object
	 *        The object to display.
	 * @param flavor
	 *        See {@link ResourceProvider#getImage(Object, Flavor)}.
	 * 
	 * @return The icon to represent the given object with.
	 * 
	 * @see #getIcon(Object)
	 */
	ThemeImage getIcon(Object object, Flavor flavor);

}
