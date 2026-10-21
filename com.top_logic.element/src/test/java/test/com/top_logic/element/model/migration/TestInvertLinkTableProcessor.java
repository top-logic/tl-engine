/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.migration;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.model.migration.model.refactor.InvertLinkTableProcessor;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.migration.MigrationContext;

/**
 * Test case for {@link InvertLinkTableProcessor}.
 */
@SuppressWarnings("javadoc")
public class TestInvertLinkTableProcessor extends AbstractDBKnowledgeBaseClusterTest {

	public void testInvertAllLinks() throws Exception {
		KnowledgeObject a;
		KnowledgeObject b;
		KnowledgeObject c;
		KnowledgeObject d;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			a = newE("a");
			b = newE("b");
			c = newE("c");
			d = newE("d");
			tx.commit();
		}

		KnowledgeAssociation linkAB;
		KnowledgeAssociation linkCD;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			linkAB = newAB(a, b);
			linkCD = newAB(c, d);
			tx.commit();
		}

		InvertLinkTableProcessor.Config<?> config =
			TypedConfiguration.newConfigItem(InvertLinkTableProcessor.Config.class);
		config.setTable(AB_NAME);

		InvertLinkTableProcessor processor = TypedConfigUtil.createInstance(config);

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

		assertFalse("Migration reported errors: " + log.getErrors(), log.hasErrors());

		String message = log.getInfos().stream().filter(info -> info.startsWith("Inverted ")).findFirst()
			.orElseGet(() -> "No migration log message.");
		assertContains("Inverted 2 links in table '" + AB_NAME + "'", message);

		DBKnowledgeBase kb2 = kbNode2();
		refetchNode2();
		updateSessionRevisionNode2();

		assertInverted(kb2, linkAB, a, b);
		assertInverted(kb2, linkCD, c, d);
	}

	private void assertInverted(DBKnowledgeBase kb2, KnowledgeAssociation link, KnowledgeObject oldSource,
			KnowledgeObject oldDest) throws Exception {
		KnowledgeAssociation linkKb2 = (KnowledgeAssociation) kb2.resolveObjectKey(inKb(kb2, link.tId()));
		assertNotNull("Link not found in second node.", linkKb2);

		Object newSource = linkKb2.getSourceObject().getObjectName();
		Object newDest = linkKb2.getDestinationObject().getObjectName();
		String state = "Link " + link.getObjectName() + " (" + oldSource.getAttributeValue(A1_NAME) + " -> "
			+ oldDest.getAttributeValue(A1_NAME) + ") after inversion: "
			+ linkKb2.getSourceObject().getAttributeValue(A1_NAME) + " -> "
			+ linkKb2.getDestinationObject().getAttributeValue(A1_NAME);
		assertEquals(state, oldDest.getObjectName(), newSource);
		assertEquals(state, oldSource.getObjectName(), newDest);
	}

	private static ObjectKey inKb(DBKnowledgeBase kb, ObjectKey id) {
		return new DefaultObjectKey(id.getBranchContext(), id.getHistoryContext(),
			kb.lookupType(id.getObjectType().getName()), id.getObjectName());
	}

	public static Test suite() {
		// The MigrationContext built here takes its branch support from the application
		// configuration, which a setup without branches does not adjust.
		return suiteNeedsBranches(TestInvertLinkTableProcessor.class);
	}
}
