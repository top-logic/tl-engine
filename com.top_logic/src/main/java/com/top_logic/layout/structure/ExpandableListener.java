/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.layout.structure.Expandable.ExpansionState;

/**
 * {@link PropertyListener} that notifies about a changing {@link Expandable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExpandableListener extends PropertyListener {

	/**
	 * Notifies about a change in {@link Expandable#getExpansionState()} of the given sender.
	 * 
	 * @param sender
	 *        The modified {@link Expandable}.
	 * @param oldValue
	 *        The old {@link ExpansionState}.
	 * @param newValue
	 *        The new {@link ExpansionState}.
	 * @return Whether to stop bubbling the event to potential parents.
	 */
	Bubble notifyExpansionStateChanged(Expandable sender, ExpansionState oldValue, ExpansionState newValue);

}
