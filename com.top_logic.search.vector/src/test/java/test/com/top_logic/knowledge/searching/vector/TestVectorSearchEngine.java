/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.searching.vector;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.IndexedDataSetup;
import test.com.top_logic.knowledge.VectorSearchTestSetup;

import com.top_logic.base.search.Query;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.searching.Precondition;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchAttribute;
import com.top_logic.knowledge.searching.SearchEngine;
import com.top_logic.knowledge.searching.SearchResult;
import com.top_logic.knowledge.searching.SearchResultSetImpl;
import com.top_logic.knowledge.searching.vector.VectorSearchEngine;
import com.top_logic.knowledge.searching.vector.VectorSearchEngine.Config;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Test the {@link VectorSearchEngine}.
 * 
 * @author Dieter Rothbächer
 */
public class TestVectorSearchEngine extends BasicTestCase {

	/** The query expression used for testing the search process */
	private static final String QUERY_EXPRESSION = "$('TopLogic')";

	/**
	 * Constructor for TestSearchService.
	 */
	public TestVectorSearchEngine(String name) {
		super(name);
	}

	/**
	 * Test getting the search attributes
	 */
	public void testGetSearchAttributes() {
		VectorSearchEngine vectorEngine = newVectorEngine();

		List<?> theAttributes = vectorEngine.getSearchAttributes();
		assertNotNull("List of search attributes must not be null.", theAttributes);
		assertFalse("Vector supports at least document ranking.", theAttributes.isEmpty());
	}

	private VectorSearchEngine newVectorEngine() {
		Config vectorSearchConf = TypedConfiguration.newConfigItem(VectorSearchEngine.Config.class);
		vectorSearchConf.setDisplayName(ResKey.forTest("Vector Index search engine"));
		VectorSearchEngine vectorEngine = TypedConfigUtil.createInstance(vectorSearchConf);
		return vectorEngine;
	}

	/**
	 * Test search process
	 */
	public void testSearch() throws Exception {
		final VectorSearchEngine vectorSearcher = newVectorEngine();

		// 1. create query configuration
		QueryConfig qConfig = new QueryConfig();
		qConfig.setQuery(Query.parse(QUERY_EXPRESSION));
		Precondition pre = this.createPrecondition(vectorSearcher);
		List<?> theAttributes = pre.getSearchAttributes();
		int idxRanking = theAttributes.indexOf(SearchAttribute.RANKING);
		int idxDescr = theAttributes.indexOf(SearchAttribute.DESCRIPTION);
		if (idxRanking != -1) {
			SearchAttribute theRanking = (SearchAttribute) theAttributes.get(idxRanking);
			theRanking.setValue(Float.valueOf(0.0f));
		}
		if (idxDescr != -1) {
			SearchAttribute theDescr = (SearchAttribute) theAttributes.get(idxDescr);
			theDescr.setValue(Boolean.TRUE);
		}
		qConfig.setPrecondition(pre);
		pre.setKnowledgeBaseName(PersistencyLayer.getKnowledgeBase().getName());
		// 2. perform search
		SearchResultSetImpl searchResultSet = new SearchResultSetImpl(list(vectorSearcher));

		// start time
		this.startTime();

		vectorSearcher.search(qConfig, searchResultSet);

		// 3. wait until search has finished
		assertTrue(searchResultSet.waitForClosed(10000));
		// searching should never take 10 sec <- Come on, this is a test case,
		// this may take "forever", don't care about it!!!

		// end time
		this.logTime("searching");

		// 4. make some tests for each result
		List<?> results = searchResultSet.getSearchResults();
		assertFalse("No result matching your query found.", searchResultSet.getSearchResults().isEmpty());
		for (Object result : results) {
			SearchResult theResult = (SearchResult) result;
			List<?> attributes = theResult.getAttributes();
			assertNotNull("List of search result attributes must not be null.", attributes);
			assertTrue("Vector Index supports at least document ranking.", attributes.size() >= 2);
			List<?> theEngines = theResult.getEngines();
			assertTrue("The Precondition should contain only the VectorSearchEngine.", theEngines.size() == 1);
		}
	}

	/**
	 * Creating a precondition for the search process
	 */
	protected Precondition createPrecondition(SearchEngine anEngine) {
		Precondition theCond = new Precondition();
		List<SearchAttribute> searchAttributes = anEngine.getSearchAttributes();
		if (theCond.addSearchEngine(anEngine)) {
			for (SearchAttribute attr : searchAttributes) {
				SearchAttribute theAttr = attr;
				theCond.addSearchAttributes(theAttr);
			}
		}
		return theCond;
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {

		TestSuite suite = new TestSuite(TestVectorSearchEngine.class);
		return VectorSearchTestSetup.createSearchTestSetup(IndexedDataSetup.createSetup(suite, "."));
	}

}
