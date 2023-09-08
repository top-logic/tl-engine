/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.event;

import java.util.EventListener;

import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;

/**
 * Listener that checks whether the commit must be blocked or not.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommitChecker extends EventListener {

	/**
	 * Checks whether the commit which will produce given {@link UpdateEvent} must be blocked orr
	 * not.
	 * 
	 * @param event
	 *        The event later send to the {@link UpdateListener}. Contains information about the
	 *        changes which are done.
	 * 
	 * @throws CommitVetoException
	 *         iff the commit must be blocked.
	 */
	void checkCommit(UpdateEvent event) throws CommitVetoException;

}

