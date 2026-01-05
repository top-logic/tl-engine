/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.indexing.vector;

import java.util.Collection;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.DataSetup;
import test.com.top_logic.knowledge.VectorSearchTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.vector.VectorIndex;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Test case for the {@link com.top_logic.knowledge.indexing.vector.VectorIndex}
 *
 * @author Dieter Rothbächer
 */
public class TestVectorIndex extends BasicTestCase {
    
    /**
	 * Constructor for TestVectorIndex.
	 */
	public TestVectorIndex(String name) {
        super(name);
    }
    
    /** Testing getting reader and writer */
	public void testReaderWriter() {
		/**
		 * Empty
		 */
    }
    
    /** Test indexing many files */
    public void testIndexing() throws Exception {
        this.startTime();

		VectorIndex theIndex = VectorIndex.getInstance();

        // this should work
        int numberOfIndexedElements = 0;
        for (KnowledgeObject ko : DataSetup.getKnowledgeObjects()) {
            theIndex.addContent(DefaultIndexingService.createContent(ko));
            numberOfIndexedElements++;
        }
        this.logTime("indexing");
 
        int maxNumberWait = 300; // number of times to wait (took ~200 seconds)
        int waitTime = 1000; // in milliseconds
        do {
			Logger.debug("Queue size: " + theIndex.queueSizes(), TestVectorIndex.class);
			Thread.sleep(waitTime);
        }
        while ((theIndex.queueSizes()) > 0 && (maxNumberWait--) > 0);
		if (maxNumberWait <= 0) {
			Object vectorThread = ReflectionUtils.getValue(theIndex, "myThread");
			
			Object addQueue = ReflectionUtils.getValue(vectorThread, "addQueue");
			assertInstanceof(addQueue, Collection.class);
			
			Object deleteQueue = ReflectionUtils.getValue(vectorThread, "deleteQueue");
			assertInstanceof(deleteQueue, Collection.class);
			
			fail("No Progress in testIndexing? There are still " + ((Collection<?>) addQueue).size() + " objects to index and " + ((Collection<?>) deleteQueue).size() +" objects to delete! (Indexed: " + numberOfIndexedElements + ")");
		}
    }

    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		TestSuite theSuite = new TestSuite(TestVectorIndex.class);
         
        ActiveTestSuite parSuite = new ActiveTestSuite();
		parSuite.addTest(TestUtils.tryEnrichTestnames(new TestVectorIndex("testIndexing"), "ActiveTestSuite 1"));
		parSuite.addTest(TestUtils.tryEnrichTestnames(new TestVectorIndex("testIndexing"), "ActiveTestSuite 2"));
		parSuite.addTest(TestUtils.tryEnrichTestnames(new TestVectorIndex("testIndexing"), "ActiveTestSuite 3"));
        theSuite.addTest(parSuite);

		return VectorSearchTestSetup.createSearchTestSetup(DataSetup.createDataSetup(theSuite, "."));
    }
    
}
