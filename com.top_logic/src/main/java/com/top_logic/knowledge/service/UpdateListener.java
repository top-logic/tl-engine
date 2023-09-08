/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

/**
 * Listener interface for synchronous batched {@link KnowledgeBase} updates.
 * 
 * <p>
 * This listener is informed about local and remote updates to a
 * {@link KnowledgeBase} immediately after a commit or revision refetch
 * completes.
 * </p>
 * 
 * @see KnowledgeBase#getUpdateChain() Asynchronous pull-style mechanism for
 *      receiving the same update events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UpdateListener {

	/**
	 * The {@link Revision} with the given revision number has either been
	 * locally committed or remotely created and refetched by the given
	 * {@link KnowledgeBase}.
	 * 
	 * @param sender
	 *        The {@link KnowledgeBase} that was updated.
	 * @param event
	 *        A collection of changes that occurred.
	 */
	void notifyUpdate(KnowledgeBase sender, UpdateEvent event);

}
