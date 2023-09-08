/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;


import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseClusterTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl;
import test.com.top_logic.knowledge.service.db2.expr.visit.TestQueryLanguage;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;

import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * Test case for {@link AbstractBoundWrapper} in cluster environments.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFlexWrapperCluster extends AbstractDBKnowledgeBaseClusterTest {

	static class TestingAttributeLoader implements
			AttributeLoader<SimpleWrapperFactoryTestScenario.BObj> {

		LinkedHashMap<BObj, FlexData> loaded =
			new LinkedHashMap<>();

		@Override
		public void loadData(long dataRevision, BObj baseObject, FlexData data) {
			loaded.put(baseObject, data);
		}

		@Override
		public void loadEmpty(long dataRevision, BObj baseObject) {
			loaded.put(baseObject, null);
		}
	}

	public void testPreloadMixed() {
		BObj b1;
		BObj c1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj(kb(), "b1");
			b1.setValue("flexValue", "a1");
			c1 = CObj.newCObj(kb(), "c1");
			c1.setValue("flexValue", "a2");
			commit(tx);
		}

		FlexDataManager flexDataManager = b1.tHandle().getFlexDataManager();
		assertSame(flexDataManager, c1.tHandle().getFlexDataManager());

		TestingAttributeLoader callback = new TestingAttributeLoader();
		flexDataManager.loadAll(callback, Wrapper.KEY_MAPPING, list(b1, c1), kb());

		assertTrue(callback.loaded.containsKey(b1));
		assertTrue(callback.loaded.containsKey(c1));

		FlexData b1Values = callback.loaded.get(b1);
		assertNotNull("Ticket #10976: Incomplete mixed preload.", b1Values);
		assertEquals("Ticket #10976: Incomplete mixed preload.", "a1", b1Values.getAttributeValue("flexValue"));

		FlexData c1Values = callback.loaded.get(c1);
		assertNotNull("Ticket #10976: Incomplete mixed preload.", c1Values);
		assertEquals("Ticket #10976: Incomplete mixed preload.", "a2", c1Values.getAttributeValue("flexValue"));
	}

	public void testPreloadLastInList() {
		BObj b1;
		BObj b2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj(kb(), "b1");
			b1.setValue("flexValue", "a1");
			b2 = BObj.newBObj(kb(), "b2");
			b2.setValue("flexValue", "a2");
			commit(tx);
		}

		FlexDataManager flexDataManager = b1.tHandle().getFlexDataManager();
		assertSame(flexDataManager, b2.tHandle().getFlexDataManager());
		TestingAttributeLoader callback = new TestingAttributeLoader();
		flexDataManager.loadAll(callback, Wrapper.KEY_MAPPING, list(b1, b2), kb());
		assertTrue(callback.loaded.containsKey(b2));
		FlexData loadedValues = callback.loaded.get(b2);
		assertNotNull("Ticket #7976: last element in list was loaded empty.", loadedValues);
	}

	/**
	 * Tests that preloading an empty list does not break down.
	 */
	public void testEmptyPreload() {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj(kb(), "b1");
		tx.commit();
		TestingAttributeLoader callback = new TestingAttributeLoader();
		b1.tHandle().getFlexDataManager()
			.loadAll(callback, Wrapper.KEY_MAPPING, Collections.<BObj> emptyList(), kb());
		assertEquals(Collections.emptySet(), callback.loaded.keySet());
	}

	/**
	 * Tests the preload with more than 1000 elements. (#8738)
	 */
	public void testLargePreload() {
		List<BObj> bs = new ArrayList<>();
		BObj b1;
		int numberObjects = 1100;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj(kb(), "referenceObject");
			for (int i = 0; i < numberObjects; i++) {
				BObj b = BObj.newBObj(kb(), "b1");
				b.setValue("flexValue", String.valueOf(i));
				bs.add(b);
			}
			commit(tx);
		}

		TestingAttributeLoader callback = new TestingAttributeLoader();
		b1.tHandle().getFlexDataManager().loadAll(callback, Wrapper.KEY_MAPPING, bs, kb());
		assertEquals(toSet(bs), callback.loaded.keySet());
	}

	public void testCheckChangeHistory() {
		final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			commit(tx);
		}
		
		final Revision r2;
		{
			Transaction tx = begin();
			b1.setF1("f1");
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			b1.setF1("f1 new");
			commit(tx);
		}
		
		final BObj b1r2 = (BObj) WrapperHistoryUtils.getWrapper(r2, b1);
		assertEquals("f1", b1r2.getF1());
		
		{
			Transaction tx = begin();
			try {
				b1r2.setF2("f2 illegal");
				fail("Must not allow modification of stable object.");
			} catch (IllegalStateException ex) {
				// Expected.
			}
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					assertNull(b1r2.getF2());
				}
			});
			
			commit(tx);
			assertNull(b1r2.getF2());
		}

		assertEquals("f1 new", b1.getF1());
		assertNull(b1.getF2());

		// refetch to get current content
		refetchNode2();

		BObj b1Node2 = getBObjNode2(b1);
		assertEquals("f1 new", b1Node2.getF1());
		assertNull(b1Node2.getF2());
	}
	
	public void testStringFlexAttributeRange() {
		final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			commit(tx);
		}
		
		refetchNode2();
		BObj b1Node2 = getBObjNode2(b1);
		
		MOKnowledgeItemImpl dataType =
			(MOKnowledgeItemImpl) kb().getMORepository().getMetaObject(AbstractFlexDataManager.FLEX_DATA);
		MOAttributeImpl varcharAttr =
			AbstractFlexDataManager.getAttribute(dataType, AbstractFlexDataManager.VARCHAR_DATA);

		doTestStringAttr(b1, b1Node2, BObj.F1_NAME, varcharAttr.getSQLSize() - 1);
		doTestStringAttr(b1, b1Node2, BObj.F1_NAME, varcharAttr.getSQLSize());
		doTestStringAttr(b1, b1Node2, BObj.F1_NAME, varcharAttr.getSQLSize() + 1);
	}

	public void testStringRowAttributeRange() {
		final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			commit(tx);
		}
		
		refetchNode2();
		BObj b1Node2 = getBObjNode2(b1);
		
		doTestStringAttr(b1, b1Node2, B1_NAME, KnowledgeBaseTestScenarioImpl.B1.getSQLSize() - 1);
		doTestStringAttr(b1, b1Node2, B1_NAME, KnowledgeBaseTestScenarioImpl.B1.getSQLSize());
		doTestStringAttr(b1, b1Node2, B1_NAME, KnowledgeBaseTestScenarioImpl.B1.getSQLSize() + 1, true);
	}

	private void doTestStringAttr(final BObj b1, final BObj b1Node2, String attributeName, int size) {
		doTestStringAttr(b1, b1Node2, attributeName, size, false);
	}
	
	private void doTestStringAttr(final BObj b1, final BObj b1Node2, String attributeName, int size, boolean expectFailure) {
		doTestStringAttr(b1, b1Node2, attributeName, size, expectFailure, true, false, false, false);
		doTestStringAttr(b1, b1Node2, attributeName, size, expectFailure, true, true, false, false);
		doTestStringAttr(b1, b1Node2, attributeName, size, expectFailure, true, true, true, false);
		doTestStringAttr(b1, b1Node2, attributeName, size, expectFailure, false, false, false, true);
	}

	private void doTestStringAttr(final BObj b1, final BObj b1Node2,
			String attributeName, int size, boolean expectFailure,
			boolean ascii, boolean latin, boolean bmp, boolean supplementary) {
		
		try {
			if (supplementary && (! kb().getConnectionPool().getSQLDialect().supportsUnicodeSupplementaryCharacters())) {
				// Skip test.
				return;
			}
		} catch (SQLException ex) {
			// Skip test.
			return;
		}
		
		String value = randomString(size, ascii, latin, bmp, supplementary);
		
		{
			Transaction tx = begin();
			b1.setValue(attributeName, value);
			commit(tx, (supplementary ? "Surrogate" : (bmp ? "Base multilingual plant" : (latin ? "Latin" : "ASCII"))) + " character storage test", expectFailure);
		}
		
		if (! expectFailure) {
			refetchNode2();
			assertEquals(value, b1Node2.getValue(attributeName));
		}
	}
	
	/** 
	 * @see TestQueryLanguage#testNullComparision()
	 */
	public void testSearch() {
		SetExpression expr =
			filter(allOf(B_NAME), eqBinary(flex(MOPrimitive.STRING, BObj.F1_NAME), attribute(B_NAME, B1_NAME)));
		
		final BObj b1;
		final BObj b2;
		final BObj b3;
		final BObj b4;
		{
			Transaction tx = begin();
			// attribute b1 and f1 are both null
			b1 = BObj.newBObj("b1");
			
			b2 = BObj.newBObj("b2");
			b2.setB1("v1");
			b2.setF1("v1");
			
			b3 = BObj.newBObj("b3");
			b3.setB1("v1");
			
			b4 = BObj.newBObj("b4");
			b4.setF1("v1");
			commit(tx);
		}
		
		List<KnowledgeObject> koResult = kb().search(queryUnresolved(expr));
		List<Wrapper> result = WrapperFactory.getWrappersForKOsGeneric(koResult);
		
		assertEquals(set(b1, b2), toSet(result));
	}
	
    public void testDeleteWithLocalModificationsWithoutPersistentData() throws Exception {
    	final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			b1.setF1("f1");
			b1.tDelete();
			commit(tx);
		}
    }

    public void testDeleteWithLocalModificationsWithPersistentData() throws Exception {
    	final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			commit(tx);
		}
    	
		{
			Transaction tx = begin();
			b1.setF1("f1");
			commit(tx);
		}
    	
		{
			Transaction tx = begin();
			b1.setF2("f2");
			b1.tDelete();
			commit(tx);
		}
    }
    
    public void testChangeCheckFlex() {
    	final BObj b1;
    	final Revision r2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setF1("f1");
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			b1.setF1("f1");
			commit(tx);
			assertNull("Ticket #884: Revision was created without actual changes.", tx.getCommitRevision());
		}
		final Revision r2again = HistoryUtils.getLastRevision();
		
		assertEquals("Ticket #884: Revision was created without actual changes.", r2, r2again);
	}
	
	public void testChangeCheckKO() {
		final BObj b1;
		final Revision r2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setA2("a2");
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			b1.setA2("a2");
			commit(tx);
			assertNull("Ticket #884: Revision was created without actual changes.", tx.getCommitRevision());
		}
		final Revision r2again = HistoryUtils.getLastRevision();
		
		assertEquals("Ticket #884: Revision was created without actual changes.", r2, r2again);
	}
	
	public void testReadRetry() throws Throwable {
		final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setF1("f1");
			commit(tx);
		}
		
		// refetch to be able to resolve object
		refetchNode2();

		DBKnowledgeBaseClusterTestSetup.simulateConnectionBreakdownNode2();
		
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				BObj b1Node2 = getBObjNode2(b1);
				assertEquals("f1", b1Node2.getF1());
			}
		});
	}
	
	public void testRollback() {
		final BObj b1;
		final BObj b2;
		final BObj b3;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setA1("hello");
			b1.setF1("world");
			
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			b1.addAB(b2);
			
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			b1.addAB(b3);
			b1.setA1("xxx");
			b1.setF1("yyy");
			
			assertEquals(set(b2, b3), b1.getAB());
			assertEquals("xxx", b1.getA1());
			assertEquals("yyy", b1.getF1());
			
			kb().addCommittable(COMMIT_FAILURE);
			commit(tx, true);
		}
		
		assertEquals(set(b2), b1.getAB());
		assertEquals("hello", b1.getA1());
		assertEquals("world", b1.getF1());
	}
	
	public void testCreation() {
		final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			assertNull("Uncommitted object not yet visible to second node.", getBObjNode2(b1));
			commit(tx);
		}
		
		{
			Transaction tx = begin();
			b1.setA2("a2");
			b1.setF1("f1");
			b1.setF2("f2");
			commit(tx);
		}

		// refetch to be able to resolve object
		refetchNode2();

		BObj b1Node2 = getBObjNode2(b1);
		assertNotNull(b1Node2);
		
		assertEquals("b1", b1Node2.getA1());
		assertEquals("a2", b1Node2.getA2());
		assertEquals("f1", b1Node2.getF1());
		assertEquals("f2", b1Node2.getF2());
	}
	
	public class SimpleSetup {
		public BObj b1;

		public BObj b1Node2;

		public SimpleSetup() {
			{
				Transaction tx = begin();
				b1 = BObj.newBObj("b1");
				b1.setF1("f1");
				commit(tx);
			}

			// refetch to be able to resolve object
			refetchNode2();

			b1Node2 = getBObjNode2(b1);
			assertNotNull(b1Node2);
		}
	}
	
	public void testKOModificationRefetch() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setA1("a1update");
			setup.b1.setA2("a2");
			
			commit(tx);
		}
		refetchNode2();
		
		assertEquals("a1update", setup.b1Node2.getA1());
		assertEquals("a2", setup.b1Node2.getA2());
	}

	public void testFlexCreationRefetch() {
		SimpleSetup setup = new SimpleSetup();
		
		// Make sure, flex data has been loaded.
		assertEquals("f1", setup.b1Node2.getF1());
		
		{
			Transaction tx = begin();
			setup.b1.setF2("f2");
			commit(tx);
		}
		refetchNode2();
		
		assertEquals("f2", setup.b1Node2.getF2());
	}
	
	/** Oracle maps empty Strings to null */
    public void testNullEmptyConfusion() {
    	final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setF1("");
			commit(tx);
		}

		refetchNode2();
        BObj b2 = getBObjNode2(b1);
        
        assertEquals("", b2.getF1());
        
		{
			Transaction tx = begin();
			b1.setF1(null);
			commit(tx);
		}
        refetchNode2();
        b2 = getBObjNode2(b1);
        assertEquals(null, b2.getF1());

		{
			Transaction tx = begin();
			b1.setF1("");
			commit(tx);
		}
        refetchNode2();
        b2 = getBObjNode2(b1);
        assertEquals("", b2.getF1());
    }

    public void testFlexModificationRefetch() {
		SimpleSetup setup = new SimpleSetup();

		// Make sure, flex data has been loaded.
		assertEquals("f1", setup.b1Node2.getF1());
		
		{
			Transaction tx = begin();
			setup.b1.setF1("f1update");
			commit(tx);
		}
		refetchNode2();
		
		assertEquals("f1update", setup.b1Node2.getF1());
	}
	
	public void testFlexDeletionRefetch() {
		SimpleSetup setup = new SimpleSetup();
		
		// Make sure, flex data has been loaded.
		assertEquals("f1", setup.b1Node2.getF1());
		
		{
			Transaction tx = begin();
			setup.b1.setF1(null);
			commit(tx);
		}
		refetchNode2();
		
		assertEquals(null, setup.b1Node2.getF1());
	}
	
	public void testDetectLostUpdateAddConcurrent() {
		SimpleSetup setup = new SimpleSetup();

		{
			Transaction tx1 = begin();
			Transaction tx2 = beginNode2();
			
			// Pseudo concurrently modify.
			setup.b1.setF2("f2node1");
			setup.b1Node2.setF2("f2node2");
			
			// Pseudo concurrently commit.
			commit(tx1);
			commitNode2Fail(tx2);
		}
		
		
		assertEquals("f2node1", setup.b1Node2.getF2());
	}
	
	public void testDetectLostUpdateDeleteModify() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx1 = begin();
			Transaction tx2 = beginNode2();
			
			// Pseudo concurrently delete and modify.
			setup.b1.tDelete();
			setup.b1Node2.setA2("f2node2");
			
			// Pseudo concurrently commit.
			commit(tx1);
			commitNode2Fail(tx2);
		}
		
		assertFalse(setup.b1.tValid());
		assertFalse(setup.b1Node2.tValid());
	}
	
	public void testDetectLostUpdateModifyDelete() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx1 = begin();
			Transaction tx2 = beginNode2();
			
			// Pseudo concurrently delete and modify.
			setup.b1.setA2("f2node2");
			setup.b1Node2.tDelete();
			
			// Pseudo concurrently commit.
			commit(tx1);
			commitNode2Fail(tx2);
		}
		
		assertTrue(setup.b1.tValid());
		assertTrue(setup.b1Node2.tValid());
	}
	
	public void testDetectLostUpdateFlexKO() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setF2("f2node1");
			commit(tx);
		}
		
		{
			Transaction tx = beginNode2();
			setup.b1Node2.setA2("a2node2");
			commitNode2Fail(tx);
		}
		
		assertEquals(null, setup.b1Node2.getA2());
	}
	
	public void testDetectLostUpdateKOFlex() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setA2("a2node1");
			commit(tx);
		}
		
		setup.b1Node2.setF2("f2node2");
		assertFalse(kbNode2().commit());
		
		assertEquals(null, setup.b1Node2.getF2());
	}
	
	public void testDetectLostUpdateFlexAdd() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setF2("f2node1");
			commit(tx);
		}
		
		setup.b1Node2.setF2("f2node2");
		assertFalse(kbNode2().commit());
		
		assertEquals("f2node1", setup.b1Node2.getF2());
	}
	
	public void testDetectLostUpdateFlexModify() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setF1("f1node1");
			commit(tx);
		}
		
		setup.b1Node2.setF1("f1node2");
		assertFalse(kbNode2().commit());
		
		assertEquals("f1node1", setup.b1Node2.getF1());
	}
	
	public void testDetectLostUpdateFlexDelete() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setF1(null);
			commit(tx);
		}
		
		setup.b1Node2.setF1(null);
		
		assertFalse(kbNode2().commit());
		
		assertEquals(null, setup.b1Node2.getF1());
	}
	
	public void testDetectLostUpdateFlexModifyDelete() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setF1("f1node1");
			commit(tx);
		}
		
		setup.b1Node2.setF1(null);
		assertFalse(kbNode2().commit());
		
		assertEquals("f1node1", setup.b1Node2.getF1());
	}
	
	public void testDetectLostUpdateFlexDeleteModify() {
		SimpleSetup setup = new SimpleSetup();
		
		{
			Transaction tx = begin();
			setup.b1.setF1(null);
			commit(tx);
		}
		
		setup.b1Node2.setF1("f1node1");
		assertFalse(kbNode2().commit());
		
		assertEquals(null, setup.b1Node2.getF1());
	}
	
    public void testBranching() throws Exception {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
    	final BObj b1;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b1.setA1("hello_b");
			b1.setF1("world_b");
			commit(tx);
		}
		
		Branch branch =
			HistoryUtils.createBranch(HistoryUtils.getContextBranch(), HistoryUtils.getLastRevision(), types(B_NAME));
		BObj b2 = (BObj) WrapperHistoryUtils.getWrapper(branch, HistoryUtils.getLastRevision(), b1);
		
		assertEquals("hello_b", b2.getA1());
		assertEquals("world_b", b2.getF1());
    }

	public static BObj getBObjNode2(BObj b1) {
		Wrapper result =
			WrapperFactory.getWrapper(KBUtils.getWrappedObjectName(b1), b1.tTable().getName(), kbNode2());
		return (BObj) result;
	}

	public static Test suite() {
		if (false) {
			return runOneTest(TestFlexWrapperCluster.class, "testLargePreload", DBType.MYSQL_DB);
		}
		return suite(TestFlexWrapperCluster.class);
	}
}
