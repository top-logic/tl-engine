/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Map;

import junit.framework.Test;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseConfiguration;

/**
 * {@link DBKnowledgeBaseTestSetup} that creates a {@link KnowledgeBase} which is optimized for
 * single node applications.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SingleNodeOptimizedTestSetup extends DBKnowledgeBaseTestSetup {

	/**
	 * Creates a new {@link SingleNodeOptimizedTestSetup}.
	 * 
	 * @param test
	 *        The actual executed test.
	 */
	public SingleNodeOptimizedTestSetup(Test test) {
		super(test);
	}

	@Override
	protected Map<String, String> createKBConfig(String name, String connectionPool) {
		Map<String, String> config = super.createKBConfig(name, connectionPool);
		config.put(KnowledgeBaseConfiguration.SINGLE_NODE_OPTIMIZATION_PROPERTY, Boolean.toString(true));
		return config;
	}

}

