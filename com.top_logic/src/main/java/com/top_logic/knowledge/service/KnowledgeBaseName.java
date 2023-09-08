/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Configuration of a {@link KnowledgeBase}.
 * 
 * <p>
 * Configurations which depend on the {@link KnowledgeBase} should extend this interface to be able
 * to configure the name of the {@link KnowledgeBase} to use.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KnowledgeBaseName extends ConfigurationItem {

	/** Configuration name of {@link #getKnowledgeBase()}. */
	String KNOWLEDGE_BASE = "knowledge-base";

	/**
	 * The name of the {@link KnowledgeBase} to use.
	 * 
	 * <p>
	 * The name of the {@link KnowledgeBase} must be known by the {@link KnowledgeBaseFactory}.
	 * </p>
	 * 
	 * @see KnowledgeBaseFactory
	 */
	@Name(KNOWLEDGE_BASE)
	@StringDefault(PersistencyLayer.DEFAULT_KNOWLEDGE_BASE_NAME)
	String getKnowledgeBase();

	/**
	 * Setter for {@link #getKnowledgeBase()}.
	 * 
	 * @see #getKnowledgeBase()
	 */
	void setKnowledgeBase(String kbName);

}
