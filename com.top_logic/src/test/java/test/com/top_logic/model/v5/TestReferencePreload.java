/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.v5;

import junit.framework.Test;
import test.com.top_logic.KBTestUtils;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.v5.ReferencePreload;

/**
 * Test for {@link ReferencePreload}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferencePreload extends AbstractDBKnowledgeBaseClusterTest {

	public void testReferencePreload() throws DataObjectException {
		Transaction refereeTx = begin();
		commit(refereeTx);
		String reference = UNTYPED_POLY_CUR_GLOBAL_NAME;

		Transaction referenceTx = begin();
		KnowledgeObject g1 = newG("g1");
		KnowledgeObject g2 = newG("g2");
		KnowledgeObject b4 = newB("b4");
		KnowledgeObject b5 = newB("b5");
		g1.setAttributeValue(reference, b4);
		g2.setAttributeValue(reference, b5);

		KnowledgeObject g3 = newG("g3");
		// must also work when no reference is set.
		KnowledgeObject g4 = newG("g3");
		KnowledgeObject b6 = newB("b6");
		g3.setAttributeValue(reference, b6);
		commit(referenceTx);
		
		// refetch to be able to access referees
		refetchNode2();
		KBTestUtils.clearCache(kbNode2());

		KnowledgeItem g1Node2 = node2Item(g1);
		KnowledgeItem g2Node2 = node2Item(g2);
		ObjectKey b4Node2Key = node2Key(b4);
		ObjectKey b5Node2Key = node2Key(b5);
		assertNull("Test sensless if item already loaded.", kbNode2().resolveCachedObjectKey(b4Node2Key));
		assertNull("Test sensless if item already loaded.", kbNode2().resolveCachedObjectKey(b5Node2Key));

		KnowledgeItem g3Node2 = node2Item(g3);
		KnowledgeItem g4Node2 = node2Item(g4);
		ObjectKey b6Node2Key = node2Key(b6);
		assertNull("Test sensless if item already loaded.", kbNode2().resolveCachedObjectKey(b6Node2Key));

		ReferencePreload preload = new ReferencePreload(G_NAME, reference);
		PreloadContext ctx1 = new PreloadContext();
		try {
			preload.prepare(ctx1, list(toWrapper(g1Node2), toWrapper(g2Node2)));
			assertNotNull("Item is referenced and should be loaded.", kbNode2().resolveCachedObjectKey(b4Node2Key));
			assertNotNull("Item is referenced and should be loaded.", kbNode2().resolveCachedObjectKey(b5Node2Key));
			assertNull("Item is not referenced from base objects.", kbNode2().resolveCachedObjectKey(b6Node2Key));
			// check loading twice is no problem
			preload.prepare(ctx1, list(toWrapper(g1Node2), toWrapper(g2Node2)));
		} finally {
			ctx1.close();
		}

		PreloadContext ctx2 = new PreloadContext();
		try {
			preload.prepare(ctx2, list(toWrapper(g3Node2), toWrapper(g4Node2)));
			assertNotNull("Item is referenced and should be loaded.", kbNode2().resolveCachedObjectKey(b6Node2Key));
		} finally {
			ctx2.close();
		}

	}

	private static Wrapper toWrapper(KnowledgeItem item) {
		// check here to avoid cast in each call
		assertInstanceof(item, KnowledgeObject.class);
		return WrapperFactory.getWrapper((KnowledgeObject) item);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestReferencePreload}.
	 */
	public static Test suite() {
		return suite(TestReferencePreload.class);
	}

}
