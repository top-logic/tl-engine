/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.indexing.lucene;

import java.io.IOException;
import java.util.Collection;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.DataSetup;
import test.com.top_logic.knowledge.LuceneSearchTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Test case for the {@link com.top_logic.knowledge.indexing.lucene.LuceneIndex}
 *
 * @author    Dieter Rothbächer
 */
public class TestLuceneIndex extends BasicTestCase {
    
    /**
     * Constructor for TestLuceneIndex.
     */
    public TestLuceneIndex(String name) {
        super(name);
    }
    
    /** Testing getting reader and writer */
	public void testReaderWriter() {
        LuceneIndex theIndex = LuceneIndex.getInstance();
        assertNotNull(theIndex);
        assertTrue(theIndex.indexExists());
        
        IndexReader reader = null;
        IndexWriter writer1 = null;
        IndexWriter writer2 = null;
        try {
            // open lucene index reader
            reader = theIndex.openReaderNowOrFail();

            // open first lucene index writer
            writer1 = theIndex.createWriterNowOrFail(false);
            // Be patient this will take some time ...
            writer2 = theIndex.createWriterNowOrFail(false);
            fail("Expected LockObtainFailedException");
        } catch (LockObtainFailedException expected) {
            // expected
        } finally {
            try {
                if (reader != null) {
                	try {
                		reader.close();
					} finally {
						if (writer1 != null) {
							try {
								writer1.close();
							} finally {
								if (writer2 != null) {
									writer2.close();
								}
							}
						}
					}
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace(); // Do not mask original failure or error ...
            }
        }
    }
    
    /** Test indexing many files */
    public void testIndexing() throws Exception {
        this.startTime();

        LuceneIndex theIndex = LuceneIndex.getInstance();

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
        	Logger.debug("Queue size: " + theIndex.queueSizes(), TestLuceneIndex.class);
			Thread.sleep(waitTime);
        }
        while ((theIndex.queueSizes()) > 0 && (maxNumberWait--) > 0);
		if (maxNumberWait <= 0) {
			Object luceneThread = ReflectionUtils.getValue(theIndex, "myThread");
			
			Object addQueue = ReflectionUtils.getValue(luceneThread, "addQueue");
			assertInstanceof(addQueue, Collection.class);
			
			Object deleteQueue = ReflectionUtils.getValue(luceneThread, "deleteQueue");
			assertInstanceof(deleteQueue, Collection.class);
			
			fail("No Progress in testIndexing? There are still " + ((Collection<?>) addQueue).size() + " objects to index and " + ((Collection<?>) deleteQueue).size() +" objects to delete! (Indexed: " + numberOfIndexedElements + ")");
		}
    }

    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
        TestSuite theSuite = new TestSuite (TestLuceneIndex.class);
         
        ActiveTestSuite parSuite = new ActiveTestSuite();
        parSuite.addTest(TestUtils.tryEnrichTestnames(new TestLuceneIndex("testIndexing"), "ActiveTestSuite 1"));
        parSuite.addTest(TestUtils.tryEnrichTestnames(new TestLuceneIndex("testIndexing"), "ActiveTestSuite 2"));
        parSuite.addTest(TestUtils.tryEnrichTestnames(new TestLuceneIndex("testIndexing"), "ActiveTestSuite 3"));
        theSuite.addTest(parSuite);

		return LuceneSearchTestSetup.createSearchTestSetup(DataSetup.createDataSetup(theSuite, "."));
    }
    
}
