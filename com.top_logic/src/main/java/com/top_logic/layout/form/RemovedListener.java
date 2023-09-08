/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;

/**
 * {@link PropertyListener} that handles removal of a {@link FormMember} from its parent.
 * 
 * @see FormMember#REMOVED_FROM_PARENT
 * @see AddedListener
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface RemovedListener extends PropertyListener {

	/**
	 * Handles removal of the given sender from its parent.
	 * 
	 * @param sender
	 *        The member removed from its parent.
	 * @param formerParent
	 *        The old parent.
	 * @return Whether this event shall bubble.
	 */
	Bubble handleRemovedFromParent(FormMember sender, FormContainer formerParent);

}

