/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.io.binary.TestingBinaryData;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.util.SystemContextThread;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.KOAttributes;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLObject;

/**
 * Test case for {@link WebFolder} .
 */
@SuppressWarnings("javadoc")
public class TestWebFolder extends BasicTestCase {

	/**
	 * Number of Threads to use for Threaded tests.
	 * 
	 * Mhh, wont work with more than 3 ? ...
	 */
	private int THREADCOUNT = 3;

	/**
	 * Number of Millisecond to wait for a thread to finish.
	 */
	private int THREAD_JOIN = 50000;

	/** This file should probably exist ... */
	private static final String TEST_FILE =
		ModuleLayoutConstants.SRC_TEST_DIR + "/test/com/top_logic/knowledge/wrap/TestWebFolder.java";

	/** Base DSN (This directory is cleared before Testing). */
	private static final String TEST_BASE = "file://tmp";

	/** Base DSN for repository (This directory is cleared before Testing). */
	private static final String TEST_KNOWLEDGE = "repository://";

	/** DSN to locate some existing data */
	private static final String TEST_URL = "webapp://";

	/**
	 * Constructor for a special test.
	 * 
	 * @param name
	 *        function name of the test to execute.
	 */
	public TestWebFolder(String name) {
		super(name);
	}

	public void testCopyRecursivly() throws IOException {
		new CopyScenario(false, false).doTest();
	}

	public void testCopyRecursivlySoft() throws IOException {
		new CopyScenario(true, false).doTest();
	}

	public void testCopyRecursivlyVersion() throws IOException {
		new CopyScenario(false, true).doTest();
	}

	public void testCopyRecursivlySoftVersion() throws IOException {
		new CopyScenario(true, true).doTest();
	}

	static class CopyScenario {
		private static final String ROOT = "root";

		private static final String SUB_1 = "sub1";

		private static final String DOC_1 = "doc1";

		private static final String SUB_2 = "sub2";

		public KnowledgeBase _kb;

		public BinaryData _data1;

		private BinaryData _data2;

		private BinaryData _data3;

		public WebFolder _root;

		private WebFolder _sub1;

		boolean _useSoftLinks;

		boolean _useVersionLinks;

		public CopyScenario(boolean useSoftLinks, boolean useVersionLinks) {
			_useSoftLinks = useSoftLinks;
			_useVersionLinks = useVersionLinks;

			_kb = KBSetup.getKnowledgeBase();
			_data1 = new TestingBinaryData(42, 10240);
			_data2 = new TestingBinaryData(13, 5678);
			_data3 = new TestingBinaryData(7, 7890);
		}

		void doTest() throws IOException {
			Document document = doCreate();
			WebFolder copy = doCopy();
			doUpdate(document);
			doCheck(copy);
		}

		private Document doCreate() {
			Document document;
			Transaction tx1 = _kb.beginTransaction();
			try {
				_root = WebFolder.createFolder(_kb, ROOT, TestWebFolder.TEST_KNOWLEDGE);
				{
					_sub1 = _root.createSubFolder(SUB_1);
					{
						document = createDocument(_sub1, DOC_1, _data1);
					}
					WebFolder sub2 = _root.createSubFolder(SUB_2);
					assertNotNull(sub2);
				}
				tx1.commit();
			} finally {
				tx1.rollback();
			}

			Transaction tx2 = _kb.beginTransaction();
			try {
				document.update(_data2);
				tx2.commit();
			} finally {
				tx2.rollback();
			}
			return document;
		}

		private WebFolder doCopy() {
			WebFolder clone;
			Transaction tx2 = _kb.beginTransaction();
			try {
				clone =
					WebFolder.createFolder(_kb, "clone-" + _useSoftLinks + "-" + _useVersionLinks,
						TestWebFolder.TEST_BASE);
				WebFolder.copyContents(_root, clone, _useSoftLinks, _useVersionLinks);
				tx2.commit();
			} finally {
				tx2.rollback();
			}
			return clone;
		}

		private void doUpdate(Document document) {
			Transaction tx = _kb.beginTransaction();
			try {
				document.update(_data3);

				tx.commit();
			} finally {
				tx.rollback();
			}
		}

		private void doCheck(WebFolder clone) throws IOException {
			BasicTestCase.assertNotNull(childByName(clone, SUB_1));
			BasicTestCase.assertNotNull(childByName((WebFolder) childByName(clone, SUB_1), DOC_1));
			TLObject child = childByName((WebFolder) childByName(clone, SUB_1), DOC_1);
			try (InputStream copiedData = stream(child)) {
				BinaryData expectedContent = _useVersionLinks ? _data2 : _data3;
				BasicTestCase.assertTrue(StreamUtilities.equalsStreamContents(expectedContent.getStream(), copiedData));
			}
			BasicTestCase.assertNotNull(childByName(clone, SUB_2));
		}

