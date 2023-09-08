/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge;

import static org.junit.Assert.*;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.knowledge.searching.lucene.TestLuceneSearchEngine;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.DefaultIndexingService.DefaultIndexingServiceConfig;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.indexing.lucene.LuceneIndexingService;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * The class {@link IndexedDataSetup} creates a collection of KOs for testing and indexes them.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IndexedDataSetup extends ThreadContextSetup {

	/**
	 * Create a Test that uses an {@link IndexedDataSetup}
	 */
	public static Test createSetup(Test test, String dataRoot) {
		return DataSetup.createDataSetup(new IndexedDataSetup(test), dataRoot);
	}

	private LuceneIndexingService luceneIndexer;

	private IndexedDataSetup(Test test) {
		super(test);
	}

	/**
	 * Returns the modifiable list of created KO's. Must just be called in a test which uses this setup.
	 * 
	 * @see DataSetup#getKnowledgeObjects()
	 */
	public static List<KnowledgeObject> getKOs() {
		return DataSetup.getKnowledgeObjects();
	}

	@Override
	protected void doSetUp() throws Exception {
		// make some initialization
		DefaultIndexingServiceConfig emptyLuceneIndexServiceConfig = TypedConfiguration.newConfigItem(DefaultIndexingServiceConfig.class);
		luceneIndexer =
			new LuceneIndexingService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				emptyLuceneIndexServiceConfig);
		for (KnowledgeObject docKO : DataSetup.getKnowledgeObjects()) {
			luceneIndexer.indexContent(DefaultIndexingService.createContent(docKO));
		}

		waitIndexFinished();

	}

	private void waitIndexFinished() throws InterruptedException {
		LuceneIndex theIndex = LuceneIndex.getInstance();
		int maxWait = 300; // seconds
		do {
			if (theIndex.isIdle()) {
				break;
			}
			maxWait--;
			Thread.sleep(1000);
		} while (maxWait > 0);

		int finalQueueSize = theIndex.queueSizes();
		assertEquals("No Progress in queueSizes? Still " + finalQueueSize + " elements to process!", 0, finalQueueSize);
	}

	@Override
	protected void doTearDown() throws Exception {
		final LuceneIndexingService tmpLuceneIndexer = luceneIndexer;
		luceneIndexer = null;
		for (KnowledgeObject ko : DataSetup.getKnowledgeObjects()) {
			try {
				tmpLuceneIndexer.removeContent(ko.tId());
				Logger.debug("Removed document " + ko, TestLuceneSearchEngine.class);
			} catch (IndexException ignored) {
				Logger.debug("Unable to remove document " + ko, ignored, TestLuceneSearchEngine.class);
			}
		}
		
		waitIndexFinished();
	}

}
