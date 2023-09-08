/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;
import test.com.top_logic.knowledge.wrap.TestFlexWrapperCluster;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MappedIterable;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * {@link TestCase} for consistent refetch of association caches in a cluster environment.
 * 
 * @see KnowledgeBase#resolveLinks(KnowledgeObject, AbstractAssociationQuery)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestKABasedCacheCluster extends AbstractDBKnowledgeBaseClusterTest {

	/**
	 * Test that object can be created in one KB and retrieved (with their associations) in the
	 * other KB.
	 */
	public void testSetup() {
		BObj b;
		CObj c;
		{
			Transaction tx = begin();

			b = BObj.newBObj("b");
			c = CObj.newCObj("c");
			b.addAB(c);

			tx.commit();
		}

		// refetch to be able to resolve object
		refetchNode2();
		BObj b_node2 = (BObj) WrapperFactory.getWrapper((KnowledgeObject) node2Item(b.tHandle()));

		assertNotNull(b_node2);
		assertNotSame(b, b_node2);

		assertEquals(Wrapper.IDENTIFIER_MAPPING, b.getAB(), b_node2.getAB());
		assertNotEquals(b.getAB(), b_node2.getAB());
	}

	/**
	 * Like {@link KBUtils#transformKey(MORepository, ObjectKey)} but ensures that a new key is
	 * created.
	 * 
	 * <p>
	 * During test, {@link KnowledgeBase}s share their type instances. {@link KBUtils} would return
	 * the same key. Resolving would result in the same object, even in a foreign KB.
	 * </p>
	 */
	private static ObjectKey copyKey(MORepository moRepository, ObjectKey key) throws UnknownTypeException {
		MetaObject type = moRepository.getMetaObject(key.getObjectType().getName());
		return KBUtils.createObjectKey(key.getBranchContext(), key.getHistoryContext(), type, key.getObjectName());
	}

	public void testPreload() {
		Map<Object, Set<Object>> targetNamesBySrcName = new HashMap<>();
		{
			Transaction tx = begin();

			// Number of caches to test bulk-loading with. Must be greater than 1000 and should not
			// be a multiple of 1000. Where 1000 is the limit for values in an SQL "in" statement.
			int srcCnt = 1479;

			int targetCnt = 10;
			BObj[] targets = new BObj[targetCnt];
			for (int n = 0; n < targetCnt; n++) {
				targets[n] = CObj.newCObj("c" + n);
			}
			Random rnd = new Random(42);
			for (int n = 0; n < srcCnt; n++) {
				BObj b = BObj.newBObj("b" + n);

				int linkCnt = rnd.nextInt(20);
				Set<Object> targetNames = new HashSet<>();
				targetNamesBySrcName.put(b.tHandle().getObjectName(), targetNames);
				for (int k = 0; k < linkCnt; k++) {
					BObj target = targets[rnd.nextInt(targetCnt)];
					b.addAB(target);
					targetNames.add(target.tHandle().getObjectName());
				}
			}

			commit(tx);
		}

		// Test that check method works as expected (in the KB where the objects are created).
		List<BObj> bs =
			WrapperFactory.getWrappersForKOs(BObj.class, kb().getAllKnowledgeObjects(B_NAME));

		StopWatch timeOrig = new StopWatch().start();
		checkCaches(targetNamesBySrcName, bs);
		timeOrig.stop();

		// refetch to be able to resolve object
		refetchNode2();
		// Test that the results after preloading is OK, too.
		List<BObj> bs_node2 =
			WrapperFactory.getWrappersForKOs(BObj.class,
				kbNode2().getAllKnowledgeObjects(B_NAME));

		StopWatch timeCached = new StopWatch().start();
		kbNode2().fillCaches(new MappedIterable<>(Wrapper.KO_MAPPING, bs_node2),
			AssociationQuery.createOutgoingQuery("B.ab", AB_NAME));

		checkCaches(targetNamesBySrcName, bs_node2);
		timeCached.stop();

		Logger.info("Speedup: " + ((double) timeOrig.getElapsed()) / timeCached.getElapsed(),
			TestKABasedCacheCluster.class);
	}

	private void checkCaches(Map<Object, Set<Object>> targetNamesBySrcName, List<BObj> bs) {
		for (BObj b : bs) {
			Set targets = b.getAB();
			Set<Object> targetNames = targetNamesBySrcName.get(b.tHandle().getObjectName());

			assertNotNull("Unexpected B '" + b + "'.", targetNames);
			for (Iterator it = targets.iterator(); it.hasNext(); ) {
				CObj target = (CObj) it.next();
				
				assertTrue(targetNames.contains(target.tHandle().getObjectName()));
			}

			assertEquals("Unexpected numbers of AB's for " + b + ".", targetNames.size(), targets.size());
		}
	}

	public void testRefetchDestinationCache() {
		BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			assertEquals(0, b1.getAB().size());
			commit(tx);
		}
		
		assertEquals(0, b1.getAB().size());
		
		// Test the same on node 2.
		refetchNode2();
		BObj b1Node2 = getBObjNode2(b1);
		assertNotNull(b1Node2);
		
		// Trigger initialization of cache.
		assertEquals(0, b1Node2.getAB().size());
		
		BObj b2;
		BObj b3;
		{
			Transaction tx = begin();
			// Create new objects on node 1.
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			
			b1.addAB(b2);
			b1.addAB(b3);
			
			assertEquals(set(b2, b3), b1.getAB());
			
			commit(tx);
		}

		assertEquals(set(b2, b3), b1.getAB());

		refetchNode2();
		assertEquals(2, b1Node2.getAB().size());
		
		BObj b2Node2 = getBObjNode2(b2);
		BObj b3Node2 = getBObjNode2(b3);
		
		assertEquals(set(b2Node2, b3Node2), b1Node2.getAB());
	}

	protected BObj getBObjNode2(BObj b1) {
		return TestFlexWrapperCluster.getBObjNode2(b1);
	}

	
    public static Test suite() {
		if (false) {
			return runOneTest(TestKABasedCacheCluster.class, "testPreload");
		}
        return suite(TestKABasedCacheCluster.class);
    }
	
}
