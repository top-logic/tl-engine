/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.Set;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.basic.SingleTestFactory;
import test.com.top_logic.basic.io.binary.TestFileBasedBinaryData;
import test.com.top_logic.knowledge.wrap.TestFlexWrapperCluster;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.FlexVersionedDataManager;

/**
 * Test case for {@link FlexVersionedDataManager}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFlexVersionedDataManager extends AbstractFlexDataManagerTest {

	@Override
	protected FlexDataManager createFlexDataManager() {
		return new FlexVersionedDataManager(
			kb().getConnectionPool(),
			kb().lookupType(AbstractFlexDataManager.FLEX_DATA));
	}
	
    @Override
    protected boolean supportsBinaryData() {
        return true;
    }

	@Override
	protected KnowledgeObject newAObject(String a1) throws DataObjectException {
		// create versioned object
		return newB(a1);
	}

	@Override
	protected KnowledgeObject newDObject(String a1) throws DataObjectException {
		// create versioned object
		return newD(a1);
	}

	/**
	 * Test mapping of Unicode characters varies between Databases with unexpected results.
	 */
    public void testUnicodeCharacters() throws DataObjectException {
    	final KnowledgeObject b1;
		final FlexData data;
    	final String uc1;
    	final String uc2;
    	final Revision r2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			data = loadData(b1, true);
			
			StringBuffer unicode = new StringBuffer(254);
			unicode.append(TestFlexWrapperCluster.randomString(8, false, true, false, false));
			unicode.append(TestFlexWrapperCluster.randomString(253 - 8, false, false, true, false));
			
			uc1 = unicode.toString();
			unicode.append(unicode.append(TestFlexWrapperCluster.randomString(32, true, true, true, false)));
			uc2 = unicode.toString();
			assert uc1.length() < 254;
			assert uc2.length() > 255;
			
			data.setAttributeValue("attr1", uc1);
			data.setAttributeValue("attr2", uc2);
			
			storeData(b1, data);
			commit(tx);
			r2 = tx.getCommitRevision();
		}
        
		FlexData persistentData2 = loadData(HistoryUtils.getKnowledgeItem(r2, b1), false);
        assertNotNull(persistentData2);
        assertEquals(uc1, persistentData2.getAttributeValue("attr1"));
        assertEquals(uc2, persistentData2.getAttributeValue("attr2"));
        assertNull(persistentData2.getAttributeValue("attr3"));
    }
    
	public void testHistoricData() throws DataObjectException {
		final KnowledgeObject b1;
		final FlexData data;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			data = loadData(b1, true);
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr2", "value2");
			data.setAttributeValue("attrB", LONG_BINARY_DATA);
			storeData(b1, data);
			commit(tx);
		}
        
        // Keep the "current" Binary Data for later check
		final FlexData data2 = loadData(HistoryUtils.getKnowledgeItem(Revision.CURRENT, b1), false);
		final Object currentB2 = data2.getAttributeValue("attrB");

        Revision r2 = HistoryUtils.getLastRevision();
		
		{
			Transaction tx = begin();
			data.setAttributeValue("attr1", "newValue1");
			data.setAttributeValue("attr2", null);
			data.setAttributeValue("attr3", "value3");
			data.setAttributeValue("attrB", null);
			storeData(b1, data);
			commit(tx);
		}
        
		final FlexData data3 = loadData(HistoryUtils.getKnowledgeItem(Revision.CURRENT, b1), false);
        Object currentB3 = data3.getAttributeValue("attrB");

        Revision r3 = HistoryUtils.getLastRevision();
		
		{
			Transaction tx = begin();
			data.setAttributeValue("attr1", "newValue1x");
			data.setAttributeValue("attr2", "restoredValue2");
			data.setAttributeValue("attr3", "newValue3");
			data.setAttributeValue("attrB", SMALL_BINARY_DATA);
			storeData(b1, data);
			commit(tx);
		}
		
		final FlexData data4 = loadData(HistoryUtils.getKnowledgeItem(Revision.CURRENT, b1), false);
	    Object currentB4 = data4.getAttributeValue("attrB");

		Revision r4 = HistoryUtils.getLastRevision();
		
		FlexData persistentData2 = loadData(HistoryUtils.getKnowledgeItem(r2, b1), false);
		assertNotNull(persistentData2);
		assertEquals("value1", persistentData2.getAttributeValue("attr1"));
		assertEquals("value2", persistentData2.getAttributeValue("attr2"));
        assertEquals(LONG_BINARY_DATA, persistentData2.getAttributeValue("attrB"));
		assertNull(persistentData2.getAttributeValue("attr3"));
        TestFileBasedBinaryData.cleanupFiles();
        assertEquals(LONG_BINARY_DATA, persistentData2.getAttributeValue("attrB"));

		FlexData persistentData3 = loadData(HistoryUtils.getKnowledgeItem(r3, b1), false);
		assertNotNull(persistentData3);
		assertEquals("newValue1", persistentData3.getAttributeValue("attr1"));
		assertNull(persistentData3.getAttributeValue("attr2"));
        assertNull(persistentData3.getAttributeValue("attrB"));
		assertEquals("value3", persistentData3.getAttributeValue("attr3"));
		
		FlexData persistentData4 = loadData(HistoryUtils.getKnowledgeItem(r4, b1), false);
		assertNotNull(persistentData4);
		assertEquals("newValue1x"     , persistentData4.getAttributeValue("attr1"));
		assertEquals("restoredValue2" , persistentData4.getAttributeValue("attr2"));
		assertEquals("newValue3"      , persistentData4.getAttributeValue("attr3"));
        assertEquals(SMALL_BINARY_DATA, persistentData4.getAttributeValue("attrB"));
        
        assertEquals(LONG_BINARY_DATA , currentB2);
        assertNull  (currentB3);
        assertEquals(SMALL_BINARY_DATA, currentB4);

        // Break implementation of Binary data to reveal re-fetch problem
        TestFileBasedBinaryData.cleanupFiles();

        assertEquals(SMALL_BINARY_DATA, currentB4);
        assertNull  (                   currentB3);
        assertEquals(LONG_BINARY_DATA,  currentB2); // TODO BHU/KHA re-fetch correct revision
	}
	
	/**
	 * Check fetching Current/Historic {@link BinaryData} objects.
	 */
    public void testCurrentDataStability() throws DataObjectException {
    	final KnowledgeObject b1;
		final FlexData data;
		{
			// Setup with LONG_BINARY_DATA (which may break on refetch)
			Transaction tx = begin();
			b1 = newB("b1");
			data = loadData(b1, true);
			data.setAttributeValue("attrB", LONG_BINARY_DATA);
			storeData(b1, data);
			commit(tx);
		}
        
        // Keep the "current" Binary Data for later check
		final FlexData data2 = loadData(HistoryUtils.getKnowledgeItem(Revision.CURRENT, b1), false);
        final Object currentB2 = data2.getAttributeValue("attrB");
        
        // assertEquals(LONG_BINARY_DATA, currentB2); This would fetch the correct version, here  

        Revision r2 = HistoryUtils.getLastRevision();
        
		{
			// Clear binary Attribute
			Transaction tx = begin();
			data.setAttributeValue("attrB", null);
			storeData(b1, data);
			commit(tx);
		}
        
		final FlexData data3 = loadData(HistoryUtils.getKnowledgeItem(Revision.CURRENT, b1), false);
		assertEquals("Single attribute deleted.", 0, data3.getAttributes().size());
        // Object currentB3 = data3.getAttributeValue("attrB");

        Revision r3 = HistoryUtils.getLastRevision();
        
		{
			// Setup with SMALL_BINARY_DATA (which is not re-fetched but kept in memory)
			Transaction tx = begin();
			data.setAttributeValue("attrB", SMALL_BINARY_DATA);
			storeData(b1, data);
			commit(tx);
		}
        
		final FlexData data4 = loadData(HistoryUtils.getKnowledgeItem(Revision.CURRENT, b1), false);
        Object currentB4 = data4.getAttributeValue("attrB");

        Revision r4 = HistoryUtils.getLastRevision();
        
		final FlexData persistentData2 = loadData(HistoryUtils.getKnowledgeItem(r2, b1), false);
        assertNotNull(persistentData2);
        final Object historicB2 = persistentData2.getAttributeValue("attrB");
        assertEquals(LONG_BINARY_DATA, historicB2);
        assertEquals(LONG_BINARY_DATA, currentB2); 

        TestFileBasedBinaryData.cleanupFiles(); // simulate timeout 
        
        assertEquals(LONG_BINARY_DATA, persistentData2.getAttributeValue("attrB"));
        assertEquals(LONG_BINARY_DATA, historicB2);
        assertEquals(LONG_BINARY_DATA, currentB2);  // Check for correct,historic revision

		final FlexData persistentData3 = loadData(HistoryUtils.getKnowledgeItem(r3, b1), false);
		assertEquals("Single attribute deleted.", 0, persistentData3.getAttributes().size());
        // assertNull(persistentData3.getAttributeValue("attrB"));
        // assertNull(currentB3);
        
		final FlexData persistentData4 = loadData(HistoryUtils.getKnowledgeItem(r4, b1), false);
        assertNotNull(persistentData4);
        final Object historicB4 = persistentData4.getAttributeValue("attrB");
        assertEquals(SMALL_BINARY_DATA, historicB4);
        assertEquals(SMALL_BINARY_DATA, currentB4);
        
        TestFileBasedBinaryData.cleanupFiles(); // simulate timeout 
        assertEquals(SMALL_BINARY_DATA, historicB4);
        assertEquals(SMALL_BINARY_DATA, persistentData4.getAttributeValue("attrB"));
        assertEquals(SMALL_BINARY_DATA, currentB4);
    }

    public void testImmutableHistoricData() throws DataObjectException {
    	final KnowledgeObject b1;
		final FlexData data;
    	final Revision r2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			data = loadData(b1, true);
			data.setAttributeValue("attr1", "value1");
			data.setAttributeValue("attr2", "value2");
			storeData(b1, data);
			commit(tx);
			r2 = tx.getCommitRevision();
		}

		FlexData persistentData2 = loadData(HistoryUtils.getKnowledgeItem(r2, b1), false);
		assertNotNull(persistentData2);

		try {
			persistentData2.setAttributeValue("attr1", "illegal update");
			fail("Succeeded updating historic data.");
		} catch (IllegalStateException ex) {
			// Expected, historic data cannot be modified.
		}
	}

	/**
	 * This method tests
	 * {@link AbstractFlexDataManager#branch(PooledConnection, long, long, long, long, Collection)}
	 * via {@link HistoryUtils#createBranch(Branch, Revision, Set)}
	 */
	public void testBranching() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		String value1 = "value1";
		String attr1 = "attr1";
		
		final KnowledgeObject b1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			final FlexData data = loadData(b1, true);
			data.setAttributeValue(attr1, value1);
			storeData(b1, data);
			commit(tx);
		}

		Branch branch2 =
			HistoryUtils.createBranch(HistoryUtils.getContextBranch(), HistoryUtils.getLastRevision(), types(B_NAME));

		KnowledgeObject b1InBranch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b1);
		
		final FlexData newData = loadData(b1InBranch2, false);
		assertNotNull("Branched KnowledgeObject '" + b1InBranch2 + "' has no attributes", newData);
		assertTrue("Branched KnowledgeObject '" + b1InBranch2 + "' expected to has attributes '" + attr1 + "' with value '" + value1 + "' but was"
				+ newData.getAttributeValue(attr1), value1.equals(newData.getAttributeValue(attr1)));
	}

	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			String testName = "testLoadAllWithDifferentObjects";
			return suite(TestFlexVersionedDataManager.class, DBType.MYSQL_DB, new SingleTestFactory(testName));
		}
		return suite(TestFlexVersionedDataManager.class);
    }
    
    /** Main function for Debugging / single Testing */
    public static void main(String args[]) {
        Logger.configureStdout();
        TestRunner runner = new TestRunner(); // new DetailedResultPrinter(System.out));
        runner.doRun(suite ());
    }

}
