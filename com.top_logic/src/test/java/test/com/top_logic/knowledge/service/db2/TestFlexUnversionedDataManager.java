/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.io.binary.TestFileBasedBinaryData;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;
import com.top_logic.knowledge.service.db2.FlexData;
import com.top_logic.knowledge.service.db2.FlexVersionedDataManager;

/**
 * Test case for {@link FlexVersionedDataManager} for unversioned types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFlexUnversionedDataManager extends AbstractFlexDataManagerTest {

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
		// create unversioned object
		return newU(a1);
	}

	@Override
	protected KnowledgeObject newDObject(String a1) throws DataObjectException {
		// create unversioned object
		return newH(a1);
	}

    /**
     * Check fetching Current/Historic {@link BinaryData} objects.
     * 
     * Makes no real sense for an unversionend access, but the versionend implementation
     * may break this.
     *   
     */
    public void testCurrentDataStability() throws DataObjectException {
		final KnowledgeObject u1;
		final FlexData data;
		{
			Transaction tx = begin();
			// Setup with LONG_BINARY_DATA (which may break on refetch)
			u1 = newU("u1");
			data = loadData(u1, true);
			data.setAttributeValue("attrB", LONG_BINARY_DATA );
			storeData(u1, data);
			commit(tx);
		}
        
        // Keep the "current" Binary Data for later check
		final FlexData data2 = loadData(u1, false);
        final Object currentB2 = data2.getAttributeValue("attrB");
        
        assertEquals(LONG_BINARY_DATA, currentB2); // This will fetch the correct version, here  

        Revision r2 = HistoryUtils.getLastRevision();
        
		{
			// Clear binary Attribute
			Transaction tx = begin();
			data.setAttributeValue("attrB", null);
			storeData(u1, data);
			commit(tx);
		}
        
		final FlexData data3 = loadData(u1, false);
		assertEquals("Single attribute deleted.", 0, data3.getAttributes().size());
        // Object currentB3 = data3.getAttributeValue("attrB");

        Revision r3 = HistoryUtils.getLastRevision();
        
		{
			// Setup with SMALL_BINARY_DATA (which is not re-fetched but kept in memory)
			Transaction tx = begin();
			data.setAttributeValue("attrB", SMALL_BINARY_DATA);
			storeData(u1, data);
			commit(tx);
		}
        
		final FlexData data4 = loadData(u1, false);
        Object currentB4 = data4.getAttributeValue("attrB");

        Revision r4 = HistoryUtils.getLastRevision();
        
		assertNull(HistoryUtils.getKnowledgeItem(r2, u1)); // Unversioned, does not support
															// Revisions

        assertEquals(LONG_BINARY_DATA, currentB2);   
        TestFileBasedBinaryData.cleanupFiles();      // simulate timeout 
        // Will not actually work
        // assertEquals(SMALL_BINARY_DATA , currentB2);  // Should re-fetch current, well

		assertNull(HistoryUtils.getKnowledgeItem(r3, u1)); // Unversioned, does not support
															// Revisions
        
		assertNull(HistoryUtils.getKnowledgeItem(r4, u1)); // Unversioned, does not support
															// Revisions
        
        assertEquals(SMALL_BINARY_DATA, currentB4);
    }

    public static Test suite() {
        // return suite(new TestFlexUnversionedDataManager("testCurrentDataStability"), DBType.MYSQL_DB);
        return suite(TestFlexUnversionedDataManager.class);
    }
    
    /** Main function for Debugging / single Testing */
    public static void main(String args[]) {
        Logger.configureStdout();
        TestRunner runner = new TestRunner(); // new DetailedResultPrinter(System.out));
        runner.doRun(suite ());
    }

}
