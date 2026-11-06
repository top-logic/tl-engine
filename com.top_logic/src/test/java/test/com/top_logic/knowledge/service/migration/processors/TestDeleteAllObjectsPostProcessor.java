/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.migration.processors;

import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.migration.processors.DeleteAllObjectsPostProcessor;

/**
 * Test of {@link DeleteAllObjectsPostProcessor}: every object of the configured table is deleted
 * through the knowledge base, with its flex data and the links it is an end of, and the objects of
 * other tables stay.
 */
public class TestDeleteAllObjectsPostProcessor extends AbstractDBKnowledgeBaseTest {

	private static final String FLEX_ATTR = "flexAttr";

	/** The objects of the table go, with their flex data and their links. */
	public void testDeletesTheObjectsOfTheTable() throws Exception {
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject d1;
		Transaction tx = begin();
		b1 = newB("b1");
		b1.setAttributeValue(FLEX_ATTR, "flex of b1");
		b2 = newB("b2");
		d1 = newD("d1");
		d1.setAttributeValue(FLEX_ATTR, "flex of d1");
		newAB(b1, d1);
		commit(tx);
		ObjectKey b1Key = b1.tId();
		ObjectKey d1Key = d1.tId();
		assertTrue("Precondition: b1 has flex data.", hasFlexData(b1Key));
		assertTrue("Precondition: b1 is linked to d1.", d1.getIncomingAssociations(AB_NAME).hasNext());

		BufferingProtocol log = postProcess(B_NAME);

		assertTrue("The deletion is reported: " + log.getInfos(),
			log.getInfos().stream().anyMatch(info -> info.contains("Deleted 2 object(s) from table '" + B_NAME + "'.")));
		assertFalse("b1 is deleted.", b1.isAlive());
		assertFalse("b2 is deleted.", b2.isAlive());
		assertTrue("Nothing of the table is left.", kb().getAllKnowledgeObjects(B_NAME).isEmpty());
		assertFalse("The flex data of b1 is deleted with it.", hasFlexData(b1Key));
		assertFalse("The link of b1 is deleted with it.", d1.getIncomingAssociations(AB_NAME).hasNext());

		assertTrue("An object of another table stays.", d1.isAlive());
		assertEquals("flex of d1", d1.getAttributeValue(FLEX_ATTR));
		assertTrue("The flex data of another table stays.", hasFlexData(d1Key));

		BufferingProtocol again = postProcess(B_NAME);
		assertTrue("An empty table is no error: " + again.getErrors(), again.getErrors().isEmpty());
		assertTrue("Reported as nothing deleted: " + again.getInfos(),
			again.getInfos().stream().anyMatch(info -> info.contains("Deleted 0 object(s)")));
	}

	private BufferingProtocol postProcess(String table) throws DataObjectException {
		DeleteAllObjectsPostProcessor.Config<?> config =
			TypedConfiguration.newConfigItem(DeleteAllObjectsPostProcessor.Config.class);
		config.update(config.descriptor().getProperty(DeleteAllObjectsPostProcessor.Config.TABLE), table);
		DeleteAllObjectsPostProcessor processor = TypedConfigUtil.createInstance(config);

		BufferingProtocol log = new BufferingProtocol();
		// A post processor runs in a transaction it does not open itself.
		Transaction tx = begin();
		processor.afterMigration(log, kb());
		commit(tx);
		return log;
	}

	private boolean hasFlexData(ObjectKey key) throws SQLException {
		ConnectionPool pool = ((DBKnowledgeBase) kb()).getConnectionPool();
		PooledConnection connection = pool.borrowReadConnection();
		try (ResultSet flexData = getFlexData(connection, key)) {
			return flexData.next();
		} finally {
			pool.releaseReadConnection(connection);
		}
	}

	/**
	 * Suite of {@link TestDeleteAllObjectsPostProcessor}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestDeleteAllObjectsPostProcessor.class);
	}

}
