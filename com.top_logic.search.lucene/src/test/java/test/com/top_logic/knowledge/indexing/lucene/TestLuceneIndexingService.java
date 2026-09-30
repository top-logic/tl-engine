/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.indexing.lucene;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.LuceneSearchTestSetup;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.indexing.DefaultIndexingService.DefaultIndexingServiceConfig;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.indexing.lucene.LuceneIndexingService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the {@link LuceneIndexingService} re-indexes the owner of an attribute value that is
 * stored in a separate table.
 */
@SuppressWarnings("javadoc")
public class TestLuceneIndexingService extends BasicTestCase {

	private static final String MODULE = "test.com.top_logic.knowledge.indexing.lucene.TestLuceneIndexingService";

	private static final String TABLE = "GenericObject";

	/** Name of the configuration property {@link DefaultIndexingServiceConfig#getMetaObjects()}. */
	private static final String META_OBJECTS = "meta-objects";

	private static final String NAME = "name";

	private static final String TITLE = "title";

	private KnowledgeBase _kb;

	private LuceneIndexingService _indexer;

	private TLModule _module;

	private TLObject _doc;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = beginTX()) {
			DynamicModelService.extendModel(new AssertProtocol(), ModelService.getApplicationModel(),
				ModelService.getInstance().getFactory(),
				ClassRelativeBinaryContent.withSuffix(TestLuceneIndexingService.class, "model.xml"));
			tx.commit();
		}
		_module = TLModelUtil.findModule(MODULE);

		DefaultIndexingServiceConfig config = TypedConfiguration.newConfigItem(DefaultIndexingServiceConfig.class);
		config.update(config.descriptor().getProperty(META_OBJECTS), List.of(TABLE));
		_indexer = new LuceneIndexingService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		_kb.addUpdateListener(_indexer);
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_indexer);
		try (Transaction tx = beginTX()) {
			if (_doc != null && _doc.tValid()) {
				_doc.tDelete();
			}
			_module.tDelete();
			tx.commit();
		}
		waitIndexFinished();
		super.tearDown();
	}

	/**
	 * Changing only a translation re-indexes the owner, although its own row is not changed.
	 */
	public void testI18NChange() throws Exception {
		TLClass type = (TLClass) _module.getType("Doc");
		try (Transaction tx = beginTX()) {
			_doc = DynamicModelService.getInstance().createObject(type);
			_doc.tUpdateByName(NAME, "doc");
			_doc.tUpdateByName(TITLE, i18n("Initialtitle"));
			tx.commit();
		}
		waitIndexFinished();
		assertContents(_doc.tId(), "initialtitle", "replacedtitle");

		try (Transaction tx = beginTX()) {
			_doc.tUpdateByName(TITLE, i18n("Replacedtitle"));
			tx.commit();
		}
		waitIndexFinished();
		assertContents(_doc.tId(), "replacedtitle", "initialtitle");
	}

	private void assertContents(ObjectKey key, String expected, String unexpected) throws IOException {
		String contents = indexedContents(key).toLowerCase(Locale.ROOT);
		assertTrue("Index does not contain '" + expected + "': " + contents, contents.contains(expected));
		assertFalse("Index still contains '" + unexpected + "': " + contents, contents.contains(unexpected));
	}

	private String indexedContents(ObjectKey key) throws IOException {
		String id = IdentifierUtil.toExternalForm(key.getObjectName());
		IndexSearcher searcher = LuceneIndex.getInstance().getSearcher();
		try {
			TopDocs hits = searcher.search(new TermQuery(new Term(LuceneIndex.FIELD_KO_ID, id)), 10);
			assertEquals("Object not indexed exactly once.", 1, hits.scoreDocs.length);
			Document document = searcher.doc(hits.scoreDocs[0].doc);
			return document.get(LuceneIndex.FIELD_CONTENTS);
		} finally {
			searcher.getIndexReader().close();
		}
	}

	private static ResKey i18n(String text) {
		ResKey.Builder builder = ResKey.builder();
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			builder.add(locale, text);
		}
		return builder.build();
	}

	private Transaction beginTX() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	private static void waitIndexFinished() throws InterruptedException {
		LuceneIndex index = LuceneIndex.getInstance();
		for (int n = 0; n < 300 && !index.isIdle(); n++) {
			Thread.sleep(100);
		}
		assertEquals("Indexing did not finish.", 0, index.queueSizes());
	}

	public static Test suite() {
		Test test = new TestSuite(TestLuceneIndexingService.class);
		test = ServiceTestSetup.createSetup(test, ModelService.Module.INSTANCE);
		return LuceneSearchTestSetup.createSearchTestSetup(test);
	}

}
