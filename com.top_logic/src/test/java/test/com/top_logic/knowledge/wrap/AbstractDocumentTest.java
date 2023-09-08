/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.io.binary.TestingBinaryData;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;

/**
 * The class {@link AbstractDocumentTest} is an abstract super class for tests around
 * {@link Document}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractDocumentTest extends BasicTestCase {

	/** Base DSN (This directory is cleared before Testing) */
	private static final String TEST_BASE = "repository://";

	/** The folder to be used for tests. */
	protected WebFolder _folder;

	/** empty content set when calling {@link #getOrCreateChildDocument(String)} */
	protected BinaryData _emptyContent;

	@Override
	protected void setUp() throws Exception {
	    super.setUp ();
		_emptyContent = new TestingBinaryData(0, 10);
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		final Transaction tx = kb.beginTransaction();
		this._folder = WebFolder.createFolder(kb, getClass().getSimpleName() + "_" + getName(), TEST_BASE);
		tx.commit();
	}

	@Override
	protected void tearDown() throws Exception {
		KnowledgeBase kb = KBSetup.getKnowledgeBase();
		final Transaction tx = kb.beginTransaction();
		if (this._folder == null && !this._folder.tValid()) {
			WebFolder.deleteRecursively(this._folder);
		}
		tx.commit();

		_folder = null; // allow gc of folder
		_emptyContent = null;
	    super.tearDown();
	}

	protected Document getOrCreateChildDocument(String testName) {
		Document result = (Document) this._folder.getChildByName(testName);
		if (result == null) {
			final KnowledgeBase kb = KBSetup.getKnowledgeBase();
			final Transaction tx = kb.beginTransaction();
			result = this._folder.createOrUpdateDocument(testName, _emptyContent);
			tx.commit();
		}
		return result;
	}

	/**
	 * Returns the test file to be used for updating a document.
	 * 
	 * @return The test file to be used.
	 */
	protected BinaryData getData(String fileName) {
		Logger.debug("Using '" + fileName + "' as test file!", this);

		return BinaryDataFactory.createBinaryData(new File(fileName));
	}

	public static void fillMandatoryDocAttributes(KnowledgeItem documentKO, long size, Object updateRevisionNumber)
			throws DataObjectException {
		documentKO.setAttributeValue((String) ReflectionUtils.getStaticValue(Document.class, "SIZE"), size);
		documentKO.setAttributeValue((String) ReflectionUtils.getStaticValue(Document.class, "UPDATE_REVISION_NUMBER"),
			updateRevisionNumber);
	}

	public static void fillMandatoryDocAttributes(KnowledgeItem documentKO) throws DataObjectException {
		fillMandatoryDocAttributes(documentKO, 0, NextCommitNumberFuture.INSTANCE);
	}

	public static KnowledgeObject createDocumentKO(KnowledgeBase kb, String name) throws DataObjectException {
		final KnowledgeObject resultKO = kb.createKnowledgeObject(Document.OBJECT_NAME);
		if (name != null) {
			resultKO.setAttributeValue("name", name);
		}
		fillMandatoryDocAttributes(resultKO);
		return resultKO;
	}

	/**
	 * Tests with {@link KnowledgeBase} without versioning will fail. Documents needed versioning.
	 * So just default KB is tested will be tested
	 */
	protected static Test singleKBSsuite(Class<? extends AbstractDocumentTest> testClass) {
		return KBSetup.getKBTest(testClass, KBSetup.DEFAULT_KB, AbstractDocumentTest.setupModulesForDocuments());
	}

	/**
	 * Creates a factory to start modules needed to use documents
	 */
	public static TestFactory setupModulesForDocuments() {
		TestFactory f = new TestFactory() {

			@Override
			public Test createSuite(Class<? extends Test> testCase, String suiteName) {
				TestSuite testSuite = new TestSuite(suiteName);
				testSuite.addTestSuite((Class<? extends TestCase>) testCase);
				return testSuite;
			}
		};
		f =
			ServiceTestSetup
				.createStarterFactoryForModules(f, PersistencyLayer.Module.INSTANCE, MimeTypes.Module.INSTANCE);
		return f;
	}

}

