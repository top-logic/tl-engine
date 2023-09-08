/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.merge;

import junit.framework.Test;
import junit.textui.TestRunner;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.merge.IndexingTask;
import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.basic.Logger;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Address;

/**
 * Testcase for the {@link com.top_logic.base.merge.CommitTask}.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class TestIndexingTask extends BasicTestCase {

    /**
     * Test perform() using the source Object
     */
    public void testPerformSource() throws Exception {
        KnowledgeBase theBase    = KBSetup.getKnowledgeBase();
        Address       theAddress = Address.createAddress(theBase);
        IndexingTask  it = new IndexingTask();
        MergeTreeNode aNode = new RootMergeNode(theAddress);
        it.perform(aNode); 
    }

    /**
     * Test perform() using the destination Object
     */
    public void testPerformDest() throws Exception {
        Address       theAddress = Address.createAddress("Street", "City", "00000", "GERMANY");
        IndexingTask  it = new IndexingTask();
        MergeTreeNode aNode = new RootMergeNode(null, theAddress);
        try {
            it.perform(aNode);
        } catch (IndexException expected ) { /* since Indexing service is not available */ }
    }

    /**
     * Return the suite of test to execute.
     */
    public static Test suite () {
		return KBSetup.getSingleKBTest(TestIndexingTask.class);
    }
    
    /**
     * Main method to start this test case.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        
        KBSetup.setCreateTables(false); // do not really need a KBase here
        
        Logger.configureStdout();
        TestRunner.run (suite ());
    }
}
