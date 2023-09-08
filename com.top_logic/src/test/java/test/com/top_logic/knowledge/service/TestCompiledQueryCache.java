/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.service.CompiledQueryCache;

/**
 * Test for {@link CompiledQueryCache}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestCompiledQueryCache extends AbstractDBKnowledgeBaseTest {

	private CompiledQueryCache _queryCache;

	private NamedConstant _key = new NamedConstant("key");

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queryCache = kb().getQueryCache();
	}

	public void testCache() {
		assertNull(_queryCache.getQuery(_key));
		CompiledQuery<KnowledgeObject> compileQuery = newQuery();
		assertEquals(compileQuery, _queryCache.storeQuery(_key, compileQuery));
		assertNotNull(_queryCache.getQuery(_key));
		assertEquals(compileQuery, _queryCache.removeQuery(_key));
	}

	private CompiledQuery<KnowledgeObject> newQuery() {
		return kb().compileQuery(queryUnresolved(allOf(B_NAME)));
	}

	public void testConcurrent() {
		assertNull(_queryCache.getQuery(_key));
		CompiledQuery<KnowledgeObject> compileQuery = newQuery();
		assertEquals(compileQuery, _queryCache.storeQuery(_key, compileQuery));
		assertSame("Cache entries are not overridden", compileQuery, _queryCache.storeQuery(_key, newQuery()));
		_queryCache.removeQuery(_key);
		assertNotEquals("Former cache entry not removed.", compileQuery, _queryCache.storeQuery(_key, newQuery()));
	}

	public void testIllegal() {
		try {
			_queryCache.storeQuery("key", null);
			fail("Storing null is not allowed");
		} catch (Exception ex) {
			// storing null is not allowed
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestCompiledQueryCache}.
	 */
	public static Test suite() {
		return suite(TestCompiledQueryCache.class);
	}

}

