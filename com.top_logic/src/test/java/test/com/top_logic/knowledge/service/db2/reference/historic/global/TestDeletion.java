/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference.historic.global;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Transaction;

/**
 * Tests that the deletion of a current object does not affect references pointing to a historic
 * variant of that object.
 *
 * <p>
 * The value of a {@link HistoryType#HISTORIC} reference is stabilized to a concrete revision when it
 * is stored. Such a reference can therefore never point to a current object, which means that the
 * {@link MOReference#getDeletionPolicy() deletion policy} of a historic reference can never be
 * triggered. The deletion is allowed to skip historic references completely.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDeletion extends AbstractDBKnowledgeBaseTest {

	/**
	 * Tests that a historic reference keeps its value when the current variant of the referenced
	 * object is deleted.
	 *
	 * <p>
	 * The {@link DeletionPolicy#CLEAR_REFERENCE default deletion policy} of
	 * {@link #REFERENCE_POLY_HIST_GLOBAL_NAME} must not clear the stabilized value.
	 * </p>
	 */
	public void testHistoricReferenceUnaffectedByDeletion() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = newD("d1");
		createTx.commit();

		Transaction createRefererTx = begin();
		KnowledgeObject referer = newE("e1");
		referer.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, reference);
		createRefererTx.commit();

		KnowledgeItem stabilizedValue =
			HistoryUtils.getKnowledgeItem(createRefererTx.getCommitRevision(), reference);
		assertEquals(stabilizedValue, referer.getAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME));

		Transaction deleteTx = begin();
		reference.delete();
		deleteTx.commit();

		assertFalse(reference.isAlive());
		assertTrue("The referer must not be deleted, since its reference is a historic one.",
			referer.isAlive());
		assertEquals("The stabilized value of a historic reference must not be cleared.",
			stabilizedValue, referer.getAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME));
	}

	/**
	 * Same as {@link #testHistoricReferenceUnaffectedByDeletion()}, but the referenced object and
	 * the referer are deleted, resp. created within the same transaction. In that case the deletion
	 * searches for referers of multiple objects at once.
	 */
	public void testHistoricReferenceUnaffectedByBulkDeletion() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference1 = newD("d1");
		KnowledgeObject reference2 = newD("d2");
		createTx.commit();

		Transaction createRefererTx = begin();
		KnowledgeObject referer = newE("e1");
		referer.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, reference1);
		createRefererTx.commit();

		KnowledgeItem stabilizedValue =
			HistoryUtils.getKnowledgeItem(createRefererTx.getCommitRevision(), reference1);

		Transaction deleteTx = begin();
		reference1.delete();
		reference2.delete();
		deleteTx.commit();

		assertFalse(reference1.isAlive());
		assertFalse(reference2.isAlive());
		assertTrue("The referer must not be deleted, since its reference is a historic one.",
			referer.isAlive());
		assertEquals("The stabilized value of a historic reference must not be cleared.",
			stabilizedValue, referer.getAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME));
	}

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestDeletion.class);
	}

}
