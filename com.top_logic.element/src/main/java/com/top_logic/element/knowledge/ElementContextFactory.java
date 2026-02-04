/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.knowledge;

import com.top_logic.knowledge.service.db2.DBContext;
import com.top_logic.knowledge.service.db2.DBContextFactory;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * {@link DBContextFactory} creating {@link ElementDBContext}.
 */
public class ElementContextFactory extends DBContextFactory {

	@Override
	public DBContext createContext(DBKnowledgeBase kb, String updater) {
		return new ElementDBContext(kb, updater);
	}

}

