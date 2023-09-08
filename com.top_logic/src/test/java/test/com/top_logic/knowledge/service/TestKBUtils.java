/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.Arrays;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.service.db2.TestDiffEventReader;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.diff.DiffEventReader;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;

/**
 * Tests methods in {@link KBUtils}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestKBUtils extends AbstractDBKnowledgeBaseClusterTest {

	public void testUndoWithReference() {
		final Revision startRev = HistoryUtils.getLastRevision(kb());
		final KnowledgeItem reference;
		final EObj eobj;
		final Revision r2;
		{
			Transaction tx = begin();
			eobj = EObj.newEObj("e1");
			KnowledgeObject tmp = newD("d1");
			commit(tx);
			r2 = tx.getCommitRevision();
			reference = HistoryUtils.getKnowledgeItem(r2, tmp);
		}

		final Revision addRefRev;
		final Revision removeRefRev;
		{
			Transaction addRef = begin();
			eobj.setReference(reference);
			commit(addRef);
			addRefRev = addRef.getCommitRevision();
			Transaction removeRef = begin();
			eobj.setReference(null);
			commit(removeRef);
			removeRefRev = removeRef.getCommitRevision();
		}

		assertNull(eobj.getReference());
		KBUtils.revert(kb(), removeRefRev, kb().getTrunk(), addRefRev, kb().getTrunk());
		assertEquals(reference, eobj.getReference());
		KBUtils.revert(kb(), addRefRev, kb().getTrunk(), r2, kb().getTrunk());
		assertNull(eobj.getReference());
	}

	/**
	 * Test the following situation. Create a wrapper and set a flex attribute.
	 * First rollback to a time before the wrapper was constructed, then
	 * rollback to the time between the construction of the wrapper and setting
	 * of the flex attribute.
	 * 
	 * Then the reanimated wrapper must not have a flex value.
	 */
	public void testUndoObjectWithFlex() {
		final long startRev = kb().getLastRevision();
		final BObj bobj;
		final ObjectBranchId objectID;
		final long r2;
		{
			Transaction tx = begin();
			bobj = BObj.newBObj("b1");
			objectID = getObjectID(bobj);
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}

		{
			Transaction tx = begin();
			bobj.setF1("b1_f1");
			commit(tx);
		}

		KBUtils.revert(kb(), kb().getRevision(startRev), kb().getTrunk());

		/*
		 * After that revert there has to be an object with the same Object id
		 * as bobj which don't have a flex value for attribute F1
		 */
		try {
			KBUtils.revert(kb(), kb().getRevision(r2), kb().getTrunk());
		} catch (AssertionError ex) {
			fail("Ticket #2591: Reanimating of Wrapper with flex attributes fail.", ex);
		}

		final Wrapper reanimatedWrapper = WrapperFactory.getWrapper(kb().getTrunk(), Revision.CURRENT,
			objectID.getObjectName(), objectID.getObjectType().getName());
		assertNull(reanimatedWrapper.getValue(BObj.F1_NAME));
	}
	
	public void testFlexUndo() {
		final BObj bobj1;
		final BObj bobj2;
		final long r1;
		{
			Transaction tx = begin();
			bobj1 = BObj.newBObj("b1");
			
			bobj2 = BObj.newBObj("b2");
			bobj2.setB2("b2_b2_val");
			bobj2.setF2("b2_f2_value");
			
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}

		final BObj bobj3;
		{
			Transaction tx = begin();
			bobj1.setB1("b1_b1_val");
			bobj1.setF1("b1_f1_value");
			
			bobj3 = BObj.newBObj("b3");
			bobj3.setB2("b3_b2_val");
			bobj3.setF2("b3_f2_value");
			
			commit(tx);
		}

		KBUtils.revert(kb(), kb().getRevision(r1), kb().getTrunk());

		final BObj revertedBobj1 = WrapperHistoryUtils.getWrapper(Revision.CURRENT, bobj1);
		assertEquals("b1", revertedBobj1.getA1());
		assertEquals(null, revertedBobj1.getB1());
		assertEquals("Ticket #2591:", null, revertedBobj1.getF1());

		final BObj revertedBobj2 = WrapperHistoryUtils.getWrapper(Revision.CURRENT, bobj2);
		assertEquals("b2", revertedBobj2.getA1());
		assertEquals("b2_b2_val", revertedBobj2.getB2());
		assertEquals("Ticket #2591:", "b2_f2_value", revertedBobj2.getF2());

		final BObj revertedBobj3 = WrapperHistoryUtils.getWrapper(Revision.CURRENT, bobj3);
		assertNull("b3 didn't exists at stopRevision", revertedBobj3);
	}
	
	/**
	 * Branch from Trunk. Revert from that branch to a revision on trunk after
	 * the time the branching occurred.
	 */
	public void testFlexRevertToBaseBranch() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final BObj bobj1;
		final BObj bobj3;
		final long r1;
		{
			Transaction tx = begin();
			bobj1 = BObj.newBObj("b1");
			bobj1.setF2("b1_f2_value");
			
			bobj3 = BObj.newBObj("b3");
			bobj3.setB2("b3_b2_val");
			bobj3.setF2("b3_f2_value");
			
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}

		final BObj bobj2;
		final long r2;
		{
			Transaction tx = begin();
			bobj1.setB1("b1_b1_val");
			bobj1.setF1("b1_f1_value");
			bobj1.setF2("b1_f2_new_value");
			
			bobj2 = BObj.newBObj("b2");
			bobj2.setB2("b2_b2_val");
			bobj2.setF2("b2_f2_value");
			
			bobj3.setB2("b3_b2_new_val");
			bobj3.setF2("b3_f2_new_value");
			
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}

		final Branch branch = kb().createBranch(kb().getTrunk(), kb().getRevision(r1), null);
		KBUtils.revert(kb(), Revision.CURRENT, branch, kb().getRevision(r2), kb().getTrunk());

		final BObj branchedBobj1 = WrapperHistoryUtils.getWrapper(branch, bobj1);
		assertEquals("b1", branchedBobj1.getA1());
		assertEquals("Rollback of row attribute which did not exist before branching fails.", "b1_b1_val", branchedBobj1.getB1());
		assertEquals("Ticket #2591: Rollback of flex attribute which did not exist before branching fails.", "b1_f1_value", branchedBobj1.getF1());
		assertEquals("Ticket #2591: Rollback of row attribute which already exist before branching fails.", "b1_f2_new_value", branchedBobj1.getF2());

		final BObj branchedBobj2 = WrapperHistoryUtils.getWrapper(branch, bobj2);
		assertEquals("b2", branchedBobj2.getA1());
		assertEquals("Row attribute is not correct.", "b2_b2_val", branchedBobj2.getB2());
		assertEquals("Ticket #2591: Flex attribute is not correct.", "b2_f2_value", branchedBobj2.getF2());

		final BObj branchedBobj3 = WrapperHistoryUtils.getWrapper(branch, bobj3);
		assertEquals("b3", branchedBobj3.getA1());
		assertEquals("Row attribute is not correct.", "b3_b2_new_val", branchedBobj3.getB2());
		assertEquals("Ticket #2591: Flex attribute is not correct.", "b3_f2_new_value", branchedBobj3.getF2());
	}
	
	
	/**
	 * Branch <code>branch1</code> from Trunk. Later branch <code>branch2</code>
	 * from Trunk. Revert from <code>branch1</code> to <code>branch2</code> and
	 * vice versa.
	 */
	public void testFlexRevertToOtherBranch() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final BObj bobj1;
		final BObj bobj3;
		{
			Transaction tx = begin();
			bobj1 = BObj.newBObj("b1");
			bobj1.setF2("b1_f2_value");
			
			bobj3 = BObj.newBObj("b3");
			bobj3.setB2("b3_b2_val");
			bobj3.setF2("b3_f2_value");
			
			commit(tx);
		}
		final long r1 = kb().getLastRevision();
		final Branch branch1 = kb().createBranch(kb().getTrunk(), kb().getRevision(r1), null);

		kb().setContextBranch(branch1);
		
		final BObj bobj1_branch1;
		final long r3;
		{
			Transaction tx = begin();
			try {
				bobj1_branch1 = BObj.newBObj("b1_branch1");
				bobj1_branch1.setF2("b1_branch1_f2_value");
			} finally {
				kb().setContextBranch(kb().getTrunk());
			}
			commit(tx);
			r3 = tx.getCommitRevision().getCommitNumber();
		}

		final BObj bobj2;
		final long r2;
		{
			Transaction tx = begin();
			bobj1.setB1("b1_b1_val");
			bobj1.setF1("b1_f1_value");
			bobj1.setF2("b1_f2_new_value");
			
			bobj2 = BObj.newBObj("b2");
			bobj2.setB2("b2_b2_val");
			bobj2.setF2("b2_f2_value");
			
			bobj3.setB2("b3_b2_new_val");
			bobj3.setF2("b3_f2_new_value");
			
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
		final Branch branch2 = kb().createBranch(kb().getTrunk(), kb().getRevision(r2), null);

		kb().setContextBranch(branch2);
		
		final BObj bobj1_branch2;
		final long r4;
		{
			Transaction tx = begin();
			try {
				bobj1_branch2 = BObj.newBObj("b1_branch2");
				bobj1_branch2.setF2("b1_branch2_f2_value");
			} finally {
				kb().setContextBranch(kb().getTrunk());
			}
			commit(tx);
			r4 = tx.getCommitRevision().getCommitNumber();
		}

		KBUtils.revert(kb(), kb().getRevision(r4), branch2, kb().getRevision(r3), branch1);

		try {
			final BObj reverted_bobj1_branch2 = WrapperHistoryUtils.getWrapper(branch2, Revision.CURRENT, bobj1_branch2);
			assertNull(reverted_bobj1_branch2);
		} catch (Exception ex) {
			// object did not exists in branch1
		}
		final BObj reverted_bobj1_branch1 = WrapperHistoryUtils.getWrapper(branch2, Revision.CURRENT, bobj1_branch1);
		assertEquals("b1_branch1", reverted_bobj1_branch1.getA1());
		assertEquals("Ticket #2591:", "b1_branch1_f2_value", reverted_bobj1_branch1.getF2());

		final BObj reverted_bobj1 = WrapperHistoryUtils.getWrapper(branch2, Revision.CURRENT, bobj1);
		assertEquals("b1", reverted_bobj1.getA1());
		assertEquals("b1_f2_value", reverted_bobj1.getF2());
		assertEquals("Ticket #2591:", null, reverted_bobj1.getF1());
		assertEquals(null, reverted_bobj1.getB1());

		final BObj reverted_bobj2 = WrapperHistoryUtils.getWrapper(branch2, Revision.CURRENT, bobj2);
		assertNull(reverted_bobj2);

		final BObj reverted_bobj3 = WrapperHistoryUtils.getWrapper(branch2, Revision.CURRENT, bobj3);
		assertEquals("b3", reverted_bobj3.getA1());
		assertEquals("b3_b2_val", reverted_bobj3.getB2());
		assertEquals("b3_f2_value", reverted_bobj3.getF2());
	}
	
	public void testMultipleUndo() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			commit(tx);
		}

		final long r1;
		{
			Transaction tx = begin();
			setB2(b1, "b1.b2");
			setB2(b2, "b2.b2");
			setB2(b3, "b3.b2");
			newAB(b1, b2);
			newAB(b1, b3);
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}

		{
			Transaction tx = begin();
			setB2(b1, "new b1.b2");
			setB2(b2, "new b2.b2");
			b3.delete();
			commit(tx);
		}

		assertEquals("new b1.b2", b1.getAttributeValue(B2_NAME));
		KBUtils.revert(kb(), kb().getRevision(r1), kb().getTrunk());
		assertEquals("b1.b2", b1.getAttributeValue(B2_NAME));
		KnowledgeObject b3Reverted =
			(KnowledgeObject) kb()
				.getKnowledgeItem(kb().getTrunk(), Revision.CURRENT, type(B_NAME), b3.getObjectName());
		assertNotNull(b3Reverted);
		assertEquals("b3.b2", b3Reverted.getAttributeValue(B2_NAME));
		assertEquals(set(b2, b3Reverted), navigateForward(b1, AB_NAME));

		{
			Transaction tx = begin();
			setB2(b3Reverted, "updated b3.b2");
			commit(tx);
		}

		// Revert again.
		try {
			KBUtils.revert(kb(), kb().getRevision(r1), kb().getTrunk());
		} catch (RuntimeException ex) {
			fail("Ticket #2255: Multiple revert failed.", ex);
		}
		assertEquals("b3.b2", b3Reverted.getAttributeValue(B2_NAME));
	}

	/**
	 * Tests a bug in
	 * {@link KBUtils#revert(KnowledgeBase, Revision, Branch, Revision, Branch)}
	 * (actually in {@link DiffEventReader}). Reverting which creates an Object
	 * without business values does not work.
	 * 
	 * @see TestDiffEventReader#testAssociationCreationWithoutValues()
	 */
	public void testMultipleUndo2() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final BObj sourceObj;
		final Revision revision0;
		{
			Transaction tx = begin();
			sourceObj = BObj.newBObj("source");
			commit(tx);
			revision0 = tx.getCommitRevision();
		}

		final CObj destObj;
		final ObjectBranchId destID;
		final KnowledgeAssociation asso;
		final ObjectBranchId associationID;
		final Revision revision1;
		{
			Transaction tx = begin();
			destObj = CObj.newCObj("dest");
			destID = getObjectID(destObj);
			
			// this association just has technical values
			asso = newBC(sourceObj.tHandle(), destObj.tHandle());
			setBC1(asso, null);
			
			associationID = getObjectID(asso);
			commit(tx);
			revision1 = tx.getCommitRevision();
		}

		{
			Transaction tx = begin();
			commit(tx);
		}

		final Branch branch = kb().createBranch(kb().getTrunk(), revision1, null);

		KBUtils.revert(kb(), Revision.CURRENT, branch, revision0, kb().getTrunk());

		final KnowledgeItem rolledAssoBackToV0 = kb().getKnowledgeItem(branch, Revision.CURRENT, associationID.getObjectType(),
				associationID.getObjectName());
		assertNull("Association should not exists since '" + branch + "' was rolled back to " + revision0 + " where it did not exists.",
				rolledAssoBackToV0);
		final KnowledgeItem rolledDestBackToV0 = kb().getKnowledgeItem(branch, Revision.CURRENT, destID.getObjectType(), destID.getObjectName());
		assertNull("Item should not exists since '" + branch + "' was rolled back to " + revision0 + " where it did not exists.", rolledDestBackToV0);

		KBUtils.revert(kb(), Revision.CURRENT, branch, revision1, kb().getTrunk());

		final KnowledgeItem rolledBackToV1 = kb().getKnowledgeItem(branch, Revision.CURRENT, associationID.getObjectType(),
				associationID.getObjectName());
		assertNotNull("Association should exists since '" + branch + "' was rolled back to " + revision1 + " where it did exists.", rolledBackToV1);
	}

	/**
	 * Tests that an object that was re-created by a {@link KnowledgeBase} revert, gets the same
	 * create revision as the original object.
	 */
	public void testRevertWithCorrectCreateRevision() throws DataObjectException {
		ObjectKey id;
		KnowledgeItem tmp;
		final Revision r2;
		{
			Transaction tx = begin();
			tmp = newD("d1");
			id = tmp.tId();
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		{
			Transaction tx = begin();
			tmp.delete();
			commit(tx);
		}

		KBUtils.revert(kb(), r2, kb().getTrunk());

		/* Note: Can not use original key, because it caches the reference to the deleted object
		 * which is not updated to be alive again. */
		ObjectKey idCopy = KBUtils.createObjectKey(id.getBranchContext(), id.getHistoryContext(), id.getObjectType(),
			id.getObjectName());
		tmp = kb().resolveObjectKey(idCopy);
		assertEquals(r2.getCommitNumber(), tmp.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
	}

	public void testBulkDelete() {
		EObj e1;
		EObj e2;
		EObj e3;
		EObj e4;
		{
			try (Transaction tx = begin()){
				e1 = EObj.newEObj("e1");
				e2 = EObj.newEObj("e2");
				e3 = EObj.newEObj("e3");
				e4 = EObj.newEObj("e4");
				tx.commit();
			}
			try (Transaction tx = begin()) {
				KBUtils.deleteAllKI(Arrays.asList(e1, e2, e3, e4).stream().map(TLObject::tHandle).iterator());
				tx.commit();
			}
			assertFalse(e1.tValid());
			assertFalse(e2.tValid());
			assertFalse(e3.tValid());
			assertFalse(e4.tValid());
		}
		{
			try (Transaction tx = begin()) {
				e1 = EObj.newEObj("e1");
				e2 = EObj.newEObj("e2");
				e3 = EObj.newEObj("e3");
				e4 = EObj.newEObj("e4");
				tx.commit();
			}
			List<KnowledgeItem> toDelete = list(e2.tHandle(), e4.tHandle());
			try (Transaction tx = begin()) {
				KBUtils.deleteAllKI(Arrays.asList(e1, e2, e3, e4).stream().map(TLObject::tHandle).iterator(),
					toDelete::contains);
				tx.commit();
			}
			assertTrue(e1.tValid());
			assertFalse(e2.tValid());
			assertTrue(e3.tValid());
			assertFalse(e4.tValid());
		}
		{
			try (Transaction tx = begin()) {
				e1 = EObj.newEObj("e1");
				e2 = EObj.newEObj("e2");
				e3 = EObj.newEObj("e3");
				e4 = EObj.newEObj("e4");
				tx.commit();
			}
			try (Transaction tx = begin()) {
				KBUtils.deleteAll(Arrays.asList(e1, e2, e3, e4).iterator(),
					item -> "e2".equals(((AObj) item).getA1()) || "e4".equals(((AObj) item).getA1()));
				tx.commit();
			}
			assertTrue(e1.tValid());
			assertFalse(e2.tValid());
			assertTrue(e3.tValid());
			assertFalse(e4.tValid());
		}
		
	}

	public static Test suite() {
		// Switch to true to activate single test.
		if (false) {
			return runOneTest(TestKBUtils.class, "testMultipleUndo");
		}
		return suite(TestKBUtils.class);
	}

}