		private InputStream stream(TLObject child) throws IOException {
			if (child instanceof Document) {
				return ((Document) child).getStream();
			} else if (child instanceof DocumentVersion) {
				return ((DocumentVersion) child).getStream();
			} else {
				throw new IllegalStateException(child + " is neither " + Document.class.getName() + " nor "
					+ DocumentVersion.class.getName());
			}
		}
	}

	public void testDeleteRecursivly() {
		KnowledgeBase kb = kb();
		// Deleting empty folder
		WebFolder emptyFolder = WebFolder.createFolder(kb, "ERNA", TEST_BASE);
		KnowledgeObject emptyFolder_KO = emptyFolder.tHandle();
		assertNotNull(emptyFolder_KO);
		assertTrue("Deleting empty Folder failed", WebFolder.deleteRecursively(emptyFolder));

		// deleting folder containing document, folder with document, and empty folder
		WebFolder folder = WebFolder.createFolder(kb, "ERNA", TEST_BASE);
		KnowledgeObject folder_KO = folder.tHandle();
		assertNotNull(folder_KO);
		WebFolder childFolder = createChildFolder(folder, "child");
		KnowledgeObject childFolder_KO = childFolder.tHandle();
		assertNotNull(childFolder_KO);
		WebFolder emptyChildFolder = createChildFolder(folder, "emptyChild");
		KnowledgeObject emptyChildFolder_KO = emptyChildFolder.tHandle();
		assertNotNull(emptyChildFolder_KO);
		Document file = createDocument(folder, "file.txt", this.getTestFile());
		Document innerFile = createDocument(childFolder, "innerFile.txt", this.getTestFile());

		assertTrue(WebFolder.deleteRecursively(folder));
		assertFalse(file.tValid());
		assertFalse(innerFile.tValid());
	}

	private KnowledgeBase kb() {
		return KBSetup.getKnowledgeBase();
	}

	/** Test Creation of Documents inside a Webfolder */
	public void testCreate() throws Exception {
		KnowledgeBase kb = kb();
		Transaction tx1 = kb.beginTransaction();

		WebFolder theFolder = WebFolder.createFolder(kb, "ERNA", TEST_BASE);
		assertNotNull(theFolder);

		theFolder = (WebFolder) assertSerializable(theFolder);
		assertNotNull(theFolder);

		try {
			tx1.commit();
		} catch (KnowledgeBaseException ex) {
			fail("Error in commiting the knowledgebase for folder " + theFolder, ex);
		}

		String dsn = (String) theFolder.tHandle().getAttributeValue(KOAttributes.PHYSICAL_RESOURCE);
		assertTrue("DSN was not saved correctly : " + dsn, dsn.startsWith(TEST_BASE));

		Transaction tx2 = kb.beginTransaction();
		createChildFolder(theFolder, "SUBFOLDER ERNA");
		try {
			tx2.commit();
		} catch (KnowledgeBaseException ex) {
			fail("Error in commiting the knowledgebase for folder " + theFolder, ex);
		}

		theFolder.clear();

		assertTrue("Error removing folder " + theFolder,
			WebFolder.deleteRecursively(theFolder));
	}

	/** Tests .... */
	public void testGetInstance() throws Exception {
		String theURL = TEST_URL;

		KnowledgeBase kb = kb();
		WebFolder theFolder1 = this.getFolder();
		WebFolder theFolder2 = WebFolder.findFolderByDSN(kb, theURL);
		assertNotNull("Unable to get folder via URL: " + theURL + " in " + kb, theFolder2);
		assertEquals("Not from the right knowledge base!", kb, theFolder2.getKnowledgeBase());

		assertTrue("Not the same WebFolder when requested via object and URL!", (theFolder1 == theFolder2));

		WebFolder theFolder3 = WebFolder.getInstance(kb, KBUtils.getWrappedObjectName(theFolder1));
		assertNotNull("Unable to get folder via ID: " + theURL + " in " + kb, theFolder3);

		assertTrue("Not the same WebFolder when requested via object and ID!", (theFolder1 == theFolder3));

		WebFolder theFolder4 = WebFolder.getInstance(kb, new DataAccessProxy(theURL));
		assertNotNull("Unable to get folder via DataAccessProxy: " + theURL + " in " + kb, theFolder4);

		assertTrue("Not the same WebFolder when requested via ID and DataAccessProxy!", (theFolder1 == theFolder4));
	}

	/** Tests .... */
	public void testExistence() {
		WebFolder theFolder1 = this.getFolder();
		assertFalse("Folder should have entries!", theFolder1.isEmpty());
	}

	/**
	 * Tests Uploading of a simple File
	 * 
	 * @param aName
	 *        name to use for creating Folder and Document.
	 */
	public void testUpload(String aName) throws Exception {
		doTestUpload(aName, TEST_BASE);
	}

