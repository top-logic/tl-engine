/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DisableListener {
	
	/**
	 * <b>NOTE: THIS METHOD IS FRAMEWORK INTERNAL AND MUST NOT BE CALLED BY THE
	 * APPLICATION</b>
	 * 
	 * Informs the view that is has to disable its view. That is a form of
	 * client side disabling and does not affect the disabled state of a model,
	 * the view eventually has.
	 * 
	 * @param disabled
	 *        whether the view should be disabled or not
	 */
	void notifyDisabled(boolean disabled);

}

