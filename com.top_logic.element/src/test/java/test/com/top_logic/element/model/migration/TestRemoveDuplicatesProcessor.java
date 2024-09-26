/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.model.migration.model.refactor.RemoveDuplicatesProcessor;
import com.top_logic.element.model.migration.model.refactor.RemoveDuplicatesProcessor.Config;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.migration.MigrationContext;

/**
 * Test case for {@link RemoveDuplicatesProcessor}.
 */
@SuppressWarnings("javadoc")
public class TestRemoveDuplicatesProcessor extends AbstractDBKnowledgeBaseClusterTest {

	public void testMigration() throws SQLException {
		KnowledgeObject source;
		KnowledgeObject dest1;
		KnowledgeObject dest2;
		try (Transaction tx = kb().beginTransaction()) {
			source = newE("src1");
			dest1 = newE("dst1");
			dest2 = newE("dst2");
			tx.commit();
		}

		KnowledgeAssociation link1;
		try (Transaction tx = kb().beginTransaction()) {
			link1 = newAB(source, dest1);
			tx.commit();
		}

		try (Transaction tx = kb().beginTransaction()) {
			newAB(source, dest2);
			tx.commit();
		}

		try (Transaction tx = kb().beginTransaction()) {
			newAB(source, dest2);
			tx.commit();
		}

		Revision duplicateRevision;
		try (Transaction tx = kb().beginTransaction()) {
			newAB(source, dest1);
			tx.commit();
			
			duplicateRevision = tx.getCommitRevision();
		}

		try (Transaction tx = kb().beginTransaction()) {
			link1.delete();
			tx.commit();
		}

		// Check that there is a duplicate assignment in the designated revision.
		KnowledgeObject inRev = (KnowledgeObject) kb().resolveObjectKey(new DefaultObjectKey(source.getBranchContext(),
			duplicateRevision.getCommitNumber(), source.tTable(), source.getIdentifier()));
		List<KnowledgeObject> destinations = resolveABs(inRev);
		assertEquals(4, destinations.size());
		assertEquals(2, new HashSet<>(destinations).size());

		Config<?> config = TypedConfiguration.newConfigItem(RemoveDuplicatesProcessor.Config.class);
		config.setAssociationTable(AB_NAME);

		RemoveDuplicatesProcessor processor = TypedConfigUtil.createInstance(config);

		ConnectionPool pool = KBUtils.getConnectionPool(kb());
		BufferingProtocol log = new BufferingProtocol();
		PooledConnection connection = pool.borrowWriteConnection();
		try {
			MigrationContext context = new MigrationContext(log, connection) {
				@Override
				public MORepository getPersistentRepository() {
					return kb().getMORepository();
				}
			};
			processor.doMigration(context, log, connection);

			connection.commit();
		} finally {
			pool.releaseWriteConnection(connection);
		}
		
		String message = log.getInfos().stream().filter(info -> info.startsWith("Deleted ")).findFirst()
			.orElseGet(() -> "No migration log message.");

		assertContains("adjusted 1 ", message);
		assertContains("Deleted 1 ", message);

		DBKnowledgeBase kb2 = kbNode2();
		refetchNode2();
		updateSessionRevisionNode2();
		
		KnowledgeObject inRevKb2 = (KnowledgeObject) kb2.resolveObjectKey(inKb(kb2, inRev.tId()));
		List<KnowledgeObject> destinationsKb2 = resolveABs(inRevKb2);
		assertEquals(2, destinationsKb2.size());
	}

	private List<KnowledgeObject> resolveABs(KnowledgeObject obj) {
		List<KnowledgeObject> destinations = CollectionUtil.toList(obj.getOutgoingAssociations(AB_NAME))
			.stream()
			.map(link -> link.getDestinationObject())
			.toList();
		return destinations;
	}

	private static ObjectKey inKb(DBKnowledgeBase kb, ObjectKey id) {
		return new DefaultObjectKey(id.getBranchContext(), id.getHistoryContext(),
			kb.lookupType(id.getObjectType().getName()), id.getObjectName());
	}

	public static Test suite() {
		return suiteDefaultDB(TestRemoveDuplicatesProcessor.class);
	}
}
