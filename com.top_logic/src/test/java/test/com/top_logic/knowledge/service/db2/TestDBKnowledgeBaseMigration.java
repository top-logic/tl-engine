/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test for migration of {@link KnowledgeBase}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDBKnowledgeBaseMigration extends AbstractDBKnowledgeBaseMigrationTest {

	/**
	 * Tests that the first {@link Revision} in {@link #kbNode2()} is adapted to same data as first
	 * revision in {@link #kb()}.
	 */
	public void testCorrectFirstRevision() throws DataObjectException {
		Revision firstNode1Revision = kb().getRevision(Revision.FIRST_REV);
		Revision firstNode2Revision = kbNode2().getRevision(Revision.FIRST_REV);
		assertNotEquals("Expected first revision of node 1 and node 2 are not created in same millisecond.",
			firstNode1Revision.getDate(), firstNode2Revision.getDate());

		Transaction tx = kb().beginTransaction();
		newB("b1");
		tx.commit();

		doMigration();
		Revision firstMigratedRevision = kbNode2().getRevision(Revision.FIRST_REV);
		assertSame("Must not change revision object during migration.", firstNode2Revision, firstMigratedRevision);

		assertEquals(firstNode1Revision.getAuthor(), firstMigratedRevision.getAuthor());
		assertEquals(firstNode1Revision.getDate(), firstMigratedRevision.getDate());
	}

	/**
	 * A cumulative {@link Test} for all Tests in {@link TestDBKnowledgeBaseMigration}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestDBKnowledgeBaseMigration.class, "");
		}
		return suite(TestDBKnowledgeBaseMigration.class);
	}

}

