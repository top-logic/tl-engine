/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.diff;

import java.sql.SQLException;

import junit.framework.Test;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.diff.AbstractDiffDeletionQuery.DiffDeletionResult;
import com.top_logic.knowledge.service.db2.diff.AbstractDiffUpdateQuery.DiffUpdateResult;
import com.top_logic.knowledge.service.db2.diff.DiffRowAttributesQuery;
import com.top_logic.knowledge.service.db2.diff.DiffRowDeletionQuery;
import com.top_logic.util.TLContext;

/**
 * Test case for {@link DiffRowAttributesQuery} and {@link DiffRowDeletionQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestDiffRowAttributesQuery extends AbstractDBKnowledgeBaseTest {

    public void testDiff() throws DataObjectException, SQLException {
    	// unmodified (blank).
    	final KnowledgeObject b1;
    	// null -> non null change
    	final KnowledgeObject b2;
    	// non null -> null change
    	final KnowledgeObject b3;
    	// non null -> non null change
    	final KnowledgeObject b4;
    	// unmodified (revert to orig value)
    	final KnowledgeObject b5;
    	// blank deleted
    	final KnowledgeObject b8;
    	// deleted with values.
    	final KnowledgeObject b9;
		long r1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			setA2(b3, "b3.a2");
			b4 = newB("b4");
			setA2(b4, "b4.a2");
			b5 = newB("b5");
			setA2(b5, "b5.a2 orig");
			b8 = newB("b8");
			b9 = newB("b9");
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
    	
		// newly created with value
		final KnowledgeObject b6;
		{
			Transaction tx = begin();
			setA2(b2, "b2.a2");
			setA2(b3, null);
			setA2(b4, "b4.a2 update");
			setA2(b5, "b5.a2 update");
			setA2(b9, "b9.a2");
			b6 = newB("b6");
			commit(tx);
		}
    	
		// newly created blank (all non-mandatory attributes are null).
		final KnowledgeObject b7;
		final Long r2;
		{
			Transaction tx = begin();
			setA2(b5, "b5.a2 orig");
			setA2(b6, "b6.a2");
			b7 = newB("b7");
			b8.delete();
			b9.delete();
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
    	
    	// Prevent unused warnings.
    	assertNotNull(b1);
    	assertNotNull(b7);
    	
    	// Test difference.
    	PooledConnection connection = kb().getConnectionPool().borrowReadConnection();
    	try {
			DiffRowDeletionQuery deletionQuery =
				DiffRowDeletionQuery.createDiffRowDeletionQuery(kb().getConnectionPool().getSQLDialect(), type(B_NAME),
					TLContext.TRUNK_ID, r1, TLContext.TRUNK_ID, r2);
    		DiffDeletionResult deletionResult = deletionQuery.query(connection);
    		try {
    			boolean b8Seen = false;
    			boolean b9Seen = false;
				while (deletionResult.next()) {
					if (b8.getObjectName().equals(deletionResult.getObjectName())) {
						b8Seen = true;
					}
					else if (b9.getObjectName().equals(deletionResult.getObjectName())) {
						b9Seen = true;
					}
					else {
						fail("Unexpected deletion: " + deletionResult.getObjectName());
					}
				}
				assertTrue(b8Seen);
				assertTrue(b9Seen);
			} finally {
				deletionResult.close();
			}
    		
			MOClass type = type(B_NAME);
			DiffRowAttributesQuery query =
				DiffRowAttributesQuery.createDiffRowAttributesQuery(kb().getConnectionPool().getSQLDialect(), type,
					TLContext.TRUNK_ID, r1, TLContext.TRUNK_ID, r2);
    		DiffUpdateResult result = query.query(connection);
    		try {
    			boolean b2Seen = false;
    			boolean b3Seen = false;
    			boolean b4Seen = false;
    			boolean b6Seen = false;
    			boolean b7Seen = false;
    			while (result.next()) {
					ObjectContext contextObject = null;
					MOAttribute A1 = type.getAttribute(A1_NAME);
					Object a1 = result.getNewValue(A1, contextObject);
					MOAttribute A2 = type.getAttribute(A2_NAME);
					if ("b2".equals(a1)) {
    					assertNull(result.getOldValue(A2, contextObject));
    					assertEquals("b2.a2", result.getNewValue(A2, contextObject));
						b2Seen = true;
    				}
    				else if ("b3".equals(a1)) {
    					assertEquals("b3.a2", result.getOldValue(A2, contextObject));
    					assertNull(result.getNewValue(A2, contextObject));
    					b3Seen = true;
    				}
    				else if ("b4".equals(a1)) {
    					assertEquals("b4.a2", result.getOldValue(A2, contextObject));
    					assertEquals("b4.a2 update", result.getNewValue(A2, contextObject));
    					b4Seen = true;
    				}
    				else if ("b6".equals(a1)) {
    					assertTrue(result.isCreation());
    					b6Seen = true;
    				}
    				else if ("b7".equals(a1)) {
    					assertTrue(result.isCreation());
    					b7Seen = true;
    				}
    				else {
    					fail("Unexpected object: " + a1);
    				}
    			}
    			assertTrue(b2Seen);
    			assertTrue(b3Seen);
    			assertTrue(b4Seen);
    			assertTrue(b6Seen);
    			assertTrue(b7Seen);
    		} finally {
    			result.close();
    		}
		} finally {
			kb().getConnectionPool().releaseReadConnection(connection);
		}
    }

	public static Test suite() {
		return suite(TestDiffRowAttributesQuery.class);
    }

}
