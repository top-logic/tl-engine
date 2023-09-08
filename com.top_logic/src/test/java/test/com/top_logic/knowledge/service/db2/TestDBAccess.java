/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.sql.SQLException;

import junit.framework.Test;

import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBAccess;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;

/**
 * {@link TestDBAccess} tests {@link DBAccess}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDBAccess extends AbstractDBKnowledgeBaseTest {

	/**
	 * Tests fetching unversioned objects.
	 */
	public void testFetchingUnversionedObjects() throws DataObjectException, SQLException {
		Transaction createTx = begin();
		KnowledgeObject u = newU("u1");
		commit(createTx);

		Transaction changeTx = begin();
		setA1(u, "u1_new");
		commit(changeTx);

		MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) type(u.tTable().getName());
		assertTrue("Test fetching objects of unversioned type.", !type.isVersioned());
		DBAccess dbAccess = type.getDBAccess();

		long branchId = trunk().getBranchId();
		ConnectionPool pool = kb().getConnectionPool();
		TLID id = u.getObjectName();
		PooledConnection con = pool.borrowReadConnection();
		try {
			KnowledgeItem u1_current_current =
				dbAccess.fetch(kb(), con, branchId, id, Revision.CURRENT_REV, Revision.CURRENT_REV);
			assertTrue(HistoryUtils.isCurrent(u1_current_current));
			assertEquals("u1_new", u1_current_current.getAttributeValue(A1_NAME));

		} finally {
			pool.releaseReadConnection(con);
		}

	}
	
	/**
	 * Tests fetching objects via {@link DBAccess}.
	 */
	public void testFetchingObjects() throws DataObjectException, SQLException {
		Transaction createTx = begin();
		KnowledgeObject b = newB("a1");
		commit(createTx);
		long r1 = createTx.getCommitRevision().getCommitNumber();

		Transaction changeTx = begin();
		setA1(b, "a1_new");
		commit(changeTx);
		long r2 = changeTx.getCommitRevision().getCommitNumber();
		
		DBAccess dbAccess = getDBAccess(b.tTable().getName());
		long branchId = trunk().getBranchId();
		ConnectionPool pool = kb().getConnectionPool();
		TLID id = b.getObjectName();
		PooledConnection con = pool.borrowReadConnection();
		try {
			try {
				dbAccess.fetch(kb(), con, branchId, id, r1, r2);
				fail("Must not fetch object with newer data.");
			} catch (Exception ex) {
				// expected
			}

			/* The fetched item is actually not a new item created from the data in the database,
			 * because the DBAccess may return the formerly cached copy, updated with the values
			 * from the database (if they are newer). */
//			KnowledgeItem b1_r1_current = dbAccess.fetch(kb(), con, branchId, Revision.CURRENT_REV, r1, filter);
//			assertTrue(HistoryUtils.isCurrent(b1_r1_current));
			// Items are accessed in session revision
			// updateSessionRevision(r1);
//			assertEquals("a1", b1_r1_current.getAttributeValue(A1_NAME));

			KnowledgeItem b1_r1_old = dbAccess.fetch(kb(), con, branchId, id, r1, r1);
			assertEquals(r1, b1_r1_old.getHistoryContext());
			assertEquals("a1", b1_r1_old.getAttributeValue(A1_NAME));

			// Items are accessed in session revision
			updateSessionRevision();
			KnowledgeItem b1_r2_current = dbAccess.fetch(kb(), con, branchId, id, Revision.CURRENT_REV, r2);
			assertTrue(HistoryUtils.isCurrent(b1_r2_current));
			assertEquals("a1_new", b1_r2_current.getAttributeValue(A1_NAME));

			KnowledgeItem b1_r2_old = dbAccess.fetch(kb(), con, branchId, id, r2, r2);
			assertEquals(r2, b1_r2_old.getHistoryContext());
			assertEquals("a1_new", b1_r2_old.getAttributeValue(A1_NAME));
		} finally {
			pool.releaseReadConnection(con);
		}

	}

	private DBAccess getDBAccess(String typeName) {
		MOKnowledgeItemImpl type = (MOKnowledgeItemImpl) type(typeName);
		return type.getDBAccess();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDBAccess}.
	 */
	public static Test suite() {
		return suite(TestDBAccess.class);
	}

}
