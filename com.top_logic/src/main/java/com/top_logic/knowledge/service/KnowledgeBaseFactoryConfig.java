/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import java.util.Map;

import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;

/**
 * Configuration of the {@link KnowledgeBaseFactory}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KnowledgeBaseFactoryConfig extends ServiceConfiguration<KnowledgeBaseFactory> {

	/**
	 * Configurations of all known {@link KnowledgeBase} indexed by name.
	 */
	@Key(KnowledgeBaseConfiguration.NAME_ATTRIBUTE)
	Map<String, KnowledgeBaseConfiguration> getKnowledgeBases();

}

