/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.KBTestUtils;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * Tests support of (partial) branches in {@link DBKnowledgeBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestBranching extends AbstractDBKnowledgeBaseTest {

	protected KnowledgeObject b1;
	protected KnowledgeObject b2;
	protected KnowledgeObject b3;
	protected KnowledgeObject b4;
	
	protected KnowledgeObject c1;
	protected KnowledgeObject d1;
	protected KnowledgeObject d2;
	protected KnowledgeObject d3;
	
	protected KnowledgeObject b1_branch2;
	protected KnowledgeObject b2_branch2;
	protected KnowledgeObject b3_branch2;
	protected KnowledgeObject b4_branch2;
	
	protected KnowledgeAssociation ab_d1d2;
	protected KnowledgeAssociation ab_b3b4;
	
	protected KnowledgeAssociation bc_d1d2;
	protected KnowledgeAssociation bc_d2d3;

	protected Set branchScheme;
	
	protected Branch trunk;
	protected Branch branch2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		HistoryUtils.setContextBranch(HistoryUtils.getTrunk());

		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			b4 = newB("b4");
			commit(tx);
		}

		{
			Transaction tx = begin();
			c1 = newC("c1");
			commit(tx);
		}

		{
			Transaction tx = begin();
			d1 = newD("d1");
			d2 = newD("d2");
			d3 = newD("d3");
			commit(tx);
		}
		
		Revision r1;
		{
			Transaction tx = begin();
			ab_d1d2 = newAB(d1, d2);
			ab_b3b4 = newAB(b3, b4);
			
			bc_d1d2 = newBC(d1, d2);
			bc_d2d3 = newBC(d2, d3);
			
			commit(tx);
			r1 = tx.getCommitRevision();
		}

		branchScheme = new HashSet();
		branchScheme.add(type(B_NAME));
		
		branch2 = HistoryUtils.createBranch(HistoryUtils.getTrunk(), r1, branchScheme);
		
		assertNotNull(branch2);
		
		trunk = HistoryUtils.getTrunk();
		
		b1_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b1);
		b2_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b2);
		b3_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b3);
		b4_branch2 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch2, b4);
		
		// Branched objects.
		assertNotEquals(b1, b1_branch2);
		assertNotEquals(b2, b2_branch2);
		assertNotEquals(b3, b3_branch2);
		assertNotEquals(b4, b4_branch2);

		// Objects not branched.
		assertEquals(c1, HistoryUtils.getKnowledgeItem(branch2, c1));
		
		assertEquals(d1, HistoryUtils.getKnowledgeItem(branch2, d1));
		assertEquals(d2, HistoryUtils.getKnowledgeItem(branch2, d2));
		assertEquals(d3, HistoryUtils.getKnowledgeItem(branch2, d3));
	}
	
	@Override
	protected void tearDown() throws Exception {
		b1 = null;
		b2 = null;
		b3 = null;
		b4 = null;
		
		c1 = null;
		d1 = null;
		d2 = null;
		d3 = null;
		
		ab_d1d2 = null;
		ab_b3b4 = null;
		
		bc_d1d2 = null;
		bc_d2d3 = null;
		
		branchScheme = null;
		branch2 = null;
		
		trunk = null;
		
		b1_branch2 = null;
		b2_branch2 = null;
		b3_branch2 = null;
		b4_branch2 = null;
		
		super.tearDown();
	}
	
	public void testSetup() {
		// Check navigation in branch 2.
		
		KBTestUtils.assertTargets("Navigation from unbranched to unbranched object.", set(d2),
			d1.getOutgoingAssociations(BC_NAME));
		KBTestUtils.assertTargets("Navigation from unbranched to unbranched object.", set(d2),
			d1.getOutgoingAssociations(AB_NAME));
		
		assertNotEquals(b3, b3_branch2);
		assertNotEquals(b4, b4_branch2);
		KBTestUtils.assertTargets("Navigation from branched to branched object.", set(b4),
			b3.getOutgoingAssociations(AB_NAME));
		KBTestUtils.assertTargets("Navigation from branched to branched object.", set(b4_branch2),
			b3_branch2.getOutgoingAssociations(AB_NAME));
	}
	
	public void testLinkingUnbranchedObjects() throws DataObjectException {
		KnowledgeAssociation ab_d1d2_branch2 = (KnowledgeAssociation) HistoryUtils.getKnowledgeItem(branch2, ab_d1d2);
		assertNull("Ticket #845: Link between unbranched objects must not be branched.", ab_d1d2_branch2);
	}
	
	public void testViewHistoryNavigation() throws DataObjectException {
		Revision r1;
		{
			Transaction tx = begin();
			setA2(d1, "modification1");
			commit(tx);
			r1 = tx.getCommitRevision();
		}
		
		Revision r2;
		{
			Transaction tx = begin();
			setA2(d1, "modification2");
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			setA2(d1, "modification3");
			commit(tx);
		}
		
		assertEquals("modification3", d1.getAttributeValue(A2_NAME));
		
		KnowledgeItem d1_branch2_r2 = HistoryUtils.getKnowledgeItem(r2, d1);
		assertEquals(trunk.getBranchId(), d1_branch2_r2.getBranchContext());
		assertEquals("modification2", d1_branch2_r2.getAttributeValue(A2_NAME));
		
		KnowledgeItem d1_r1 = HistoryUtils.getKnowledgeItem(r1, d1);
		assertEquals(trunk.getBranchId(), d1_r1.getBranchContext());
		assertEquals("modification1", d1_r1.getAttributeValue(A2_NAME));
	}

	public void testViewHistoryLookup() throws DataObjectException {
		{
			Transaction tx = begin();
			setA2(d1, "modification1");
			commit(tx);
		}
		
		Revision r2;
		{
			Transaction tx = begin();
			setA2(d1, "modification2");
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		{
			Transaction tx = begin();
			setA2(d1, "modification3");
			commit(tx);
		}

		HistoryUtils.setContextBranch(branch2);
		KnowledgeItem d1_branch2_r2 = HistoryUtils.getKnowledgeItem(r2, type(D_NAME), d1.getObjectName());
		assertEquals(trunk.getBranchId(), d1_branch2_r2.getBranchContext());
		assertEquals("modification2", d1_branch2_r2.getAttributeValue(A2_NAME));
	}
	
	public void testUncommittedViewNavigationOrigCreate() throws DataObjectException {
		{
			Transaction tx = begin();
			KnowledgeAssociation d3d2 = newBC(d3, d2);
			assertNotNull(d3d2);
			
			checkLinkContext();
			
			rollback(tx);
		}
	}
	
	public void testUncommittedViewNavigationViewCreate() throws DataObjectException {
		{
			Transaction tx = begin();
			KnowledgeAssociation d3d2 = newBC(d3, d2);
			assertNotNull(d3d2);
			
			checkLinkContext();
			
			rollback(tx);
		}
	}

	private void checkLinkContext() throws InvalidLinkException {
		List d3_bc_branch2 = toList(d3.getOutgoingAssociations(BC_NAME));
		assertTrue(d3_bc_branch2.size() > 0);
		
		for (Iterator it = d3_bc_branch2.iterator(); it.hasNext(); ) {
			KnowledgeAssociation link = (KnowledgeAssociation) it.next();
			assertEquals(trunk.getBranchId(), link.getBranchContext());
			assertEquals(trunk.getBranchId(), link.getDestinationObject().getBranchContext());
		}
	}
	
	public void testObjectViewDelete() throws DataObjectException {
		{
			Transaction tx = begin();
			d1.delete();
			commit(tx);
		}
	}
	
	public void testObjectViewDeleteKb() throws DataObjectException {
		{
			Transaction tx = begin();
			kb().delete(d1);
			commit(tx);
		}
	}
	
	public void testAssociationViewDelete() throws DataObjectException {
		List d1d2_branch2 = toList(d1.getOutgoingAssociations(BC_NAME, d2));
		assertTrue(d1d2_branch2.size() > 0);
		
		{
			Transaction tx = begin();
			for (Iterator it = d1d2_branch2.iterator(); it.hasNext(); ) {
				KnowledgeAssociation link = (KnowledgeAssociation) it.next();
				
				link.delete();
			}
			commit(tx);
		}
	}
	
	public void testAssociationViewDeleteKb() throws DataObjectException {
		List d1d2_branch2 = toList(d1.getOutgoingAssociations(BC_NAME, d2));
		assertTrue(d1d2_branch2.size() > 0);

		{
			Transaction tx = begin();
			for (Iterator it = d1d2_branch2.iterator(); it.hasNext(); ) {
				KnowledgeAssociation link = (KnowledgeAssociation) it.next();
				
				kb().delete(link);
			}
			commit(tx);
		}
	}
	
	public void testBranchObjectViewLookup() throws NoSuchAttributeException {
		HistoryUtils.setContextBranch(branch2);
		
		KnowledgeObject d1_branch2_lookup = kb().getKnowledgeObject(D_NAME, d1.getObjectName());
		assertNotNull(d1_branch2_lookup);
		assertEquals(trunk.getBranchId(), d1_branch2_lookup.getBranchContext());
	}
	
	public void testBranchObjectViewSearch() throws NoSuchAttributeException {
		Branch branch3 = HistoryUtils.createBranch(branch2, HistoryUtils.getLastRevision(), branchScheme);
		HistoryUtils.setContextBranch(branch3);
		
		KnowledgeObject d1_branch3_lookup = kb().getKnowledgeObject(D_NAME, d1.getObjectName());
		assertNotNull(d1_branch3_lookup);
		assertEquals(trunk.getBranchId(), d1_branch3_lookup.getBranchContext());
	}
	
	public void testAssociationCreation() throws DataObjectException {
		// Create (branched) association between two branched objects in context of trunk.
		HistoryUtils.setContextBranch(HistoryUtils.getTrunk());
		
		{
			Transaction tx = begin();
			KnowledgeAssociation link;
			try {
				link = newAB(b2_branch2, b2_branch2);
				fail("It is impossible to create in trunk branch local association between objects on branch.");
			} catch (DataObjectException ex) {
				// expected
				Branch former = HistoryUtils.setContextBranch(branch2);
				link = newAB(b2_branch2, b2_branch2);
				HistoryUtils.setContextBranch(former);
			}
			assertNotNull(link);
			
			// Association is created in branch 2.
			assertEquals(branch2.getBranchId(), link.getBranchContext());
			
			commit(tx);
		}
		
		Set branchTargets = navigateForward(b2_branch2, AB_NAME);
		assertTrue(branchTargets.contains(b2_branch2));
		assertFalse(branchTargets.contains(b2));

		Set originalTargets = navigateForward(b2, AB_NAME);
		assertFalse(originalTargets.contains(b2));
		assertFalse(originalTargets.contains(b2_branch2));
	}
	
	public void testAssociationViewCreation() throws DataObjectException {
		// Create unbranched association between two views of unbranched objects in context of trunk.
		HistoryUtils.setContextBranch(branch2);
		
		{
			Transaction tx = begin();
			KnowledgeAssociation link;
			try {
				link = newBC(c1, c1);
				fail("It is impossible to create on branch 'branch2' a branch local association between objects on trunk.");
			} catch (DataObjectException ex) {
				// expected
				Branch former = HistoryUtils.setContextBranch(trunk);
				link = newBC(c1, c1);
				HistoryUtils.setContextBranch(former);
			}
			assertNotNull(link);
			
			// The association is created in trunk but a view in branch 2 is returned.
			assertEquals(trunk.getBranchId(), link.getBranchContext());
			
			commit(tx);
		}
		
		Set branchTargets = navigateForward(c1, BC_NAME);
		assertTrue(branchTargets.contains(c1));

		Set originalTargets = navigateForward(c1, BC_NAME);
		assertTrue(originalTargets.contains(c1));
	}

	public void testObjectViewCreation() throws DataObjectException {
		HistoryUtils.setContextBranch(branch2);
		
		{
			Transaction tx = begin();
			// Create new unbranched object in the context of branch 2. 
			KnowledgeObject c2 = newC("c2");
			
			assertEquals(trunk.getBranchId(), c2.getBranchContext());
			
			commit(tx);
		}
	}
	
	public void testCheckBranching() throws DataObjectException {
		Revision r2;
		{
			// Create association between formerly branched and unbranched objects.
			Transaction tx = begin();
			newBC(b1, c1);
			
			commit(tx);
			r2 = tx.getCommitRevision();
		}
		
		try {
			HistoryUtils.createBranch(HistoryUtils.getTrunk(), r2, branchScheme);
			fail("Ticket #427: Branching must be rejected, because there unbranched links between branched and unbranched objects.");
		} catch (IllegalArgumentException ex) {
			// Expected: The association type BC must also be branched to preserve consistency.
		}

		branchScheme.add(type(C_NAME));
		
		Branch branch3 = HistoryUtils.createBranch(HistoryUtils.getTrunk(), r2, branchScheme);
		assertNotNull(branch3);
		
		KnowledgeObject b1_branch3 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch3, b1);
		KnowledgeObject c1_branch3 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch3, c1);
		
		// Check that D is not branched.
		assertSame(d1, HistoryUtils.getKnowledgeItem(branch3, d1));
		assertSame(d2, HistoryUtils.getKnowledgeItem(branch3, d2));
		assertSame(d3, HistoryUtils.getKnowledgeItem(branch3, d3));
		
		// Test navigation of unbranched objects in context of branch3.
		KnowledgeObject d1_branch3 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch3, d1);
		KnowledgeObject d2_branch3 = (KnowledgeObject) HistoryUtils.getKnowledgeItem(branch3, d2);
		KBTestUtils.assertTargets("", set(d2_branch3), d1_branch3.getOutgoingAssociations(AB_NAME));
		
		{
			// Delete original of c1_branch3 (c1) through an operation in branch2.
			Transaction tx = begin();
			c1.delete();
			
			commit(tx);
		}
		
		// Make sure that navigation in branch3 is consistent.
		KBTestUtils.assertTargets("", set(c1_branch3), b1_branch3.getOutgoingAssociations(BC_NAME));
	}

	public void testBranchingWithReferences() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject referenceF = newF("f1");
		KnowledgeObject referenceD = newD("d1");

		// branch global
		setReference(referer, referenceF, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		setReference(referer, referenceD, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		setReference(referer, referenceF, !MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		setReference(referer, referenceD, MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		setReference(referer, referenceF, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		setReference(referer, referenceD, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);

		// branch local
		setReference(referer, referenceF, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(referer, referenceD, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(referer, referenceF, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		setReference(referer, referenceD, MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		setReference(referer, referenceF, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		setReference(referer, referenceD, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		commit(tx);

		KnowledgeItem refDHistoric = HistoryUtils.getKnowledgeItem(tx.getCommitRevision(), referenceD);
		KnowledgeItem refFHistoric = HistoryUtils.getKnowledgeItem(tx.getCommitRevision(), referenceF);
		Transaction changeTX = begin();
		// branch local references to current values avoid branching
		setReference(referer, null, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(referer, null, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(referer, refFHistoric, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		setReference(referer, refDHistoric, MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		changeTX.commit();

		try {
			// reference from branched type to not branched type.
			HistoryUtils.createBranch(trunk, tx.getCommitRevision(), set(type(E_NAME), type(D_NAME)));
			fail("Branching must fail after creating reference of a branched to an unbranched type.");
		} catch (IllegalArgumentException ex) {
			// Ignored.
		}

		Branch branch = HistoryUtils.createBranch(trunk, lastRevision(), set(type(E_NAME), type(D_NAME)));
		assertNotSame(D_NAME + " was branched.", referenceD, HistoryUtils.getKnowledgeItem(branch, referenceD));
		assertNotSame(E_NAME + " was branched.", referer, HistoryUtils.getKnowledgeItem(branch, referer));
		assertSame(F_NAME + " was not branched.", referenceF, HistoryUtils.getKnowledgeItem(branch, referenceF));
	}

	public void testBranchingWithExistingBranchLocalCurrentReferences() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject reference = newF("f1");
		setReference(referer, reference, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		commit(tx);

		try {
			// reference from branched type to not branched type.
			HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), set(type(E_NAME), type(D_NAME)));
			fail("Branching must fail after creating reference of a branched to an unbranched type.");
		} catch (IllegalArgumentException ex) {
			// Ignored.
		}
	}

	public void testBranchingWithExistingBranchLocalMixedReferences() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject reference = newF("f1");
		setReference(referer, reference, !MONOMORPHIC, HistoryType.MIXED, !BRANCH_GLOBAL);
		commit(tx);

		try {
			// reference from branched type to not branched type.
			HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), set(type(E_NAME), type(D_NAME)));
			fail("Branching must fail after creating reference of a branched to an unbranched type.");
		} catch (IllegalArgumentException ex) {
			// Ignored.
		}
	}

	/**
	 * Tests that branching is not possible when model contradicts. This means if a type that is
	 * branched has a branch local, monomorphic, current reference with target type which is not
	 * branched, then it would be not possible to set this reference on the branch. Therefore the
	 * reference is useless on the branch.
	 */
	public void testBranchingContradictingModel() {
		try {
			// Should fail early because E has reference which is monomorphic, current, branch
			// local and target type is not branched.
			HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), set(type(E_NAME)));
			fail("Branching must fail after creating reference of a branched to an unbranched type.");
		} catch (IllegalArgumentException ex) {
			// Ignored.
		}
	}

	public void testLinking() throws DataObjectException {
		{
			// Link objects branched in branch2 and objects not branched in branch2.
			Transaction tx = begin();
			newBC(b1, c1);
			commit(tx);
		}
		
		// This prevents creating further branches with the same branching scheme.
		try {
			HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), branchScheme);
			fail("Branching must fail after creating links between branched and unbranched objects.");
		} catch (IllegalArgumentException ex) {
			// Ignored.
		}
	}
	
	/**
	 * Tests that only those associations are branched which have ends that are branched.
	 */
	public void testNotAllAssociationsBranched() throws DataObjectException {
		KnowledgeAssociation branchedBC;
		KnowledgeAssociation unbranchedBC;
		{
			// Link objects branched in branch2 and objects not branched in branch2.
			Transaction tx = begin();
			branchedBC = newBC(b1, b1);
			unbranchedBC = newBC(c1, c1);
			commit(tx);
		}

		Branch branch = HistoryUtils.createBranch(trunk, HistoryUtils.getLastRevision(), set(type(B_NAME)));
		KnowledgeItem branchedBC_branch = HistoryUtils.getKnowledgeItem(branch, branchedBC);
		assertNotNull("Branched link not found.", branchedBC_branch);
		assertEquals("Ends are branched, so reference is branched.", branch.getBranchId(),
			branchedBC_branch.getBranchContext());
		assertNull("Ends are not branched, so reference is also not branched.",
			HistoryUtils.getKnowledgeItem(branch, unbranchedBC));
	}

	public void testCheckLinking() throws DataObjectException {
		// Create unbranched association between branched and unbranched objects.
		try {
			newBC(b1_branch2, c1);
			fail("Ticket #427: Was able to create association between branched and unbranched types.");
		} catch (DataObjectException ex) {
			// Expected, because there must not be an instance of an association
			// between objects, if the type of those objects is branched in a
			// branch branch2, where the association exists as view.
		}
	}
		
    public static Test suite() {
    	if (false) {
			return runOneTest(TestBranching.class, "testCheckBranching");
    	}
		return suiteNeedsBranches(TestBranching.class);
    }

}
