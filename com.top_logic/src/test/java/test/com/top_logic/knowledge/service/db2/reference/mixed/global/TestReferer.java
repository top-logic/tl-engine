/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference.mixed.global;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleQuery;

/**
 * Tests getting referee given by {@link DBKnowledgeBase#getAnyReferer(KnowledgeItem)}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferer extends AbstractDBKnowledgeBaseTest {

	private String getAttribute() {
		return getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
	}

	public void testGetCurrentReferences() throws DataObjectException {
		KnowledgeItem reference;
		KnowledgeItem stableReference;
		{
			Transaction createReferenceTx = begin();
			reference = newD("d1");
			commit(createReferenceTx);
			stableReference = HistoryUtils.getKnowledgeItem(createReferenceTx.getCommitRevision(), reference);
		}

		Transaction createRefereeTX = begin();
		KnowledgeObject currMatch = newE("e1");
		KnowledgeObject notCurrMatch = newE("e1");
		commit(createRefereeTX);

		CompiledQuery<KnowledgeObject> currentQuery;
		{
			Expression search = isCurrent(reference(E_NAME, getAttribute()));
			SimpleQuery<KnowledgeObject> simpleQuery = SimpleQuery.queryUnresolved(type(E_NAME), search);
			currentQuery = kb().compileSimpleQuery(simpleQuery);
		}

		CompiledQuery<KnowledgeObject> notCurrentQuery;
		{
			Expression search = not(isCurrent(reference(E_NAME, getAttribute())));
			SimpleQuery<KnowledgeObject> simpleQuery = SimpleQuery.queryUnresolved(type(E_NAME), search);
			notCurrentQuery = kb().compileSimpleQuery(simpleQuery);
		}

		Transaction tx1 = begin();
		currMatch.setAttributeValue(getAttribute(), reference);
		notCurrMatch.setAttributeValue(getAttribute(), stableReference);
		// search in TX
		assertEquals(list(currMatch), currentQuery.search());
		assertEquals(list(notCurrMatch), notCurrentQuery.search());
		tx1.commit();
		
		// search without TX
		assertEquals(list(currMatch), currentQuery.search());
		assertEquals(list(notCurrMatch), notCurrentQuery.search());
		
		// invert values and therefore results.
		Transaction tx2 = begin();
		currMatch.setAttributeValue(getAttribute(), stableReference);
		notCurrMatch.setAttributeValue(getAttribute(), reference);
		
		// search in TX
		assertEquals(list(notCurrMatch), currentQuery.search());
		assertEquals(list(currMatch), notCurrentQuery.search());
		
		// check that searching history in transaction does not include changes in transaction.
		Revision oldRevision = tx1.getCommitRevision();
		assertEquals(list(HistoryUtils.getKnowledgeItem(oldRevision, currMatch)),
			currentQuery.search( revisionArgs().setRequestedRevision(oldRevision.getCommitNumber())));
		assertEquals(list(HistoryUtils.getKnowledgeItem(oldRevision, notCurrMatch)),
			notCurrentQuery.search(revisionArgs().setRequestedRevision(oldRevision.getCommitNumber())));
		
		tx2.commit();
	}

	public void testGetHistoricRefereesOnHistoricObject() throws DataObjectException {
		KnowledgeItem reference;
		{
			Transaction createReferenceTx = begin();
			reference = newD("d1");
			commit(createReferenceTx);
			reference = HistoryUtils.getKnowledgeItem(createReferenceTx.getCommitRevision(), reference);
		}

		Transaction createRefereeTX = begin();
		KnowledgeObject referee = newE("e1");
		referee.setAttributeValue(getAttribute(), reference);
		commit(createRefereeTX);

		Transaction changeTx = begin();
		referee.setAttributeValue(A2_NAME, "a2");
		commit(changeTx);

		List<KnowledgeItem> referees = kb().getAnyReferer(reference);
		assertNotNull(referees);
		assertEquals(1, referees.size());
		assertEquals(referee, referees.get(0));

		Revision revision = createRefereeTX.getCommitRevision();
		List<KnowledgeItem> historicReferees = kb().getAnyReferer(revision, reference);
		assertEquals(list(HistoryUtils.getKnowledgeItem(revision, referee)), historicReferees);
	}

	public void testGetHistoricRefereesOnCurrentObject() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referee = newE("e1");
		referee.setAttributeValue(getAttribute(), reference);
		commit(createTX);

		Transaction changeTx = begin();
		referee.setAttributeValue(A2_NAME, "a2");
		commit(changeTx);

		List<KnowledgeItem> referees = kb().getAnyReferer(reference);
		assertNotNull(referees);
		assertEquals(1, referees.size());
		assertEquals(referee, referees.get(0));

		Revision revision = createTX.getCommitRevision();
		List<KnowledgeItem> historicReferees =
			kb().getAnyReferer(revision, HistoryUtils.getKnowledgeItem(revision, reference));
		assertEquals(list(HistoryUtils.getKnowledgeItem(revision, referee)), historicReferees);
	}

	public static Test suite() {
		return suiteDefaultDB(TestReferer.class);
	}
}


