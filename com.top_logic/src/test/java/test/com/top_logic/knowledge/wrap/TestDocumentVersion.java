/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;

/**
 * Tests the {@link DocumentVersion}
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestDocumentVersion extends AbstractDocumentTest {
   
    /** The name of the test document to be used. */
    private static final String TEST_NAME = "TestDocument.txt";

	/** This file should probably exist ... */
	private static final String TEST_FILE =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/knowledge/wrap/TestDocumentVersion.java";


    public void testDocVersion() throws Exception {
		Document document = getOrCreateChildDocument(TEST_NAME);
		KnowledgeBase kb = document.getKnowledgeBase();

		assertNotNull("Unable to get DAP when there is no physical resource for Document " + document,
			document.getDAP());

		assertEquals(1, document.getDocumentVersions().size());

		final Wrapper oldDoc = WrapperHistoryUtils.getWrapper(HistoryUtils.getLastRevision(kb.getHistoryManager()), document);
		
		final Wrapper updatedDoc;
		{
			final Transaction tx = kb.beginTransaction();
			assertTrue("Error in uploading new content to document " + document,
				document.update(this.getData(TEST_FILE)));
			tx.commit();
			updatedDoc = WrapperHistoryUtils.getWrapper(tx.getCommitRevision(), document);

		}

		assertNotNull("Unable to get DAP even when there a physical resource for Document " + document,
			document.getDAP());
		{
			List<? extends DocumentVersion> res = document.getDocumentVersions();
			assertEquals(2, res.size());

			DocumentVersion version2 = res.get(0);
			assertEquals(2, version2.getRevision());
			assertEquals(updatedDoc, version2.getDocument());

			DocumentVersion version1 = res.get(1);
			assertEquals(1, version1.getRevision());
			assertEquals(oldDoc, version1.getDocument());

			assertEquals(version1, DocumentVersion.getDocumentVersion(document, version1.getRevision()));
		}
		{
			final Transaction tx = kb.beginTransaction();
			document.update(this.getData(TEST_FILE));
			tx.commit();
			List<? extends DocumentVersion> res = document.getDocumentVersions();
			assertEquals(3, res.size());
		}
		{
			final Transaction tx = kb.beginTransaction();
			document.update(this.getData(TEST_FILE));
			tx.commit();
			List<? extends DocumentVersion> res = document.getDocumentVersions();
			assertEquals(4, res.size());

			assertEquals(3, res.get(1).getRevision());
			assertEquals(2, res.get(2).getRevision());
			assertEquals(1, res.get(3).getRevision());
			
			assertEquals(oldDoc, res.get(3).getDocument());        
		}
        
		assertTrue("Error removing document " + document, this._folder.remove(document));
    }

	/**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
		return AbstractDocumentTest.singleKBSsuite(TestDocumentVersion.class);
    }

}
