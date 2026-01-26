/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;

/**
 * Factory creating {@link DBContext} for {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class DBContextFactory {

	/**
	 * Creates a new {@link DBContext} for the given {@link KnowledgeBase} and the given updater.
	 * 
	 * @param kb
	 *        {@link KnowledgeBase} initiating the change.
	 * @param updater
	 *        Name identifying the person that triggers the change
	 * 
	 * @see Revision#getAuthor()
	 */
	public DBContext createContext(DBKnowledgeBase kb, String updater) {
		return new DefaultDBContext(kb, updater);
	}

}

