/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;

/**
 * {@link TestCase} for association caching in the context of branching.
 * 
 * @see KnowledgeBase#resolveLinks(KnowledgeObject, AbstractAssociationQuery)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestKABasedCacheBranching extends AbstractDBKnowledgeBaseTest {

	/**
	 * Setup that creates a test fixture.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public class Setup extends LocalTestSetup {

		BObj b1;
		BObj b2;
		BObj b3;
		BObj b4;
		
		CObj c1;
		CObj c2;
		CObj c3;
		CObj c4;
		
		public Setup(Test test) {
			super(test);
		}
		
		public Setup toBranch(Branch branch) {
			Setup result = new Setup(getTest());
			
			result.b1 = (BObj) onBranch(branch, b1);
			result.b2 = (BObj) onBranch(branch, b2);
			result.b3 = (BObj) onBranch(branch, b3);
			result.b4 = (BObj) onBranch(branch, b4);
			
			result.c1 = (CObj) onBranch(branch, c1);
			result.c2 = (CObj) onBranch(branch, c2);
			result.c3 = (CObj) onBranch(branch, c3);
			result.c4 = (CObj) onBranch(branch, c4);
			
			return result;
		}

		public Setup toRevision(Revision revision) {
			Setup result = new Setup(getTest());
			
			result.b1 = (BObj) toRevision(revision, b1);
			result.b2 = (BObj) toRevision(revision, b2);
			result.b3 = (BObj) toRevision(revision, b3);
			result.b4 = (BObj) toRevision(revision, b4);
			
			result.c1 = (CObj) toRevision(revision, c1);
			result.c2 = (CObj) toRevision(revision, c2);
			result.c3 = (CObj) toRevision(revision, c3);
			result.c4 = (CObj) toRevision(revision, c4);
			
			return result;
		}
		
		private Wrapper toRevision(Revision revision, Wrapper obj) {
			KnowledgeObject ko = obj.tHandle();
			return WrapperFactory.getWrapper((KnowledgeObject) HistoryUtils.getKnowledgeItem(revision, ko));
		}

		private Wrapper onBranch(Branch branch, AObj obj) {
			KnowledgeObject ko = obj.tHandle();
			return WrapperFactory.getWrapper((KnowledgeObject) HistoryUtils.getKnowledgeItem(branch, ko));
		}
//		
//		private HistoryManager historyManager(KnowledgeObject ko) {
//			return ((HistoryManager) ko.getKnowledgeBase());
//		}
		
		@Override
		protected void setUpLocal() throws Exception {
			super.setUpLocal();
			
			{
				Transaction tx = begin();
				b1 = BObj.newBObj("b1");
				b2 = BObj.newBObj("b2");
				b3 = BObj.newBObj("b3");
				b4 = BObj.newBObj("b4");
				
				b1.addAB(b1);
				b1.addAB(b2);
				b1.addAB(b3);
				b1.addAB(b4);
				
				b2.addAB(b1);
				b2.addAB(b3);
				b2.addAB(b4);
				
				c1 = CObj.newCObj("c1");
				c2 = CObj.newCObj("c2");
				c3 = CObj.newCObj("c3");
				c4 = CObj.newCObj("c4");
				
				c1.addAB(c2);
				c1.addAB(c3);
				c1.addAB(c4);
				
				b1.addBC(c1);
				b1.addBC(c2);
				
				c1.addBC(b1);
				c1.addBC(b2);
				c1.addBC(b3);
				
				checkInitial();
				
				commit(tx);
			}
			
			checkInitial();
		}

		public void checkInitial() {
			assertEquals(set(c2, c3, c4), c1.getAB());
			assertEquals(set(), c1.getABSources());
			assertEquals(set(b1, b2, b3), c1.getBC());
			assertEquals(set(b1), c1.getBCSources());
			
			assertEquals(set(b1, b2, b3, b4), b1.getAB());
			assertEquals(set(b1, b2), b1.getABSources());
			assertEquals(set(c1, c2), b1.getBC());
			assertEquals(set(c1), b1.getBCSources());
		}
		
		@Override
		protected void tearDownLocal() throws Exception {
			b1 = null;
			b2 = null;
			b3 = null;
			b4 = null;

			c1 = null;
			c2 = null;
			c3 = null;
			c4 = null;
			
			super.tearDownLocal();
		}
	}

	Setup setup;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		setup = new Setup(this);
		setup.setUp();
	}
	
	@Override
	protected void tearDown() throws Exception {
		setup.tearDown();
		setup = null;
		
		super.tearDown();
	}
	
	public void testDataBranch() {
		// Create full branch.
		Branch branch1 = HistoryUtils.createBranch(HistoryUtils.getContextBranch(), HistoryUtils.getLastRevision(), null);
		
		Setup onBranch1 = setup.toBranch(branch1);
		
		onBranch1.checkInitial();
	}
	
	public void testBranching() throws Throwable {
		// Only branch B, AB, and BC.
		Branch branch1 =
			HistoryUtils.createBranch(HistoryUtils.getContextBranch(), HistoryUtils.getLastRevision(),
				types(B_NAME, C_NAME));
		
		final Setup onBranch1 = setup.toBranch(branch1);
		onBranch1.checkInitial();
		
		HistoryUtils.setContextBranch(branch1);
		
		final Execution checkNoModification;
		final Execution checkModification;
		final Revision oldState;
		{
			Transaction tx = begin();
			setup.c2.setC1("c2-modification-1");
			onBranch1.c2.setC1("c2-modification-2");
			onBranch1.c2.addBC(onBranch1.b2);
			onBranch1.c2.addBC(onBranch1.b3);
			
			checkNoModification = new Execution() {
				@Override
				public void run() throws Exception {
					assertNull(setup.c2.getC1());
					assertNull(onBranch1.c2.getC1());
					
					assertEquals(set(), onBranch1.c2.getBC());
					assertEquals(set(), setup.c2.getBC());
					
					assertEquals(set(setup.c1), setup.b3.getBCSources());
					assertEquals(set(onBranch1.c1), onBranch1.b3.getBCSources());
				}
			};
			
			checkModification = new Execution() {
				@Override
				public void run() throws Exception {
					// Original and view of unbranched type are modified.
					assertEquals("c2-modification-1", setup.c2.getC1());
					assertEquals("c2-modification-2", onBranch1.c2.getC1());
					
					// Association changes are only visible on the branch, because
					// the association type is branched.
					assertEquals(set(onBranch1.b2, onBranch1.b3), onBranch1.c2.getBC());
					assertEquals(set(), setup.c2.getBC());
					
					assertEquals(set(setup.c1), setup.b3.getBCSources());
					assertEquals(set(onBranch1.c1, onBranch1.c2), onBranch1.b3.getBCSources());
				}
			};
			
			checkModification.run();
			inThread(checkNoModification);
			
			oldState = HistoryUtils.getLastRevision();
			
			commit(tx);
		}
		
		checkModification.run();
		inThread(checkModification);

		// Go back in time and check that every thing is unchanged.
		Setup beforeModification = setup.toRevision(oldState);
		
		beforeModification.checkInitial();
		assertEquals(set(), beforeModification.c2.getBC());
	}

    public static Test suite() {
		return suiteNeedsBranches(TestKABasedCacheBranching.class);
    }
	
}