	public void testUpload2() throws Exception {
		doTestUpload("TestWebFolderUpload2", TEST_KNOWLEDGE);
	}

	/**
	 * Tests Uploading of a simple File
	 * 
	 * @param aName
	 *        name to use for creating Folder and Document.
	 */
	public void doTestUpload(String aName, String aFolderBase) throws Exception {
		KnowledgeBase theBase = kb();
		Transaction tx1 = theBase.beginTransaction();
		WebFolder theFolder1 = WebFolder.createFolder(theBase, aName, aFolderBase);
		tx1.commit();
		{
			aName = theFolder1.getName(); // may use different name ...
			assertNotNull("WebFolder has no Name", aName);

			Transaction tx2 = theBase.beginTransaction();
			WebFolder theFolder2 = createChildFolder(theFolder1, "Upload");
			tx2.commit();
			{
				assertNotNull("WebFolder has no Name", theFolder2.getName());

				assertNotNull("Folder not created!", this.getFolder(theFolder2.getDSN()));

				Transaction tx3 = theBase.beginTransaction();
				Document theDocument = createDocument(theFolder2, aName + ".txt", this.getTestFile());
				tx3.commit();
				{
					// create a second version
					Transaction updateTx = theBase.beginTransaction();
					theDocument.update(this.getTestFile());
					updateTx.commit();

					Integer version = theDocument.getVersionNumber();
					if (TEST_KNOWLEDGE.equals(aFolderBase)) {
						assertEquals(Integer.valueOf(2), version);
					}

					assertNotNull("Document has no Name", theDocument.getName());

					assertNotNull("Document not found in " + theBase + '!', this.getDocument(theDocument.getDSN()));

					this.validateDocument(theDocument);

					Transaction removeDocTx = theBase.beginTransaction();
					assertTrue(theFolder2.remove(theDocument));
					removeDocTx.commit();

					Transaction removeFolderTx = theBase.beginTransaction();
					assertTrue(theFolder1.remove(theFolder2));
					removeFolderTx.commit();

					Transaction removeRecTx = theBase.beginTransaction();
					assertTrue(WebFolder.deleteRecursively(theFolder1));
					removeRecTx.commit();
				}
				/* finally */ {
					Transaction txDel = theBase.beginTransaction();
					theDocument.tDelete();
					txDel.commit();

				}
			}
			/* finally */ {
				Transaction txDel = theBase.beginTransaction();
				theFolder2.tDelete();
				txDel.commit();
			}
		}
		/* finally */ {
			Transaction txDel = theBase.beginTransaction();
			theFolder1.tDelete();
			txDel.commit();
		}
	}

	/** Tests Uploading of a simple File */
	public void testUpload() throws Exception {

		testUpload("TestWebFolderUpload");
	}

	private interface MultiThreadedTest {
		void executeTest(String nameSuffix) throws Exception;
	}

	/** Tests Uploading of with multipleThreads. */
	public void testThreadUpload() throws Exception {
		runMultiThreaded(new MultiThreadedTest() {

			@Override
			public void executeTest(String nameSuffix) throws Exception {
				testUpload("ThreadUpload_" + nameSuffix);
			}
		});
	}

	private void runMultiThreaded(final MultiThreadedTest callback) throws Exception {
		final Exception[] problems = new Exception[THREADCOUNT];
		Thread[] threads = new Thread[THREADCOUNT];
		for (int i = 0; i < THREADCOUNT; i++) {
			final int threadIndex = i;
			final String name = String.valueOf(i);
			threads[i] = new SystemContextThread("Thread: " + name) {

				@Override
				public void internalRun() {
					super.internalRun();
					try {
						callback.executeTest(name);
					} catch (Exception ex) {
						problems[threadIndex] = ex;
					}
				}
			};
		}
		for (int i = 0; i < THREADCOUNT; i++) {
			Thread thread = threads[i];
			thread.setDaemon(true);
			thread.start();
			Thread.yield();
		}

		callback.executeTest("Base");

		for (int i = 0; i < THREADCOUNT; i++) {
			Thread theThread = threads[i];
			theThread.join(THREAD_JOIN);
			assertFalse("Thread still active", theThread.isAlive());
			if (problems[i] != null) {
				throw problems[i];
			}
		}
	}

