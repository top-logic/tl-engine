/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles visibility change of an object.
 * 
 * @see LayoutComponent#VISIBILITY_EVENT
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface VisibilityListener extends PropertyListener {

	/**
	 * Handles change of the visibility of the given object.
	 * 
	 * @param sender
	 *        Object whose visibility changed.
	 * @param oldVisibility
	 *        Former visibility.
	 * @param newVisibility
	 *        Current visibility.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility);

}