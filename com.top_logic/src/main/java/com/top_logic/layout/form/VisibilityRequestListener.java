/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles the request to make the given {@link FormMember} visible.
 * 
 * @see FormMember#VISIBILITY_REQUEST
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface VisibilityRequestListener extends PropertyListener {

	/**
	 * Handles the request to make the given {@link FormMember} visible.
	 * 
	 * @param sender
	 *        The {@link FormMember} to make visible.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleVisibilityRequested(FormMember sender);

}

