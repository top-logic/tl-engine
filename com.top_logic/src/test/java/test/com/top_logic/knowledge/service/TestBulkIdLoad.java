/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.Set;

import junit.framework.Test;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test for {@link BulkIdLoad}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBulkIdLoad extends AbstractDBKnowledgeBaseClusterTest {

	public void testLoad() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject b2 = newB("b2");
		commit(tx);

		BulkIdLoad loader = new BulkIdLoad(kb());
		loader.add(b1.tId());
		loader.add(b2.tId());
		assertEquals(set(b1, b2), toSet(loader.load()));

		refetchNode2();
		BulkIdLoad node2loader = new BulkIdLoad(kbNode2());
		node2loader.add(node1Key(b1));
		node2loader.add(node2Key(b2));

		/* Attention: do not inline variable to ensure loader loads the objects, and not the
		 * creation of expectation. */
		Set<KnowledgeItem> loadResult = toSet(node2loader.load());
		assertEquals(set(node2Item(b1), node2Item(b2)), loadResult);
	}

	public void testClear() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject b2 = newB("b2");
		commit(tx);

		refetchNode2();
		BulkIdLoad node2loader = new BulkIdLoad(kbNode2());
		node2loader.add(node1Key(b1));
		node2loader.add(node2Key(b2));
		node2loader.clear();
		assertEmpty("Loader has been cleared", false, node2loader.load());

		node2loader.add(node1Key(b1));
		node2loader.add(node2Key(b2));
		/* Attention: do not inline variable to ensure loader loads the objects, and not the
		 * creation of expectation. */
		Set<KnowledgeItem> loadResult = toSet(node2loader.load());
		assertEquals(set(node2Item(b1), node2Item(b2)), loadResult);

		node2loader.clear();
		assertEmpty("Loader has been cleared", false, node2loader.load());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestBulkIdLoad}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestBulkIdLoad.class, "testClear");
		}
		return suite(TestBulkIdLoad.class);
	}

}

