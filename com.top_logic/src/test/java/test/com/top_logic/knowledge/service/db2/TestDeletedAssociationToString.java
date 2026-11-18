/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;

/**
 * Test for the string representation of a {@link KnowledgeAssociation} that is not alive in the
 * accessing context, and for the message of the {@link DeletedObjectAccess} reported when accessing
 * its data.
 */
@SuppressWarnings("javadoc")
public class TestDeletedAssociationToString extends AbstractDBKnowledgeBaseTest {

	public void testAliveLinkShowsSourceAndDestination() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject d1 = newD("d1");
		KnowledgeAssociation link = newAB(b1, d1);
		commit(tx);

		String linkString = link.toString();
		assertContains(identifier(link), linkString);
		assertContains(b1.tId() + "->" + d1.tId(), linkString);
	}

	public void testDeletedLink() throws DataObjectException {
		KnowledgeAssociation link = createLink();

		Transaction tx = begin();
		link.delete();
		commit(tx);

		assertFalse(link.isAlive());
		assertNotAliveLink(link);

		try {
			link.getSourceIdentity();
			fail("Data of a link must not be accessible after its deletion: " + link);
		} catch (DeletedObjectAccess ex) {
			assertContains(identifier(link), ex.getMessage());
		}
	}

	public void testLinkDeletedInOpenTransaction() throws DataObjectException {
		KnowledgeAssociation link = createLink();

		Transaction tx = begin();
		link.delete();
		try {
			assertFalse(link.isAlive());
			assertNotAliveLink(link);
		} finally {
			rollback(tx);
		}
		assertTrue(link.isAlive());
	}

	public void testNewLinkDeletedInOpenTransaction() throws DataObjectException {
		Transaction tx = begin();
		try {
			KnowledgeAssociation link = newAB(newB("b1"), newD("d1"));
			link.delete();

			assertFalse(link.isAlive());
			assertContains(identifier(link), link.toString());
		} finally {
			rollback(tx);
		}
	}

	private KnowledgeAssociation createLink() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeAssociation link = newAB(newB("b1"), newD("d1"));
		commit(tx);
		return link;
	}

	private static void assertNotAliveLink(KnowledgeAssociation link) {
		String linkString = link.toString();
		assertContains(identifier(link), linkString);

		try {
			link.getAttributeValue(AB1_NAME);
			fail("Data of a link must not be accessible after its deletion: " + linkString);
		} catch (DeletedObjectAccess ex) {
			assertContains(identifier(link), ex.getMessage());
		} catch (Exception ex) {
			throw new AssertionError("Unexpected failure accessing a deleted link: " + linkString, ex);
		}
	}

	private static String identifier(KnowledgeAssociation link) {
		return link.tId().toString();
	}

	public static Test suite() {
		return suite(TestDeletedAssociationToString.class);
	}

}
