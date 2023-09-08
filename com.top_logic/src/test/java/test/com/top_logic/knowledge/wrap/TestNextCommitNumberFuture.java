/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;

/**
 * The class {@link TestNextCommitNumberFuture} tests the {@link NextCommitNumberFuture}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestNextCommitNumberFuture extends AbstractDBKnowledgeBaseTest {

	public void testAsRowValue() throws DataObjectException {
		// X8_NAME is a Long attribute
		String attributeName = X8_NAME;
		testStoreInAttribute(attributeName);
	}

	private void testStoreInAttribute(String attributeName) throws DataObjectException, NoSuchAttributeException,
			KnowledgeBaseException {
		Transaction tx = begin();
		KnowledgeObject x = newX("x1");
		x.setAttributeValue(attributeName, NextCommitNumberFuture.INSTANCE);
		assertEquals(NextCommitNumberFuture.INSTANCE, x.getAttributeValue(attributeName));
		tx.commit();

		assertEquals(tx.getCommitRevision().getCommitNumber(), x.getAttributeValue(attributeName));

		Transaction changeTX = begin();
		setX10(x, "newX1");
		changeTX.commit();

		assertEquals("Commit number must not change after different commit.", tx.getCommitRevision().getCommitNumber(),
			x.getAttributeValue(attributeName));
	}

	public void testAsFlexValue() throws DataObjectException {
		String attributeName = "myFunnyFlexAttribute";
		testStoreInAttribute(attributeName);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestNextCommitNumberFuture}.
	 */
	public static Test suite() {
		return suite(TestNextCommitNumberFuture.class);
	}

}
