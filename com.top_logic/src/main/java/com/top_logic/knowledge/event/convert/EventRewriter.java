/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event.convert;

import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;

/**
 * Rewriter of events.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface EventRewriter {

	/**
	 * Processes the given {@link ChangeSet}. Each result of the processing must be delivered to the
	 * given {@link EventWriter}. If the event shall be deleted nothing must be delivered.
	 * 
	 * @param cs
	 *        The event to rewrite.
	 * @param out
	 *        the {@link EventWriter} to deliver all created or changed events to.
	 */
	void rewrite(ChangeSet cs, EventWriter out);
}