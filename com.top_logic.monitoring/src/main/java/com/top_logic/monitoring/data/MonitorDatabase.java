/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;

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
public class MonitorDatabase extends DynamicMBeanElement {

	/** {@link ConfigurationItem} for the {@link MonitorDatabase}. */
	public interface Config extends DynamicMBeanElement.Config {

		@Override
		@StringDefault("com.top_logic.monitoring.data:name=MonitorDatabase")
		public String getName();
	}

	/** {@link TypedConfiguration} constructor for {@link MonitorDatabase}. */
	public MonitorDatabase(InstantiationContext context, Config config) {
		super(context, config);

		buildDynamicMBeanInfo(config);
	}

	@Override
	protected MBeanConstructorInfo[] createConstructorInfo() {
		return null;
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[2];

		dAttributes[0] = new MBeanAttributeInfo(
			"SizeOfKnowledgebaseCache", // name
			"java.lang.Integer", // type
			"The size of the used knowledge-base cache.", // description
			true, // readable
			false, // writable
			false); // isIs

		dAttributes[1] = new MBeanAttributeInfo(
			"LastRevision", // name
			"java.lang.String", // type
			"The last revision of the knowledge-base.", // description
			true, // readable
			false, // writable
			false); // isIs

		return dAttributes;
	}

	@Override
	protected MBeanOperationInfo[] createOperationInfo() {
		return null;
	}

	/** Returns the size of the used knowledge-base cache. */
	public int getSizeOfKnowledgebaseCache() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		return kb.getCacheSize();
	}

	/** Returns the last revision of the knowledge-base. */
	public String getLastRevision() {
		return ThreadContextManager.inSystemInteraction(MonitorDatabase.class, this::calcLastRevision);
	}

	private String calcLastRevision() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		return HistoryUtils.getLastRevision(kb.getHistoryManager()).toString();
	}

}
