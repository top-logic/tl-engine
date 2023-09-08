/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ajax.client.service;

import com.top_logic.ajax.client.control.JSControl;

/**
 * Factory for client-side controls that create the GWT control and links it to the DOM.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface JSControlFactory {

	/**
	 * Creates a {@link JSControl}, initializes it with the given arguments and links it to the
	 * client-side DOM element with the given ID.
	 *
	 * @param controlId
	 *        The ID of the DOM element to link the created control to.
	 * @return The created {@link JSControl}.
	 */
	JSControl createControl(String controlId);

}
