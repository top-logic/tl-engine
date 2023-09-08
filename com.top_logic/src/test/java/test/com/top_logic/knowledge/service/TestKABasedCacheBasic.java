/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.DatabaseTestSetup.DBType;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.AObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQueryUtil;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLObject;

/**
 * {@link TestCase} for association caching in the context of branching.
 * 
 * @see KnowledgeBase#resolveLinks(KnowledgeObject, AbstractAssociationQuery)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestKABasedCacheBasic extends AbstractDBKnowledgeBaseTest {
    
    public void testNavigateDeleted() {
    	final BObj b1;
    	final BObj b2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b1.addAB(b2);
			commit(tx);
		}
    	
    	assertEquals(set(b2), b1.getAB());
    	assertEquals(set(b1), b2.getABSources());
    	
    	KnowledgeObject b2Ko = b2.tHandle();
		{
			Transaction tx = begin();
			b2.tDelete();
			commit(tx);
		}
    	
    	assertEquals(set(), b1.getAB());
    	try {
    		b2.getABSources();
    		fail("Navigation of deleted objects must be prevented.");
    	} catch (IllegalStateException ex) {
    		// Expected.
    	}
    	
		try {
			AssociationQueryUtil.resolveWrappers(b2Ko, TLObject.class, AObj.AB_REFERENCES_ATTR);
    		fail("Navigation of deleted objects must be prevented.");
    	} catch (IllegalStateException ex) {
    		// Expected.
    	}
    }
    
	public void testHistoricNavigation() {
    	final BObj b1;
    	final BObj b2;
    	final BObj b3;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			commit(tx);
			// Revision r2 = tx.getCommitRevision();
		}
    	
		Revision r3;
		{
			Transaction tx = begin();
			b1.addAB(b2);
			commit(tx);
			r3 = tx.getCommitRevision();
		}
    	
		Revision r4;
		{
			Transaction tx = begin();
			b1.addAB(b3);
			commit(tx);
			r4 = tx.getCommitRevision();
		}
    	
		{
			Transaction tx = begin();
			b2.addAB(b1);
			commit(tx);
		}
    	
    	{
    		BObj b1_r3 = (BObj) WrapperHistoryUtils.getWrapper(r3, b1);
    		BObj b2_r3 = (BObj) WrapperHistoryUtils.getWrapper(r3, b2);
    		BObj b3_r3 = (BObj) WrapperHistoryUtils.getWrapper(r3, b3);
    		
    		assertEquals(set(b2_r3), b1_r3.getAB());
    		assertNotNull(b3_r3);
    	}
    	
    	{
	    	BObj b1_r4 = (BObj) WrapperHistoryUtils.getWrapper(r4, b1);
	    	BObj b2_r4 = (BObj) WrapperHistoryUtils.getWrapper(r4, b2);
	    	BObj b3_r4 = (BObj) WrapperHistoryUtils.getWrapper(r4, b3);
	    	
	    	assertEquals(set(b2_r4, b3_r4), b1_r4.getAB());
    	}
    }
    
	public void testLocalModifications() {
    	final BObj b1;
    	final BObj b2;
    	final BObj b3;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			
			// Must commit before accessing objects from other threads.
			commit(tx);
		}
    	
		{
			Transaction tx = begin();
			b1.addAB(b2);
			b1.addAB(b3);
			
			// Local view is correct.
			assertEquals(set(b2, b3), b1.getAB());
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Global view is untouched.
					assertEquals(set(), b1.getAB());
				}
			});
			
			commit(tx);
		}
    	
    	// Global view is correct.
    	assertEquals(set(b2, b3), b1.getAB());
    	
		{
			Transaction tx = begin();
			// Create local context.
			BObj b4 = BObj.newBObj("b4");
			
			// Trigger creation of localized cache.
			b1.addAB(b4);
			assertEquals(set(b2, b3, b4), b1.getAB());
			
			// Additional modification.
			b1.removeAB(b2);
			
			// Check that localized cache is still up to date.
			assertEquals(set(b3, b4), b1.getAB());
			
			rollback(tx);
		}
    }
    
    public void testRemoveNewAssociatedObject() throws Throwable {
    	final BObj b1;
    	final BObj b2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b1.addAB(b2);
			commit(tx);
		}
    	
		{
			Transaction tx = begin();
			// Create new object and associate with b1.
			final BObj b3 = BObj.newBObj("b3");
			b1.addAB(b3);
			
			// Create cache and check addition.
			assertEquals(set(b2, b3), b1.getAB());
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Check global cache.
					assertEquals(set(b2), b1.getAB());
				}
			});
			
			// Delete new object b3.
			b3.tDelete();
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Check global cache.
					assertEquals(set(b2), b1.getAB());
				}
			});
			
			// Check removal from cache.
			assertEquals(set(b2), b1.getAB());
			
			commit(tx);
		}
    	
    	assertEquals(set(b2), b1.getAB());
    	inThread(new Execution() {
    		@Override
			public void run() throws Exception {
    	    	// Check global cache.
    	    	assertEquals(set(b2), b1.getAB());
    		}
    	});
    }

	public void testRemoveNewAssociation() {
    	final BObj b1;
    	final BObj b2;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b1.addAB(b2);
			commit(tx);
		}
    	
		{
			// Create new object and associate with b1.
			Transaction tx = begin();
			final BObj b3 = BObj.newBObj("b3");
			b1.addAB(b3);
			
			// Create cache and check addition.
			assertEquals(set(b2, b3), b1.getAB());
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Check global cache.
					assertEquals(set(b2), b1.getAB());
				}
			});
			
			// Delete new association b1-b3.
			b1.removeAB(b3);
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Check global cache.
					assertEquals(set(b2), b1.getAB());
				}
			});
			
			// Check removal from cache.
			assertEquals(set(b2), b1.getAB());
			
			commit(tx);
		}
    	
    	assertEquals(set(b2), b1.getAB());
    	inThread(new Execution() {
    		@Override
			public void run() throws Exception {
    	    	// Check global cache.
    	    	assertEquals(set(b2), b1.getAB());
    		}
    	});
    }

    public void testRemoveNewlyAssociatedObject() throws Throwable {
    	final BObj b1;
    	final BObj b2;
    	final BObj b3;
		{
			Transaction tx = begin();
			b1 = BObj.newBObj("b1");
			b2 = BObj.newBObj("b2");
			b3 = BObj.newBObj("b3");
			b1.addAB(b2);
			commit(tx);
		}
    	
		{
			Transaction tx = begin();
			// Associate b3 with b1.
			b1.addAB(b3);
			
			// Create cache and check addition.
			assertEquals(set(b2, b3), b1.getAB());
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Check global cache.
					assertEquals(set(b2), b1.getAB());
				}
			});
			
			// Delete new object b3.
			b3.tDelete();
			
			inThread(new Execution() {
				@Override
				public void run() throws Exception {
					// Check global cache.
					assertEquals(set(b2), b1.getAB());
				}
			});
			
			// Check removal from cache.
			assertEquals(set(b2), b1.getAB());
			
			commit(tx);
		}
    	
    	assertEquals(set(b2), b1.getAB());
    	inThread(new Execution() {
    		@Override
			public void run() throws Exception {
    	    	// Check global cache.
    	    	assertEquals(set(b2), b1.getAB());
    		}
    	});
    }
    
	public static Test suite() {
		if (!true) {
			return runOneTest(TestKABasedCacheBasic.class, "testHistoricNavigation", DBType.MSSQL_DB);
		}
        return suite(TestKABasedCacheBasic.class);
    }
	
}
