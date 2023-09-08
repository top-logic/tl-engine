/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference.historic.global;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Tests getting referer given by
 * {@link DBKnowledgeBase#getAnyReferer(KnowledgeItem)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferer extends AbstractDBKnowledgeBaseTest {

	public void testGetReferer0() throws DataObjectException {
		Transaction createTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		createTX.commit();

		Transaction createRefTX = kb().beginTransaction();
		KnowledgeObject referer = newE("e1");
		referer.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, reference);
		createRefTX.commit();
		KnowledgeItem historicReference = HistoryUtils.getKnowledgeItem(createRefTX.getCommitRevision(), reference);

		List<KnowledgeItem> referers = kb().getAnyReferer(historicReference);
		assertNotNull(referers);
		assertEquals(1, referers.size());
		assertEquals(referer, referers.get(0));
	}

	public void testGetReferer2() throws DataObjectException {
		Transaction createTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		createTX.commit();

		Transaction createRef1TX = kb().beginTransaction();
		KnowledgeObject referer1 = newF("f1");
		referer1.setAttributeValue(REFERENCE_POLY_HIST_GLOBAL_NAME, reference);
		createRef1TX.commit();
		KnowledgeItem historicReference = HistoryUtils.getKnowledgeItem(createRef1TX.getCommitRevision(), reference);

		Transaction createRef2TX = kb().beginTransaction();
		KnowledgeObject referer2 = newE("e1");
		setReference(referer2, historicReference, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		createRef2TX.commit();

		List<KnowledgeItem> referers = kb().getAnyReferer(historicReference);
		assertNotNull(referers);
		assertEquals(2, referers.size());
		assertEquals(set(referer1, referer2), toSet(referers));
	}

	public void testGetReferer1() throws DataObjectException {
		Transaction createTX = kb().beginTransaction();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referer = newE("e1");
		setReference(referer, reference, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		createTX.commit();
		KnowledgeItem historicReference;
		{
			Revision commitRevision = createTX.getCommitRevision();
			historicReference = HistoryUtils.getKnowledgeItem(commitRevision, reference);
		}

		// Historic references stabilize the object to the referer must be derived from the historic
		// object
		List<KnowledgeItem> referers = kb().getAnyReferer(historicReference);
		assertNotNull(referers);
		assertEquals(1, referers.size());
		assertEquals(referer, referers.get(0));
	}

	public static Test suite() {
		return suiteDefaultDB(TestReferer.class);
	}

}

