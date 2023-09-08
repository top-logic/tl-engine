/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Stack;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.KIReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBTypeRepository;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCompileProtocol;
import com.top_logic.knowledge.service.db2.expr.visit.SimpleExpressionEvaluator;
import com.top_logic.knowledge.service.db2.expr.visit.TypeBinding;

/**
 * The class {@link AbstractKIReferenceTest} contains tests for {@link KIReference}s.
 * 
 *          test.com.top_logic.knowledge.service.db2.TestMOReferenceAttributeImpl
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractKIReferenceTest extends AbstractDBKnowledgeBaseTest {

	private static int CNT = 0;

	private KnowledgeObject e;

	private Stack<Transaction> tx;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tx = new Stack<>();
		txBegin();
		e = createReferer("objectWithReferences");
		txCommit();
	}

	private KnowledgeObject createReferer(String refererName) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(refererTypeName());
		result.setAttributeValue(A1_NAME, refererName);
		return result;
	}

	/**
	 * Name of the type that has the {@link MOReference} attributes.
	 */
	protected abstract String refererTypeName();

	/**
	 * A new {@link MOReference} with the implementation class under test.
	 */
	protected abstract MOReference newReferenceAttribute(String name, MetaObject targetType);

	@Override
	protected void tearDown() throws Exception {
		e = null;
		super.tearDown();
	}

	private void txBegin() {
		this.tx.push(begin());
	}

	private Transaction txCommit() throws KnowledgeBaseException {
		final Transaction transaction = this.tx.pop();
		transaction.commit();
		return transaction;
	}

	public void testCurrentSemantikOfLiteral() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referee = newE("e1");
		String referenceAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		referee.setAttributeValue(referenceAttr, reference);
		commit(createTx);

		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttr), literal(reference))));

		assertEquals(list(referee), kb().search(query, revisionArgs()));

		Revision r1 = lastRevision();
		KnowledgeItem historicReferee = HistoryUtils.getKnowledgeItem(r1, referee);
		RevisionQueryArguments historicArgs = revisionArgs().setRequestedRevision(r1.getCommitNumber());
		assertEquals("Ticket #9603", list(historicReferee), kb().search(query, historicArgs));
	}

	public void testCurrentSemantikOfParameter() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referee = newE("e1");
		String referenceAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		referee.setAttributeValue(referenceAttr, reference);
		commit(createTx);

		SetExpression searchExpr = filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttr), param("item")));
		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(params(paramDecl(BasicTypes.ITEM_TYPE_NAME, "item")), searchExpr, NO_ORDER);

		assertEquals(list(referee), kb().search(query, revisionArgs().setArguments(reference)));

		Revision r1 = lastRevision();
		KnowledgeItem historicReferee = HistoryUtils.getKnowledgeItem(r1, referee);
		RevisionQueryArguments historicArgs =
			revisionArgs().setRequestedRevision(r1.getCommitNumber()).setArguments(reference);
		assertEquals("Ticket #9603", list(historicReferee), kb().search(query, historicArgs));
	}

	public void testSettingNonKIValue() throws DataObjectException {
		String referenceAttr = getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		Transaction createRefereeTX = begin();
		KnowledgeObject referee = newE("e1");
		try {
			referee.setAttributeValue(referenceAttr, new Object());
			fail("Must only allow KnowledgeItems as reference");
		} catch (DataObjectException ex) {
			// expected
		}
		commit(createRefereeTX);
	}

	public void testSettingWrongHistoryType() throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject reference = newD("d1");
		KnowledgeObject referee = newE("e1");
		commit(createTx);

		String currentReferenceAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		Transaction settingCurrentValueTX = begin();
		referee.setAttributeValue(currentReferenceAttr, reference);
		commit(settingCurrentValueTX);
		checkReference(referee, reference, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		KnowledgeItem historicObject =
			HistoryUtils.getKnowledgeItem(settingCurrentValueTX.getCommitRevision(), reference);

		Transaction settingHistoricValueTX = begin();
		try {
			referee.setAttributeValue(currentReferenceAttr, historicObject);
			fail("Must only allow current objects as reference");
		} catch (DataObjectException ex) {
			// expected
		}
		checkReference(referee, reference, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		commit(settingHistoricValueTX);
	}

	public void testNavigateMixedReferenceAfterBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		KnowledgeItem reference;
		{
			Transaction createReferenceTx = begin();
			reference = newD("d1");
			commit(createReferenceTx);
		}
		String referenceAttr = getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);

		KnowledgeObject referee;
		{
			Transaction createRefereeTX = begin();
			referee = newE("e1");
			referee.setAttributeValue(referenceAttr, reference);
			commit(createRefereeTX);
		}

		Branch branch = HistoryUtils.createBranch(trunk(), lastRevision());
		Revision r1 = lastRevision();
		{
			Transaction dummyTX = begin();
			newE("e1");
			commit(dummyTX);
		}

		assertEquals(reference, referee.getAttributeValue(referenceAttr));

		KnowledgeItem historicReferee = HistoryUtils.getKnowledgeItem(r1, referee);
		KnowledgeItem historicReference = HistoryUtils.getKnowledgeItem(r1, reference);
		assertEquals(historicReference, historicReferee.getAttributeValue(referenceAttr));

		KnowledgeItem refereeOnBranch = HistoryUtils.getKnowledgeItem(branch, referee);
		KnowledgeItem referenceOnBranch = HistoryUtils.getKnowledgeItem(branch, reference);
		assertEquals(referenceOnBranch, refereeOnBranch.getAttributeValue(referenceAttr));

		KnowledgeItem historicRefereeOnBranch = HistoryUtils.getKnowledgeItem(r1, refereeOnBranch);
		KnowledgeItem historicReferenceOnBranch = HistoryUtils.getKnowledgeItem(r1, referenceOnBranch);
		assertEquals(historicReferenceOnBranch, historicRefereeOnBranch.getAttributeValue(referenceAttr));

	}

	public void testSearchMixedReferenceAfterBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		KnowledgeItem reference;
		{
			Transaction createReferenceTx = begin();
			reference = newD("d1");
			commit(createReferenceTx);
		}
		String referenceAttr = getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		
		KnowledgeObject referee;
		{
			Transaction createRefereeTX = begin();
			referee = newE("e1");
			referee.setAttributeValue(referenceAttr, reference);
			commit(createRefereeTX);
		}
		
		Branch branch = HistoryUtils.createBranch(trunk(), lastRevision());
		
		SetExpression search = filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttr), param("p")));
		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(params(paramDecl(BasicTypes.ITEM_TYPE_NAME, "p")), search, NO_ORDER);

		List<KnowledgeObject> searchTrunk =
			kb().search(query, revisionArgs().setRequestedBranch(trunk()).setArguments(reference));
		assertEquals(list(referee), searchTrunk);

		KnowledgeItem refereeOnBranch = HistoryUtils.getKnowledgeItem(branch, referee);
		KnowledgeItem referenceOnBranch = HistoryUtils.getKnowledgeItem(branch, reference);
		List<KnowledgeObject> searchBranch =
			kb().search(query, revisionArgs().setRequestedBranch(branch).setArguments(referenceOnBranch));
		assertEquals(list(refereeOnBranch), searchBranch);
	}

	public void testSearchMixedReferenceInHistory() throws DataObjectException {
		KnowledgeItem reference;
		KnowledgeItem histReference;
		{
			Transaction createReferenceTx = begin();
			reference = newD("d1");
			commit(createReferenceTx);
			histReference = HistoryUtils.getKnowledgeItem(createReferenceTx.getCommitRevision(), reference);
		}
		String referenceCurrAttr = getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		String referenceHistAttr = getReferenceAttr(!MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);

		KnowledgeObject referee;
		Revision r1;
		{
			Transaction createRefereeTX = begin();
			referee = newE("e1");
			referee.setAttributeValue(referenceCurrAttr, reference);
			referee.setAttributeValue(referenceHistAttr, histReference);
			commit(createRefereeTX);
			r1 = createRefereeTX.getCommitRevision();
		}

		// searching mixed reference filled with historic object
		SetExpression histSearch = filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceHistAttr), param("p")));
		RevisionQuery<KnowledgeObject> histQuery =
			queryUnresolved(params(paramDecl(BasicTypes.ITEM_TYPE_NAME, "p")), histSearch, NO_ORDER);

		List<KnowledgeObject> searchHistCurrent = kb().search(histQuery, revisionArgs().setArguments(histReference));
		assertEquals(list(referee), searchHistCurrent);

		List<KnowledgeObject> searchHistHistory =
			kb().search(histQuery,
				revisionArgs().setRequestedRevision(r1.getCommitNumber()).setArguments(histReference));
		assertEquals(list(HistoryUtils.getKnowledgeItem(r1, referee)), searchHistHistory);

		// searching mixed reference filled with current object
		SetExpression currSearch = filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceCurrAttr), param("p")));
		RevisionQuery<KnowledgeObject> currQuery =
			queryUnresolved(params(paramDecl(BasicTypes.ITEM_TYPE_NAME, "p")), currSearch, NO_ORDER);
		
		List<KnowledgeObject> searchCurrCurrent = kb().search(currQuery, revisionArgs().setArguments(reference));
		assertEquals(list(referee), searchCurrCurrent);

		List<KnowledgeObject> searchCurrHistory =
			kb().search(currQuery, revisionArgs().setRequestedRevision(r1.getCommitNumber()).setArguments(reference));
		assertEquals("Ticket #6507: Searching mixed references with current content in history fails.",
			list(HistoryUtils.getKnowledgeItem(r1, referee)), searchCurrHistory);
	}

	public void testDeletedTarget() throws KnowledgeBaseException, DataObjectException {
		KnowledgeObject reference = createReferenceObject();

		txBegin();
		reference.delete();
		try {
			setReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			fail("Must not set deleted objects as reference");
		} catch (DataObjectException ex) {
			// expected
		}
		txCommit();
		try {
			setReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			fail("Must not set deleted objects as reference");
		} catch (DataObjectException ex) {
			// expected
		}
	}

	public void testVersionedSourceUnversionedTarget() throws KnowledgeBaseException, DataObjectException {
		KnowledgeObject reference = createReferenceObject();
		
		txBegin();
		setReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		KnowledgeObject unversionedTarget = kb().createKnowledgeObject(H_NAME);
		unversionedTarget.setAttributeValue(A1_NAME, "a1");
		txCommit();
		
		checkReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		
		Transaction begin = begin();
		try {
			assertTrue("Test checks reference from versioned to unversioned.", ((MOClass) e.tId()
				.getObjectType()).isVersioned());
			assertFalse("Test checks reference from versioned to unversioned.", ((MOClass) unversionedTarget
				.tId().getObjectType()).isVersioned());
			setReference(e, unversionedTarget, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			fail("Must not reference from versioned type to unversioned");
		} catch(DataObjectException ex) {
			// commit must fail
		}
		begin.rollback();

		// reference must remain
		checkReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
	}

	public void testFetchHistoricObjectWithCurrentReference() throws KnowledgeBaseException, DataObjectException {
		KnowledgeObject reference = createReferenceObject();

		txBegin();
		setReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		Revision r1 = txCommit().getCommitRevision();

		RevisionQuery<KnowledgeObject> findRefererQuery =
			queryUnresolved(filter(allOf(refererTypeName()), eqBinary(identifier(), literal(e.tId().getObjectName()))));
		List<KnowledgeObject> result =
			kb().search(findRefererQuery, revisionArgs().setRequestedRevision(r1.getCommitNumber()));
		assertEquals("Just one element committed", 1, result.size());
		KnowledgeObject searchResult = result.get(0);
		KnowledgeItem historicE = HistoryUtils.getKnowledgeItem(r1, e);
		assertEquals(historicE, searchResult);

		KnowledgeItem historicReference = getReference(searchResult, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		assertSame("Current reference in historic object does not live in same historic context",
			historicE.getHistoryContext(), historicReference.getHistoryContext());

	}

	public void testAccessMixedReferenceOnViews() throws DataObjectException {
		KnowledgeObject reference = createReferenceObject();

		txBegin();
		setReference(e, reference, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		Revision r1 = txCommit().getCommitRevision();
		KnowledgeItem e1 = HistoryUtils.getKnowledgeItem(r1, e);

		txBegin();
		setA2(reference, "reference_a2");
		Revision r2 = txCommit().getCommitRevision();
		KnowledgeItem e2 = HistoryUtils.getKnowledgeItem(r2, e);

		txBegin();
		setA2(e, "referee_a2");
		Revision r3 = txCommit().getCommitRevision();
		KnowledgeItem e3 = HistoryUtils.getKnowledgeItem(r3, e);

		txBegin();
		setA2(reference, "reference_newA2");
		Revision r4 = txCommit().getCommitRevision();
		KnowledgeItem e4 = HistoryUtils.getKnowledgeItem(r4, e);

		txBegin();
		setA2(e, "referee_newA2");
		Revision r5 = txCommit().getCommitRevision();
		KnowledgeItem e5 = HistoryUtils.getKnowledgeItem(r5, e);
		assertEquals(null, getReference(e1, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL).getAttributeValue(A2_NAME));
		assertEquals("reference_a2",
			getReference(e2, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL).getAttributeValue(A2_NAME));
		assertEquals("reference_a2",
			getReference(e3, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL).getAttributeValue(A2_NAME));
		assertEquals("reference_newA2", getReference(e4, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL)
			.getAttributeValue(A2_NAME));
		assertEquals("reference_newA2", getReference(e5, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL)
			.getAttributeValue(A2_NAME));

	}

	public void testCurrentReferenceRemainsCurrent() throws DataObjectException {
		KnowledgeObject reference = createReferenceObject();

		txBegin();
		setReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		txCommit();

		// produce traffic on object different to referee to get view on referee
		txBegin();
		setA2(reference, "a2");
		Revision changeRev = txCommit().getCommitRevision();

		// produce traffic on object different to referee to get view on referee
		txBegin();
		setA2(reference, "newA2");
		Revision changeRev2 = txCommit().getCommitRevision();
		KnowledgeItem almostCurrentE = HistoryUtils.getKnowledgeItem(changeRev2, e);

		KnowledgeItem oldE = HistoryUtils.getKnowledgeItem(changeRev, almostCurrentE);
		KnowledgeItem oldReference =
			getReference(oldE, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		assertEquals(changeRev.getCommitNumber(), oldReference.tId().getHistoryContext());

		assertEquals("a2", oldReference.getAttributeValue(A2_NAME));
		assertEquals("Current reference does not return current value when accessing historic value before", "newA2",
			getReference(almostCurrentE, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL)
				.getAttributeValue(A2_NAME));

	}

	public void testCurrentReferenceInHistoricReferer() throws KnowledgeBaseException, DataObjectException {
		KnowledgeObject reference = createReferenceObject();

		txBegin();
		setReference(e, reference, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		setReference(e, reference, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		txCommit();

		txBegin();
		reference.setAttributeValue(A2_NAME, "a2_1");
		Revision a2_1 = txCommit().getCommitRevision();

		txBegin();
		reference.setAttributeValue(A2_NAME, "a2_2");
		Revision a2_2 = txCommit().getCommitRevision();

		KnowledgeItem e_a2_1 = HistoryUtils.getKnowledgeItem(a2_1, e);
		assertEquals("a2_1", getAttribute(e_a2_1, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL, A2_NAME));
		assertEquals("a2_1", getAttribute(e_a2_1, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL, A2_NAME));

		KnowledgeItem e_a2_2 = HistoryUtils.getKnowledgeItem(a2_2, e);
		assertEquals("a2_2", getAttribute(e_a2_2, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL, A2_NAME));
		assertEquals("a2_2", getAttribute(e_a2_2, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL, A2_NAME));
		assertEquals("a2_2", getAttribute(e, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL, A2_NAME));
		assertEquals("a2_2", getAttribute(e, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL, A2_NAME));
	}

	Object getAttribute(KnowledgeItem referer, boolean monomorphic, HistoryType historyType, boolean branchGlobal,
			String attributeName) throws DataObjectException {
		KnowledgeItem reference = getReference(referer, monomorphic, historyType, branchGlobal);
		assertNotNull(
			"expected referer has value for reference attribute '"
				+ getReferenceAttr(monomorphic, historyType, branchGlobal) + "'", reference);
		assertInstanceof(reference, KnowledgeItem.class);
		return reference.getAttributeValue(attributeName);
	}

	public void testCurrentRefInHistoricObjHasSameRev() throws KnowledgeBaseException, DataObjectException {
		KnowledgeObject reference = createReferenceObject();

		String referenceAttrName = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		txBegin();
		e.setAttributeValue(referenceAttrName, reference);
		txCommit();
		KnowledgeItem historicE = HistoryUtils.getKnowledgeItem(lastRevision(), e);

		txBegin();
		setA2(reference, "a2");
		txCommit();

		Object historicVersionOfReference = historicE.getAttributeValue(referenceAttrName);
		assertInstanceof(historicVersionOfReference, KnowledgeItem.class);
		assertSame(historicE.getHistoryContext(), ((KnowledgeItem) historicVersionOfReference).getHistoryContext());
		assertNull(((KnowledgeItem) historicVersionOfReference).getAttributeValue(A2_NAME));
	}

	public void testSetDeletedObjectCurrent() throws KnowledgeBaseException, DataObjectException {
		// object to delete within transaction
		final KnowledgeObject referenceObject = createReferenceObject();
		Transaction deleteTx = begin();
		referenceObject.delete();
		deleteTx.commit();

		Transaction transaction = begin();
		try {
			setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
			fail("Must not set deleted Object into a Reference");
		} catch (DataObjectException ex) {
			// expected
		}
		transaction.commit();
	}

	public void testSetDeletedObjectHistoric() throws KnowledgeBaseException, DataObjectException {
		// object to delete within transaction
		final KnowledgeObject referenceObject = createReferenceObject();
		Transaction deleteTx = begin();
		referenceObject.delete();
		deleteTx.commit();

		Transaction transaction = begin();
		try {
			setReference(e, referenceObject, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
			fail("Must not set deleted Object into a Reference");
		} catch (DataObjectException ex) {
			// expected
		}
		transaction.commit();
	}

	public void testSetDeletedObjectWithinTxHistoric() throws KnowledgeBaseException, DataObjectException {
		// object to delete within transaction
		final KnowledgeObject referenceObject = createReferenceObject();

		Transaction transaction = begin();
		referenceObject.delete();
		try {
			setReference(e, referenceObject, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
			fail("Must not set deleted Object into a Reference");
		} catch (DataObjectException ex) {
			// expected
		}
		transaction.commit();
	}

	public void testSetDeletedObjectWithinTxCurrent() throws KnowledgeBaseException, DataObjectException {
		// object to delete within transaction
		final KnowledgeObject referenceObject = createReferenceObject();

		Transaction transaction = begin();
		referenceObject.delete();
		try {
			setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
			fail("Must not set deleted Object into a Reference");
		} catch (DataObjectException ex) {
			// expected
		}
		transaction.commit();
	}

	public void testMixedReference() throws KnowledgeBaseException, DataObjectException {
		final KnowledgeObject referenceObject = createReferenceObject();

		String mixedAttr = getReferenceAttr(MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		Transaction transaction = begin();
		e.setAttributeValue(mixedAttr, referenceObject);
		transaction.commit();
		assertEquals(referenceObject, e.getAttributeValue(mixedAttr));

		KnowledgeItem referencedObjectHistoric =
			HistoryUtils.getKnowledgeItem(transaction.getCommitRevision(), referenceObject);
		Transaction transaction2 = begin();
		e.setAttributeValue(mixedAttr, referencedObjectHistoric);
		transaction2.commit();
		assertEquals(referencedObjectHistoric, e.getAttributeValue(mixedAttr));

	}

	public void testMonoCurrentLocalRefererOnDescBranch() throws KnowledgeBaseException, DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject referenceObject = createReferenceObject();

		Branch someBranch = HistoryUtils.createBranch(trunk(), lastRevision());
		Branch previousBranch = HistoryUtils.setContextBranch(someBranch);
		try {
			KnowledgeObject referer = createReferer("objectWithReference");
			KnowledgeItem refObjectOnBranch = HistoryUtils.getKnowledgeItem(someBranch, referenceObject);
			txBegin();
			setReference(referer, refObjectOnBranch, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			txCommit();
			checkReference(referer, refObjectOnBranch, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		} finally {
			HistoryUtils.setContextBranch(previousBranch);
		}
	}

	/**
	 * Tests setter and getter of monomorphic, historic, and branch-global property
	 */
	public void testSettingProperties() throws SQLException, DataObjectException {
		checkAttributes(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		checkAttributes(MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		checkAttributes(!MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		checkAttributes(!MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		checkAttributes(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		checkAttributes(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		checkAttributes(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		checkAttributes(!MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		checkAttributes(MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		checkAttributes(MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		checkAttributes(!MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		checkAttributes(!MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);

	}

	/**
	 * Creates an {@link KIReference} with the given properties and checks that the getter work
	 * correct.
	 */
	private void checkAttributes(boolean monomorphic, HistoryType historyType, boolean branchGlobal)
			throws SQLException, DataObjectException {
		final MOClass tmp = new MOKnowledgeItemImpl(Integer.toString(CNT++));
		MOReference attr = newReferenceAttribute(Integer.toString(CNT++), type(REFERENCE_TYPE_NAME));
		attr.setMonomorphic(monomorphic);
		attr.setHistoryType(historyType);
		attr.setBranchGlobal(branchGlobal);
		tmp.addAttribute(attr);
		DBHelper sqlDialect = kb().getConnectionPool().getSQLDialect();
		boolean multipleBranches = kb().getMORepository().multipleBranches();
		DBTypeRepository typeRepository = new DBTypeRepository(sqlDialect, multipleBranches);
		tmp.resolve(typeRepository);
		tmp.freeze();

		assertTrue("Setting (or getting) monomorphic property failed", attr.isMonomorphic() == monomorphic);
		assertTrue("Setting (or getting) historic property failed", attr.getHistoryType() == historyType);
		assertTrue("Setting (or getting) branch global property failed", attr.isBranchGlobal() == branchGlobal);
	}

	/**
	 * Tests that a current reference of an historic object does have a historic value.
	 */
	public void testCurrentRefOnHistObj() throws KnowledgeBaseException, DataObjectException {
		final KnowledgeObject referenceObject = createReferenceObject();
		final KnowledgeItem stableRef = HistoryUtils.getKnowledgeItem(lastRevision(), referenceObject);

		txBegin();
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(e, stableRef, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		txCommit();
		checkReference(true, e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		checkReference(true, e, stableRef, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);

		assertNull(getAttribute(e, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL, A2_NAME));
		assertNull(getAttribute(e, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL, A2_NAME));

		txBegin();
		setA2(referenceObject, "a2");
		final Revision refChangeRevision = txCommit().getCommitRevision();

		assertEquals("a2", getAttribute(e, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL, A2_NAME));
		assertNull(getAttribute(e, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL, A2_NAME));

		txBegin();
		setA2(referenceObject, "newA2");
		txCommit();

		assertEquals("newA2", getAttribute(e, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL, A2_NAME));
		assertNull(getAttribute(e, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL, A2_NAME));

		final KnowledgeItem historicSource = HistoryUtils.getKnowledgeItem(refChangeRevision, e);
		assertEquals("a2", getAttribute(historicSource, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL, A2_NAME));
		assertNull(getAttribute(historicSource, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL, A2_NAME));
	}

	/**
	 * Tests that it is not possible to set a branch local reference to a type which is not branched
	 */
	public void testBranchLocalRefOnTypeShiningThrough() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		KnowledgeObject referenceObject = createReferenceObject();

		txBegin();
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		txCommit();

		Branch b1 = HistoryUtils.createBranch(trunk(), lastRevision(), null);

		KnowledgeItem eOnb1 = HistoryUtils.getKnowledgeItem(b1, e);
		KnowledgeItem referenceObjectOnb1 = HistoryUtils.getKnowledgeItem(b1, referenceObject);

		checkReference(true, eOnb1, referenceObjectOnb1, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);

		// change of property after creation of branch must be visible as type shines through
		txBegin();
		setA2(referenceObject, "a2");
		Revision commitRevision2 = txCommit().getCommitRevision();
		KnowledgeItem referenceAfterBranchCreation = HistoryUtils.getKnowledgeItem(commitRevision2, referenceObject);

		// check that there is only the reference on trunk
		txBegin();
		setReference(e, referenceAfterBranchCreation, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		txCommit();

		checkReference(true, e, referenceAfterBranchCreation, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		checkReference(false, eOnb1, referenceAfterBranchCreation, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);

		// check that there is also the reference on branch
		txBegin();
		try {
			setReference(eOnb1, referenceAfterBranchCreation, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail("History context of referenced object is later than branch time. Therefore the reference is not branch local.");
		} catch (DataObjectException ex) {
			// expected
		}
		txCommit();
	}

	/**
	 * Checks that the given reference is related to the given source
	 * 
	 * @param equal
	 *        must be reference to the given object or not
	 */
	private void checkReference(boolean equal, KnowledgeItem source,
			KnowledgeItem reference, boolean monomorphic, HistoryType historyType, boolean branchGlobal)
			throws DataObjectException {
		final SetExpression searchExpressionMonoCurLocal = filter(allOf(refererTypeName()),
			eqBinary(reference(refererTypeName(), getReferenceAttr(monomorphic, historyType, branchGlobal), ReferencePart.name),
				literal(reference.tId().getObjectName())));
		final List<?> search = kb().search(queryUnresolved(searchExpressionMonoCurLocal).setBranchParam(BranchParam.all));

		if (equal) {
			assertEquals(reference, getReference(source, monomorphic, historyType, branchGlobal));
			assertTrue(search.contains(source));
		} else {
			assertNotEquals(reference, getReference(source, monomorphic, historyType, branchGlobal));
			assertFalse(search.contains(source));
		}
	}

	/**
	 * Tests the deleting a reference does not result in a dangling reference
	 */
	public void testDataIntegrity() throws DataObjectException {
		final KnowledgeObject referenceObject = createReferenceObject();

		txBegin();
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		txCommit();

		txBegin();
		referenceObject.delete();
		txCommit();

		final KnowledgeItem reference = getReference(e, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		if (reference != null && reference.isAlive()) {
			fail("Ticket #4519: The referenced Object is deleted but accessible");
		}
	}

	/**
	 * Tests that a branch local reference can be set to an object on an ancestor branch.
	 */
	public void testBranchLocalRefOnPreviousBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeItem referenceObject = createStableReferenceObject();

		txBegin();
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		final Revision commitRevision = txCommit().getCommitRevision();

		final Branch b1 = HistoryUtils.createBranch(trunk(), commitRevision);

		final KnowledgeItem eOnb1 = HistoryUtils.getKnowledgeItem(b1, e);

		checkReference(true, eOnb1, referenceObject, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		checkReference(true, e, referenceObject, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		assertEquals(referenceObject, getReference(eOnb1, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));
	}

	/**
	 * Tests the value of a historic reference is a historic object, also if the value was current
	 * when set.
	 */
	public void testMorphCurrentToHistoric() throws DataObjectException {
		final KnowledgeObject referenceObject = createReferenceObject();

		txBegin();
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		setA2(referenceObject, "a2");
		final Revision commitRevision = txCommit().getCommitRevision();

		assertEquals(referenceObject, getReference(e, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL));
		final KnowledgeItem stabilizedObject = HistoryUtils.getKnowledgeItem(commitRevision, referenceObject);
		assertEquals(stabilizedObject, getReference(e, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));

		txBegin();
		setA2(referenceObject, "new a2");
		txCommit();

		assertEquals("a2", getAttribute(e, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL, A2_NAME));
		assertEquals("new a2", getAttribute(e, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL, A2_NAME));

		final SetExpression searchExpressionMonoCurGlobal =
			filter(
				allOf(refererTypeName()),
				eqBinary(reference(refererTypeName(), REFERENCE_MONO_CUR_GLOBAL_NAME, ReferencePart.name), literal(referenceObject
					.tId().getObjectName())));
		assertEquals(set(e), toSet(kb().search(queryUnresolved(searchExpressionMonoCurGlobal))));
		final SetExpression searchExpressionMonoHistGlobal =
			filter(
				allOf(refererTypeName()),
				eqBinary(reference(refererTypeName(), REFERENCE_MONO_HIST_GLOBAL_NAME, ReferencePart.name),
					literal(stabilizedObject
						.tId().getObjectName())));
		assertEquals(set(e), toSet(kb().search(queryUnresolved(searchExpressionMonoHistGlobal))));
	}

	public void testCurrentReference() throws KnowledgeBaseException, DataObjectException {
		final KnowledgeObject referenceObject = createReferenceObject();

		txBegin();
		setReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		txCommit();
		checkReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		// reference is the same after editing the source object
		txBegin();
		setA2(e, "a2");
		txCommit();
		checkReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		// reference is the same after editing the dest object
		txBegin();
		setA2(referenceObject, "a2");
		txCommit();
		checkReference(e, referenceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

	}

	public void testMonoHistGlobal() throws DataObjectException {
		final KnowledgeItem stableReference = createStableReferenceObject();

		txBegin();
		setReference(e, stableReference, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		assertEquals("value must be accessible before commit.", stableReference,
			getReference(e, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
		txCommit();

		assertEquals("value must be accessible after commit.", stableReference,
			getReference(e, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));

		txBegin();
		setA2(e, "a2");
		txCommit();

		assertEquals("value must not bew changed after value change.", stableReference,
			getReference(e, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
	}

	protected KnowledgeItem createStableReferenceObject() throws DataObjectException {
		return stabilze(createReferenceObject());
	}

	protected KnowledgeItem stabilze(final KnowledgeItem reference) throws DataObjectException {
		final Revision createRevision = HistoryUtils.getLastRevision();

		final KnowledgeItem stableReference = HistoryUtils.getKnowledgeItem(createRevision, reference);
		return stableReference;
	}

	protected KnowledgeObject createReferenceObject() throws DataObjectException, KnowledgeBaseException {
		final Branch branch = kb().getTrunk();
		return createReferenceOnBranch(branch);
	}

	protected KnowledgeObject createReferenceOnBranch(final Branch branch) throws DataObjectException,
			KnowledgeBaseException {
		txBegin();
		final KnowledgeObject reference = kb().createKnowledgeObject(branch, null, REFERENCE_TYPE_NAME);
		setA1(reference, "reference" + CNT++);
		txCommit();
		return reference;
	}

	public void testNotCorrectType() throws DataObjectException {
		txBegin();
		final KnowledgeObject referer = createReferer("illegalTypeRef");
		txCommit();
		KnowledgeItem stableRef = stabilze(referer);
		try {
			setReference(e, stableRef, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
			fail("object of type " + refererTypeName() + " as value for monomorphic reference of type " + REFERENCE_TYPE_NAME);
		} catch (DataObjectException dox) {
			dox.printStackTrace();
			// expected
		}

		txBegin();
		final KnowledgeObject newB = newB("illegalTypeRef");
		txCommit();
		stableRef = stabilze(newB);
		try {
			setReference(e, stableRef, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
			fail("object of type " + B_NAME + " as value for monomorphic reference of type " + REFERENCE_TYPE_NAME);
		} catch (DataObjectException dox) {
			// expected
		}
		try {
			setReference(e, stableRef, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
			fail("object of type " + B_NAME + " as value for non monomorphic reference of type " + REFERENCE_TYPE_NAME);
		} catch (DataObjectException dox) {
			// expected
		}
	}

	public void testLocalReferenceDifferentBranches2() throws KnowledgeBaseException, DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeItem referenceTrunkBeforeBranch = createStableReferenceObject();

		final Branch branch1 = HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getLastRevision());

		final KnowledgeItem referenceTrunkAfterBranch = createStableReferenceObject();

		final Branch branchBeforeSource = HistoryUtils.createBranch(branch1, HistoryUtils.getLastRevision());
		final KnowledgeItem illegalRef1 = stabilze(createReferenceOnBranch(branchBeforeSource));

		txBegin();
		final KnowledgeObject source = kb().createKnowledgeObject(branch1, null, refererTypeName());
		setA1(source, "xxx");
		txCommit();

		final Branch branchAfterSource = HistoryUtils.createBranch(branch1, HistoryUtils.getLastRevision());
		final KnowledgeItem illegalRef2 = stabilze(createReferenceOnBranch(branchAfterSource));

		checkReference(source, referenceTrunkBeforeBranch, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);

		try {
			setReference(source, referenceTrunkAfterBranch, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail(referenceTrunkAfterBranch + " lives on basebranch after branch creation. sourceObject:" + source);
		} catch (DataObjectException dox) {
			// expected
		}
		try {
			setReference(source, illegalRef1, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail(referenceTrunkAfterBranch + " lives on branch based on branch of source object. sourceObject:"
				+ source);
		} catch (DataObjectException dox) {
			// expected
		}
		try {
			setReference(source, illegalRef2, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail(referenceTrunkAfterBranch + " lives on branch based on branch of source object. sourceObject:"
				+ source);
		} catch (DataObjectException dox) {
			// expected
		}
	}

	public void testLocalReferenceDifferentBranches1() throws KnowledgeBaseException, DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final Branch branch1 = HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getLastRevision());

		final KnowledgeItem illegalReferenceOnLaterBranch = stabilze(createReferenceOnBranch(branch1));
		try {
			setReference(e, illegalReferenceOnLaterBranch, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail(illegalReferenceOnLaterBranch + " lives on foreigtn branch");
		} catch (DataObjectException dox) {
			// expected
		}
	}

	public void testMonoHistGlobalOnDifferentBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}

		final Branch branch1 = HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getLastRevision());
		final KnowledgeObject referenceOnDifferentBranch = createReferenceOnBranch(branch1);
		final KnowledgeItem stableReference = stabilze(referenceOnDifferentBranch);

		checkReference(e, stableReference, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);

		txBegin();
		setA2(e, "a2");
		txCommit();

		assertEquals("value must not bew changed after value change.", stableReference,
			getReference(e, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL));
	}

	public void testLocalTypeShiningThough() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeItem referenceObjectBeforeBranch1Creation = createStableReferenceObject();

		final long revisionBeforeBranch1 = kb().getLastRevision();
		final Branch branch1 =
			HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getLastRevision(),
				types(refererTypeName(), REFERENCE_TYPE_NAME));

		final KnowledgeItem sourceOnBranch = HistoryUtils.getKnowledgeItem(branch1, e);

		txBegin();
		setReference(sourceOnBranch, referenceObjectBeforeBranch1Creation, MONOMORPHIC, HistoryType.HISTORIC,
			!BRANCH_GLOBAL);
		txCommit();
		assertEquals(referenceObjectBeforeBranch1Creation,
			getReference(sourceOnBranch, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL));

		final KnowledgeItem referenceObjectAfterBranch1Creation = createStableReferenceObject();
		txBegin();
		try {
			setReference(sourceOnBranch, referenceObjectAfterBranch1Creation, MONOMORPHIC, HistoryType.HISTORIC,
				!BRANCH_GLOBAL);
			fail("History context of referenced object is later than branch time. Therefore the reference is not branch local.");
		} catch (DataObjectException ex) {
			// expected
		}
		txCommit();

		final Branch branchBeforeBranch1 =
			HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getRevision(revisionBeforeBranch1));
		final KnowledgeItem illegalReference1 = stabilze(createReferenceOnBranch(branchBeforeBranch1));
		try {
			setReference(sourceOnBranch, illegalReference1, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail(illegalReference1 + " does not live on the correct branch as branchlocal value of " + sourceOnBranch);
		} catch (DataObjectException dox) {
			// expected
		}

		final Branch branchAfterBranch1 =
			HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getLastRevision());
		final KnowledgeItem illegalReference2 = stabilze(createReferenceOnBranch(branchAfterBranch1));
		try {
			setReference(sourceOnBranch, illegalReference2, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail(illegalReference2 + " does not live on the correct branch as branchlocal value of " + sourceOnBranch);
		} catch (DataObjectException dox) {
			// expected
		}

	}

	public void testMonoHistLocal2() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final KnowledgeObject createReferenceObject = createReferenceObject();
		// Must branch after creation to have corresponding object on branch
		final Branch branch1 = HistoryUtils.createBranch(kb().getTrunk(), HistoryUtils.getLastRevision());
		final KnowledgeItem stableRefOnDiffBranch = HistoryUtils.getKnowledgeItem(branch1, createReferenceObject);
		txBegin();
		try {
			setReference(e, stableRefOnDiffBranch, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
			fail("branch local reference with objects on different branches: source:" + e + ", reference:"
				+ stableRefOnDiffBranch);
		} catch (DataObjectException ex) {
			// expected
		} finally {
			txCommit();
		}
	}

	public void testMonoHistLocal() throws DataObjectException {
		checkReference(e, createStableReferenceObject(), MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
	}

	public void testReferenceEvaluation() throws DataObjectException {
		final String referenceAttrName = REFERENCE_POLY_HIST_GLOBAL_NAME;
		KnowledgeItem reference = createStableReferenceObject();

		txBegin();
		e.setAttributeValue(referenceAttrName, reference);
		txCommit();

		final String sourceObjectType = e.tId().getObjectType().getName();
		assertEquals(reference, evaluate(reference(sourceObjectType, referenceAttrName, null)));
		assertEquals(reference.tTable().getName(),
			evaluate(reference(sourceObjectType, referenceAttrName, ReferencePart.type)));
		assertEquals(reference.tId().getObjectName(),
			evaluate(reference(sourceObjectType, referenceAttrName, ReferencePart.name)));
	}

	private Object evaluate(Expression expr) {
		TypeBinding.bindTypes(typeSystem(), new ExpressionCompileProtocol(new BufferingProtocol()), expr);
		return SimpleExpressionEvaluator.evaluate(expr, e);
	}

	private void checkReference(final KnowledgeObject source, final KnowledgeItem value, boolean monomorphic,
			HistoryType historyType, boolean branchGlobal) throws DataObjectException, KnowledgeBaseException {
		txBegin();
		setReference(source, value, monomorphic, historyType, branchGlobal);
		assertEquals("wrong value before commit", value, getReference(source, monomorphic, historyType, branchGlobal));
		txCommit();
		assertEquals("wrong value after commit", value, getReference(source, monomorphic, historyType, branchGlobal));
	}

}