	/**
	 * Test a document based on a repository
	 * 
	 * @param folderName
	 *        name to use for folder/document.
	 */
	public void testRepository(String folderName) throws Exception {
		KnowledgeBase theBase = kb();

		assertTrue(TEST_KNOWLEDGE + " does not exists", new DataAccessProxy(TEST_KNOWLEDGE).exists());

		Transaction createFolderTx = theBase.beginTransaction();
		WebFolder folder = WebFolder.createFolder(theBase, folderName, TEST_KNOWLEDGE);
		createFolderTx.commit();
		try {
			assertNotNull("Failed to create folder in " + theBase, folder);

			Transaction createDocTx = theBase.beginTransaction();
			Document document = createDocument(folder, folderName + ".txt", this.getTestFile());
			createDocTx.commit();
			try {
				assertNotNull("No document returned from createDocument()!", document);

				assertNotNull("Document not found in " + theBase + '!', this.getDocument(document.getDSN()));

				this.validateDocument(document);

				Transaction renameTx = theBase.beginTransaction();
				assertTrue("Cannot rename document in Repository", !document.rename("test01.txt"));
				renameTx.commit();

				this.validateDocument(document);

				Integer documentVersion = document.getVersionNumber();

				Transaction updateTx = theBase.beginTransaction();
				document.update(this.getTestFile());
				updateTx.commit();

				Integer theVersion2 = document.getVersionNumber();
				assertTrue("Version not changed!", !documentVersion.equals(theVersion2));
			} finally {
				Transaction delTx = theBase.beginTransaction();
				document.tDelete();
				delTx.commit();
			}
		} finally {
			Transaction delTx = theBase.beginTransaction();
			folder.tDelete();
			delTx.commit();
		}
	}

	/** Test a document based on a repository */
	public void testRepository() throws Exception {
		testRepository("TestWebFolderRepository");
	}

	/** Tests Repository with multipleThreads. */
	public void testThreadRepository() throws Exception {
		runMultiThreaded(new MultiThreadedTest() {
			
			@Override
			public void executeTest(String nameSuffix) throws Exception {
				testRepository("ReposUpload_" + nameSuffix);
			}
		});
	}


	private void validateDocument(Document aDoc) throws Exception {
		DataAccessProxy theProxy = aDoc.getDAP();
		try (InputStream theOrig = this.getTestFile().getStream()) {
			try (InputStream in2 = theProxy.getEntry()) {
				assertTrue("Created file doesn't match the original one!",
					FileUtilities.equalsStreamContents(theOrig, in2));
			}
		}

	}

	private BinaryData getTestFile() {
		return BinaryDataFactory.createBinaryData(new File(TEST_FILE));
	}

	private WebFolder getFolder() {
		return (this.getFolder(TEST_URL));
	}

	private WebFolder getFolder(String anURL) {
		KnowledgeBase theBase = kb();
		KnowledgeObject theObject = this.search(theBase, WebFolder.OBJECT_NAME, KOAttributes.PHYSICAL_RESOURCE, anURL);

		assertNotNull("Unable to find folder: " + anURL + " in " + theBase, theObject);

		WebFolder theFolder = (WebFolder) WrapperFactory.getWrapper(theObject);
		assertNotNull("Unable to get folder via KnowledgeObject: " + anURL + " in " + theBase, theFolder);
		assertEquals("Not from the right knowledge base!", theBase, theFolder.tHandle().getKnowledgeBase());

		return (theFolder);
	}

	/** Used to find a Document matching the given DSN. */
	protected Document getDocument(String aDSN) {
		KnowledgeBase theBase = kb();
		KnowledgeObject theObject = this.search(theBase, Document.OBJECT_NAME, KOAttributes.PHYSICAL_RESOURCE, aDSN);

		assertNotNull("Unable to find document: " + aDSN + " in " + theBase, theObject);

		Document theFolder = Document.getInstance(theObject);

		assertNotNull("Unable to get document via KnowledgeObject: " + aDSN + " in " + theBase, theFolder);
		assertEquals("Not from the right knowledge base!", theBase, theFolder.tHandle().getKnowledgeBase());

		return (theFolder);
	}

	/** Used to fetch an Object By Attribute. */
	protected KnowledgeObject search(KnowledgeBase aBase, String aType, String aKey, String anURL) {
		return (KnowledgeObject) aBase.getObjectByAttribute(aType, aKey, anURL);
	}

	static Document createDocument(WebFolder folder, String docName, BinaryData content) {
		assertNull("There already is a document '" + docName + "' in folder '" + folder + "'",
			childByName(folder, docName));
		return folder.createOrUpdateDocument(docName, content);
	}

	static TLObject childByName(WebFolder folder, String childName) {
		return folder.getChildByName(childName);
	}

	private static WebFolder createChildFolder(final WebFolder folder, String folderName) {
		assertNull("There already is a folder '" + folderName + "' in folder '" + folder + "'",
			childByName(folder, folderName));
		return folder.getOrCreateChildFolder(folderName);
	}

	/**
	 * Return the suite of tests to perform.
	 */
	public static Test suite() {
		TestFactory starter =
			ServiceTestSetup.createStarterFactoryForModules(
				MimeTypes.Module.INSTANCE, WebFolderFactory.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(TestWebFolder.class, starter);
	}

}
