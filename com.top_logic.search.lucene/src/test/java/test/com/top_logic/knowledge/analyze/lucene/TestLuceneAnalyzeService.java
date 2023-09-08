/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.analyze.lucene;

import java.util.Collection;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.IndexedDataSetup;
import test.com.top_logic.knowledge.LuceneSearchTestSetup;

import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.analyze.AnalyzeService;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.analyze.lucene.LuceneAnalyzeService;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * {@link TestCase} for the {@link LuceneAnalyzeService}.
 * 
 * <b>If the test fails, before starting to debug it, make sure your Lucene index is not
 * corrupted.</b>
 * 
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class TestLuceneAnalyzeService extends BasicTestCase {

    /** The first Document created (as a single KO) */
    private KnowledgeObject firstDocument;

    /**
     * Constructor for TestLuceneAnalyzeService
     */
    public TestLuceneAnalyzeService(String name) {
        super(name);
    }
    
    // Some tests 

    /** Test the analyze service */
    public void testAnalyzer() throws Exception {
		AnalyzeService theAnalyzer = DefaultAnalyzeService.getAnalyzeService();
        
        // test similar without ranking
		Collection<?> col1 = theAnalyzer.findSimilar(firstDocument);
		assertTrue(
			"There should be more than 0 similar documents. (If your Lucene index is corrupted, this might be an aftereffect.)",
			col1.size() > 0);
        
        // test similar with ranking
		Collection<?> col2 = theAnalyzer.findSimilarRanked(firstDocument);
		assertTrue(
			"There should be more than 0 similar and ranked documents. (If your Lucene index is corrupted, this might be an aftereffect.)",
			col2.size() > 0);
        
        // test keywords
		Collection<?> keywords = theAnalyzer.extractKeywords(firstDocument);
		assertTrue(
			"There should be more than 0 keywords for that document. (If your Lucene index is corrupted, this might be an aftereffect.)",
			keywords.size() > 0);
    }

    // Test Setup and Teardown

    @Override
	protected void setUp() throws Exception {
    	super.setUp();
    	firstDocument = IndexedDataSetup.getKOs().get(0);
     }

    @Override
	protected void tearDown() throws Exception {
    	firstDocument = null;
        super.tearDown();
    }


    /** 
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		// the modules are needed by LuceneAnalyzeService. We must start it
		// manually as we instantiate the class manually
		final Test test = ServiceTestSetup.createSetup(TestLuceneAnalyzeService.class,
			FormatConverterFactory.Module.INSTANCE, MimeTypes.Module.INSTANCE);
		return LuceneSearchTestSetup.createSearchTestSetup(IndexedDataSetup.createSetup(test,
			"com/top_logic/knowledge/analyze/lucene/testdata"));
    }

}
