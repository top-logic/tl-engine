/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.service.db2.IDQueryCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.service.db2.SimpleQueryCache;

/**
 * Tests {@link IDQueryCache}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSimpleQueryCacheByID extends TestSimpleQueryCache {

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSimpleQueryCacheByID}.
	 */
	public static Test suite() {
		return suite(TestSimpleQueryCacheByID.class);
	}

	@Override
	protected SimpleQueryCache<KnowledgeObject> newQueryCache(SimpleQuery<KnowledgeObject> simpleQuery) {
		return new IDQueryCache<>(kb(), simpleQuery, ExpressionFactory.revisionArgs());
	}

}

