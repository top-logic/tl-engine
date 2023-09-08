/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.layout.basic.ThemeImage;

/**
 * A {@link ResourceProvider} translates model objects into information that is
 * required for displaying the corresponding object to a user.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResourceProvider extends LabelProvider {
    
	/**
	 * Translates the given model object to a description of its type.
	 * 
	 * The result is displayed as an alternative text for images that represent
	 * the given model object.
	 * 
	 * @param anObject 
	 *     The model object.
	 * @return a description of the type of the given model object.
	 */
	public String getType(Object anObject);
	
	/**
	 * Get a tooltip text for the given object.
	 * 
	 * @return the tooltip for the given object. <code>null</code> denotes
	 *         that no tooltip is available.
	 */
	public String getTooltip(Object anObject);

    /**
	 * Translate the given model object into an image path (relative to the theme).
	 * 
	 * @param anObject
	 *        The model object
	 * @param aFlavor
	 *        The image {@link Flavor} to request.
	 * @return An image that represents the given model object.
	 */
    public ThemeImage getImage(Object anObject, Flavor aFlavor);
    
    /**
	 * Translate the given model object into a link that navigates to the given
	 * object.
	 * 
	 * @param anObject
	 *     The model object
	 * @return An absolute link URL that navigates to the given model object (or
	 *     <code>null</code>, if there is no URL for the given object).
	 */
    public String getLink(DisplayContext context, Object anObject);

	/**
	 * A CSS class to use for displaying the given object.
	 */
	public String getCssClass(Object anObject);

}
