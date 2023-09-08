/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.util.ResKey;

/**
 * {@link PropertyListener} handling changes in executable state and reason key of a
 * {@link ButtonUIModel}.
 * 
 * @see ButtonUIModel#EXECUTABLE_PROPERTY
 * @see ButtonUIModel#NOT_EXECUTABLE_REASON_PROPERTY
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ExecutableListener extends PropertyListener {

	/**
	 * Handles change of the {@link ButtonUIModel#isExecutable() executable state} of the given
	 * {@link ButtonUIModel}.
	 * 
	 * @param sender
	 *        The {@link ButtonUIModel} whose execution state changed.
	 * @param oldExecutability
	 *        Whether the given model was executable before.
	 * @param newExecutability
	 *        Whether the given model is now executable.
	 * @return Whether this event shall bubble.
	 * 
	 * @see ButtonUIModel#isExecutable()
	 */
	Bubble handleExecutableChange(ButtonUIModel sender, Boolean oldExecutability, Boolean newExecutability);

	/**
	 * Handles change of the {@link ButtonUIModel#getNotExecutableReasonKey()} of the given
	 * {@link ButtonUIModel}.
	 * 
	 * @param sender
	 *        The {@link ButtonUIModel} whose reason key changed.
	 * @param oldReason
	 *        Former reason.
	 * @param newReason
	 *        Current reason.
	 * @return Whether this event shall bubble.
	 * 
	 * @see ButtonUIModel#getNotExecutableReasonKey()
	 */
	Bubble handleNotExecutableReasonChange(ButtonUIModel sender, ResKey oldReason, ResKey newReason);

}

