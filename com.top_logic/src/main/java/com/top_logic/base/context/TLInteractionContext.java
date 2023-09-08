/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.context;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.util.TopLogicServlet;

/**
 * Special {@link InteractionContext} with methods not available in basic.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TLInteractionContext extends InteractionContext {

	/**
	 * Developer information to be handed over to logging mechanism in {@link TopLogicServlet}.
	 * 
	 * @return The debugging information for the developer.
	 */
	ProcessingInfo getProcessingInfo();

	@Override
	TLSessionContext getSessionContext();

	@Override
	TLSubSessionContext getSubSessionContext();

	/**
	 * Returns the revision of the current interaction.
	 * 
	 * <p>
	 * Must not be called by the application. Use {@link HistoryManager#getSessionRevision()}
	 * instead.
	 * </p>
	 * 
	 * @param historyManager
	 *        The {@link HistoryManager} which is used to access with the returned revision.
	 * 
	 * @see HistoryManager#getSessionRevision()
	 */
	@FrameworkInternal
	long getInteractionRevision(HistoryManager historyManager);

	/**
	 * Updates the revision of the current interaction for the given {@link HistoryManager}.
	 * 
	 * <p>
	 * Must not be called by the application.
	 * </p>
	 * 
	 * @param historyManager
	 *        The {@link HistoryManager} to update revision for.
	 * @param newRevision
	 *        The new interaction revision.
	 * @return The former stored {@link UpdateChainLink}. May be <code>null</code>.
	 */
	@FrameworkInternal
	long updateInteractionRevision(HistoryManager historyManager, long newRevision);

}

