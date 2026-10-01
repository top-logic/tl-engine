/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.TLID;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;

/**
 * Test case for Wrapper of the {@link com.top_logic.knowledge.objects.KnowledgeObject
 * KnowledgeObjects} of type Document.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TestDocument extends AbstractDocumentTest {

	/** This file should probably exist ... */
	private static final String TEST_FILE =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/knowledge/wrap/TestDocument.java";

	/** This file should probably exist ... */
	private static final String HTML_FILE =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/knowledge/wrap/TestDocumentFixture.html";

	/** The name of the test document to be used. */
	private static final String TEST_NAME = "TestDocument.txt";

	/** The name of the HTML document to be used. */
	private static final String HTML_NAME = "TestDocument.html";

	private Document _testDocument;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_testDocument = getOrCreateChildDocument(TEST_NAME);
	}

	@Override
	protected void tearDown() throws Exception {
		Transaction deleteTx = _testDocument.getKnowledgeBase().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		boolean succeeded = this._folder.remove(_testDocument);
		deleteTx.commit();
		assertTrue("Error removing document " + _testDocument, succeeded);
		super.tearDown();
	}

	/**
	 * Test for boolean updateDocument(InputStream) and other functions
	 */
	public void testUpdate() throws Exception {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();

		final Document oldDocument =
			(Document) WrapperHistoryUtils.getWrapper(HistoryUtils.getLastRevision(), _testDocument);
		InputStream in1 = this._emptyContent.getStream();
		try {
			InputStream in2 = _testDocument.getStream();
			try {
				assertTrue("Created file doesn't match the original one!", FileUtilities.equalsStreamContents(in1, in2));
			} finally {
				in2.close();
			}
		} finally {
			in1.close();
		}
		InputStream in11 = this._emptyContent.getStream();
		try {
			InputStream in2 = oldDocument.getStream();
			try {
				assertTrue("Stable version of document doesn't match the original one!",
					FileUtilities.equalsStreamContents(in11, in2));
			} finally {
				in2.close();
			}
		} finally {
			in11.close();
		}


		Transaction updateTx = _testDocument.getKnowledgeBase().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		assertFalse("Error in sending null to update method within " +
			"document " + _testDocument, _testDocument.update((BinaryData) null));

		assertTrue("Error in uploading new content to document " + _testDocument,
			_testDocument.update(this.getData(TEST_FILE)));

		updateTx.commit();
		InputStream in12 = this.getData(TEST_FILE).getStream();
		try {
			InputStream in2 = _testDocument.getStream();
			try {
				assertTrue("Created file doesn't match the original one!",
					FileUtilities.equalsStreamContents(in12, in2));
			} finally {
				in2.close();
			}
		} finally {
			in12.close();
		}
		InputStream in13 = this._emptyContent.getStream();
		try {
			InputStream in2 = oldDocument.getStream();
			try {
				assertTrue("Stable version of document changed value after update!",
					FileUtilities.equalsStreamContents(in13, in2));
			} finally {
				in2.close();
			}
		} finally {
			in13.close();
		}



		TLID docId = KBUtils.getWrappedObjectName(_testDocument);

		assertEquals(_testDocument, Document.getInstance(kb, docId));
		assertEquals(_testDocument, Document.getInstance(kb, docId));

		assertNotNull(_testDocument.getSimilar());
		assertNotNull(_testDocument.getKeywords());
		assertNotNull(_testDocument.getKeywordsAsString());
	}

	/**
	 * Test extraction of titles from HTMDocuments and other obscure things.
	 */
	public void testExtraction() throws Exception {
		_testDocument = getOrCreateChildDocument(HTML_NAME);

		Transaction updateTx = _testDocument.getKnowledgeBase().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		_testDocument.update(this.getData(HTML_FILE));
		updateTx.commit();

		InputStream in1 = this.getData(HTML_FILE).getStream();
		try {
			InputStream in2 = _testDocument.getStream();
			try {
				assertTrue(FileUtilities.equalsStreamContents(in1, in2));
			} finally {
				in2.close();
			}
		} finally {
			in1.close();
		}


		assertEquals("text/html", _testDocument.getContentType());
	}

	/* Test for OutputStream updateDocument() public void testUpdateDocument() throws Exception { } */

	/**
	 * Tests that each update stores a new content version, and that the current and historic
	 * revisions read their own content.
	 */
	public void testContentVersions() throws Exception {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		Document document = getOrCreateChildDocument("TestContentVersions.txt");
		assertEquals(1, document.getVersionNumber());
		assertEquals(_emptyContent.getSize(), document.getSize());

		byte[] content2 = "Second version of the document.".getBytes(StandardCharsets.UTF_8);
		byte[] content3 = "Third version of the document, a bit longer than the second one.".getBytes(StandardCharsets.UTF_8);

		Transaction tx2 = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		assertTrue(document.update(BinaryDataFactory.createBinaryData(content2, "application/octet-stream", "other.bin")));
		tx2.commit();
		Document revision2 = (Document) WrapperHistoryUtils.getWrapper(tx2.getCommitRevision(), document);

		Transaction tx3 = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		assertTrue(document.update(BinaryDataFactory.createBinaryData(content3)));
		tx3.commit();

		assertEquals(3, document.getVersionNumber());
		assertEquals(content3.length, document.getSize());
		assertEquals("TestContentVersions.txt", document.getName());
		assertEquals("text/plain", document.getContentType());
		assertContent(content3, document);

		BinaryData stored = document.getStoredContent();
		assertEquals("The stored content has the name of the document.", document.getName(), stored.getName());
		assertEquals("The stored content has the content type of the document.", document.getContentType(),
			stored.getContentType());
		assertEquals(content3.length, stored.getSize());

		assertEquals(2, revision2.getVersionNumber());
		assertEquals(content2.length, revision2.getSize());
		assertContent(content2, revision2);

		List<? extends DocumentVersion> versions = document.getDocumentVersions();
		assertEquals(3, versions.size());
		assertEquals(3, versions.get(0).getRevision());
		assertContent(content3, versions.get(0).getDocument());
		assertEquals(2, versions.get(1).getRevision());
		assertContent(content2, versions.get(1).getDocument());
		assertEquals(1, versions.get(2).getRevision());
		assertEquals(_emptyContent.getSize(), versions.get(2).getDocument().getSize());

		assertContent(content2, document.getVersionContent(2));
		assertContent(content3, document.getVersionContent(3));
		assertNull(document.getVersionContent(4));

		Transaction deleteTx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
		assertTrue(this._folder.remove(document));
		deleteTx.commit();

		assertContent(content2, revision2);
	}

	private static void assertContent(byte[] expected, BinaryData actual) throws IOException {
		assertNotNull(actual);
		try (InputStream in = actual.getStream()) {
			assertEquals(expected, StreamUtilities.readStreamContents(in));
		}
	}

	/**
	 * Tests that creating the test document worked.
	 */
	public void testExists() throws Exception {
		assertNotNull("Error in creating document (" + TEST_NAME + ')', _testDocument);

		assertTrue(_testDocument.exists());
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		return AbstractDocumentTest.singleKBSsuite(TestDocument.class);
	}
}
