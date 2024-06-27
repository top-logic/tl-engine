/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import javax.management.MBeanAttributeInfo;

import com.top_logic.basic.CalledByReflection;
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
public class KnowledgeBaseMBean extends AbstractDynamicMBean {

	/** {@link ConfigurationItem} for the {@link KnowledgeBaseMBean}. */
	public interface Config extends AbstractDynamicMBean.Config {

		@Override
		@StringDefault("com.top_logic.monitoring.data:name=KnowledgeBaseMBean")
		public String getName();
	}

	/** {@link TypedConfiguration} constructor for {@link KnowledgeBaseMBean}. */
	public KnowledgeBaseMBean(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected MBeanAttributeInfo[] createAttributeInfo() {
		MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[2];

		attributes[0] = new MBeanAttributeInfo(
			"SizeOfKnowledgebaseCache", // name
			"java.lang.Integer", // type
			"The size of the knowledge-base cache.", // description
			true, // readable
			false, // writable
			false); // isIs

		attributes[1] = new MBeanAttributeInfo(
			"LastRevision", // name
			"java.lang.String", // type
			"The last revision of the knowledge-base.", // description
			true, // readable
			false, // writable
			false); // isIs

		return attributes;
	}

	/** Returns the size of the used knowledge-base cache. */
	@CalledByReflection
	public int getSizeOfKnowledgebaseCache() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		return kb.getCacheSize();
	}

	/** Returns the last revision of the knowledge-base. */
	@CalledByReflection
	public Long getLastRevision() {
		return ThreadContextManager.inSystemInteraction(KnowledgeBaseMBean.class, this::calcLastRevision);
	}

	private Long calcLastRevision() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		return HistoryUtils.getLastRevision(kb.getHistoryManager()).getCommitNumber();
	}

}
