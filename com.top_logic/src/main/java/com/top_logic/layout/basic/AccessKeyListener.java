/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * Listener to handle changes of {@link ButtonUIModel#getAccessKey()}.
 * 
 * @see ButtonUIModel#ACCESS_KEY_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface AccessKeyListener extends PropertyListener {

	/**
	 * Handles the change of {@link ButtonUIModel#getAccessKey()}.
	 * 
	 * @param sender
	 *        The model whose access key changed.
	 * @param oldKey
	 *        Old access key.
	 * @param newKey
	 *        New Access key.
	 * @return Whether this event shall bubble.
	 * 
	 * @see ButtonUIModel#getAccessKey()
	 */
	Bubble handleAccessKeyChanged(ButtonUIModel sender, Character oldKey, Character newKey);
}

