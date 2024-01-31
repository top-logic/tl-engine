/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;

/**
 * MBean to monitor data of the database.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MonitorDatabase implements MonitorDatabaseMBean, NamedMonitor {

	@Override
	public String getName() {
		return "com.top_logic.monitoring.data:name=MonitorDatabase";
	}

	@Override
	public int getSizeOfKnowledgebaseCache() {
		KnowledgeBaseFactory kbFactory = KnowledgeBaseFactory.getInstance();
		for (String kbName : kbFactory.getKnowledgeBaseNames()) {
			KnowledgeBase kb = kbFactory.getKnowledgeBase(kbName);
			return kb.getCacheSize();
		}

		return 0;
	}

}
