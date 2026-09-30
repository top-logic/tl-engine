/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.xml.TagUtil;

/**
 * Algorithm for computing a tooltip for an application object.
 */
public interface TooltipProvider {

	/**
	 * Computes a tooltip of the given object.
	 * 
	 * @param object
	 *        The application object, for which a tooltip should be computed.
	 * @return The tooltip HTML to display for the given object. Note: For compatibility reasons,
	 *         the returned value is displayed as raw unsafe HTML. All values embedded into this
	 *         result must be safely quoted. See {@link TagUtil#encodeXML(String)}.
	 */
	String getTooltip(Object object);

}
