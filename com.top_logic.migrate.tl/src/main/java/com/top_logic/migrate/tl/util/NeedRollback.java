/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import com.top_logic.knowledge.event.KnowledgeEvent;

/**
 * {@link NeedRollback} is an interface which can be used by processors of {@link KnowledgeEvent} if
 * some rollback must be done during migration.
 * 
 * The class {@link NeedRollback} can be implemented by {@link LazyEventRewriter} given in
 * {@link MigrateParameters}. It is filled once before the first event is written.
 * 
 * @see Rollback
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface NeedRollback {

	/**
	 * Sets the callback which can be used to rollback all actions done since the last database
	 * commit.
	 * 
	 * @param rollback
	 *        not <code>null</code>. is set once before migration starts
	 */
	public void setRollback(Rollback rollback);
}
