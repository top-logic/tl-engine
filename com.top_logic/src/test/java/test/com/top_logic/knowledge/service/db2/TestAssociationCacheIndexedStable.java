/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Map.Entry;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;

/**
 * Test case for {@link IndexedLinkQuery} with stable result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationCacheIndexedStable extends TestAssociationCacheIndexed {

	static final IndexedLinkQuery<String, EObj> INDEXED_REFERERS = IndexedLinkQuery.indexedLinkQuery(
		new NamedConstant("indexedReferers"), EObj.class, E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, A1_NAME, String.class);

	public void testIndexedLinkCacheConcurrency() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");

		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		e3.setPolyCurLocal(d1);
		tx1.commit();

		assertIndex(d1, e1, e2, e3);

		Transaction tx2 = begin();
		for (String key : resolveIndex(d1).keySet()) {
			final EObj e = EObj.newEObj(key + "x");
			e.setPolyCurLocal(d1);
		}
		tx2.commit();

		assertEquals(set("e1", "e2", "e3", "e1x", "e2x", "e3x"), resolveIndex(d1).keySet());

		Transaction tx3 = begin();
		for (Entry<String, EObj> entry : resolveIndex(d1).entrySet()) {
			if (entry.getKey().endsWith("x")) {
				entry.getValue().tDelete();
			}
		}
		tx3.commit();

		assertEquals(set("e1", "e2", "e3"), resolveIndex(d1).keySet());
	}

	@Override
	protected IndexedLinkQuery<String, EObj> query() {
		return INDEXED_REFERERS;
	}

	public static Test suite() {
		return suite(TestAssociationCacheIndexedStable.class);
	}

}
