/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseMigrationTest;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer.Index;
import com.top_logic.knowledge.service.db2.migration.rewriters.IndexerImpl;

/**
 * Test for {@link IndexerImpl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestIndexerImpl extends AbstractDBKnowledgeBaseMigrationTest {

	private IndexerImpl _indexer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		IndexerImpl.Config config = TypedConfiguration.newConfigItem(IndexerImpl.Config.class);
		config.setImplementationClass(IndexerImpl.class);
		_indexer = (IndexerImpl) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	public void testIndexer() throws DataObjectException {
		Transaction tx = kb().beginTransaction();

		KnowledgeObject b1 = newB("b1");
		KnowledgeObject b2 = newB("b2");
		KnowledgeObject c1 = newC("c1");
		c1.setAttributeValue(C1_NAME, "val1");
		KnowledgeObject c2 = newC("c1");
		c2.setAttributeValue(C1_NAME, "val2");
		tx.commit();

		Index bSelfIndex = _indexer.register(B_NAME, list(A1_NAME), list(Indexer.SELF_ATTRIBUTE));
		Index cIndex = _indexer.register(C_NAME, list(A1_NAME), list(C1_NAME));

		doMigration(list(_indexer), tx.getCommitRevision());

		assertEquals(node2Key(b1), bSelfIndex.getValue("b1"));
		assertEquals(node2Key(b2), bSelfIndex.getValue("b2"));
		List<Object[]> multiValues = cIndex.getMultiValues("c1");
		assertEquals(set("val1", "val2"), set(multiValues.get(0)[0], multiValues.get(1)[0]));

		tx = kb().beginTransaction();
		b1.delete();
		b2.setAttributeValue(A1_NAME, "b3");
		c1.setAttributeValue(A1_NAME, "c2");
		tx.commit();

		doMigration(list(_indexer), tx.getCommitRevision());

		assertEquals(node2Key(b1), bSelfIndex.getValue("b1"));
		flushDeletions();
		assertNull(bSelfIndex.getValue("b1"));

		assertEquals(node2Key(b2), bSelfIndex.getValue("b3"));
		assertNull(bSelfIndex.getValue("b2"));

	}

	private void flushDeletions() {
		int rev = -1;
		ChangeSet changeSet = new ChangeSet(rev);
		changeSet.setCommit(new CommitEvent(rev, "", System.currentTimeMillis(), "No MSG"));
		_indexer.rewrite(changeSet, new CachingEventWriter());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestIndexerImpl}.
	 */
	public static Test suite() {
		return suite(TestIndexerImpl.class);
	}

}

