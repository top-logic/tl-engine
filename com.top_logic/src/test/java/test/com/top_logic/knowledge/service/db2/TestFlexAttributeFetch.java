/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.RefetchTimeout;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.FlexAttributeFetch;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.export.PreloadContext;

/**
 * Tests {@link FlexAttributeFetch}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestFlexAttributeFetch extends AbstractDBKnowledgeBaseClusterTest {

	public void testDuplicateLoadingFlexAttribute() throws RefetchTimeout {
		String flexValue = "flexAttributeValue";
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		b1.setF2(flexValue);
		BObj b2 = BObj.newBObj("b1");
		b2.setF2(flexValue);
		tx.commit();

		kbNode2().refetch();
		BObj b1Node2 = (BObj) WrapperFactory.getWrapper((KnowledgeObject) node2Item(b1.tHandle()));
		BObj b2Node2 = (BObj) WrapperFactory.getWrapper((KnowledgeObject) node2Item(b2.tHandle()));
		try (PreloadContext context = new PreloadContext()) {
			FlexAttributeFetch.INSTANCE.prepare(context, list(b1Node2, b1Node2, b2Node2, b2Node2));
		}

		assertEquals(flexValue, b1Node2.getF2());
		assertEquals(flexValue, b2Node2.getF2());
	}

	/**
	 * Test that bulk loading of flex values for historic objects loads the correct values.
	 * 
	 * @see "Ticket #27228"
	 */
	public void testBulkLoadHistoricValues() throws RefetchTimeout {
		String flexValue = "flexAttributeValue";
		String newFlexValue = "newFlexAttributeValue";

		int numberObjects = 10;
		List<BObj> cache = new ArrayList<>();

		Transaction tx1 = begin();
		for (int i = 0; i < numberObjects; i++) {
			BObj b1 = BObj.newBObj("b1");
			b1.setF2(flexValue);
			cache.add(b1);
		}
		tx1.commit();

		Transaction tx2 = begin();
		for (int i = 0; i < numberObjects; i++) {
			cache.get(i).setF2(newFlexValue);
		}
		tx2.commit();

		kbNode2().refetch();
		for (int i = 0; i < numberObjects; i++) {
			ObjectKey node2Key = node2Key(cache.get(i).tHandle());
			ObjectKey historicKey =
				KBUtils.createHistoricObjectKey(node2Key, tx1.getCommitRevision().getCommitNumber());
			cache.set(i, kbNode2().resolveObjectKey(historicKey).getWrapper());
		}

		try (PreloadContext context = new PreloadContext()) {
			FlexAttributeFetch.INSTANCE.prepare(context, cache);
		}

		for (int i = 0; i < numberObjects; i++) {
			assertEquals(flexValue, cache.get(i).getF2());
		}

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestFlexAttributeFetch}.
	 */
	public static Test suite() {
		return AbstractDBKnowledgeBaseTest.suite(TestFlexAttributeFetch.class);
	}

}
