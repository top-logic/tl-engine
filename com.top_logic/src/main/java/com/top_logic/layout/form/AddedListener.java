/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles adding a {@link FormMember} to its parent.
 * 
 * @see FormMember#ADDED_TO_PARENT
 * @see RemovedListener
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface AddedListener extends PropertyListener {

	/**
	 * Handles adding the given sender to its parent.
	 * 
	 * @param sender
	 *        The member removed from its parent.
	 * @param newParent
	 *        The new parent.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleAddedToParent(FormMember sender, FormContainer newParent);

}

