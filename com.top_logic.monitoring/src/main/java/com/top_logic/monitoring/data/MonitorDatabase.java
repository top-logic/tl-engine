/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * MBean to monitor data of the database.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class MonitorDatabase extends AbstractConfiguredInstance<MonitorDatabase.Config>
		implements MonitorDatabaseMBean, NamedMonitor {

	/** {@link ConfigurationItem} for the {@link MonitorDatabase}. */
	public interface Config extends MBeanConfiguration<MonitorDatabase> {

		@Override
		@StringDefault("com.top_logic.monitoring.data:name=MonitorDatabase")
		public String getName();
	}

	/** {@link TypedConfiguration} constructor for {@link MonitorDatabase}. */
	public MonitorDatabase(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public int getSizeOfKnowledgebaseCache() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		return kb.getCacheSize();
	}

	@Override
	public String getLastRevision() {
		return ThreadContextManager.inSystemInteraction(MonitorDatabase.class, this::calcLastRevision);
	}

	private String calcLastRevision() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		return HistoryUtils.getLastRevision(kb.getHistoryManager()).toString();
	}

}
