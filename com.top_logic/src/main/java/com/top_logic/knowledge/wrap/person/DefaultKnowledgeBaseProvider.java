/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import com.top_logic.basic.col.Provider;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * {@link Provider} of the default {@link KnowledgeBase}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DefaultKnowledgeBaseProvider implements Provider<KnowledgeBase> {

	public static final DefaultKnowledgeBaseProvider INSTANCE = new DefaultKnowledgeBaseProvider();

	private DefaultKnowledgeBaseProvider() {
		// Singleton instance
	}

	@Override
	public KnowledgeBase get() {
		return PersistencyLayer.getKnowledgeBase();
	}

}