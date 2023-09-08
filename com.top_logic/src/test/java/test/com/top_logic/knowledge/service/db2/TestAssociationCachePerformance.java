/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;

import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;


/**
 * Tests of performance relevant behavior in the association caching.
 * 
 * @see KnowledgeBase#resolveLinks(KnowledgeObject, AbstractAssociationQuery)
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestAssociationCachePerformance extends AbstractDBKnowledgeBaseTest {

	/** In milliseconds */
	private static final long WAIT_TIMEOUT = 1000;

	/** Tests whether the association cache finds associations for uncommitted objects. */
	public void testPreCommitAssociationLookup() throws Throwable {
		// Create objects
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");

		// Create association
		KnowledgeAssociation bc1 = newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());

		// Change association
		setBC1(bc1, "bc1.bc1");
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());

		// Commit association
		tx.commit();
		assertEquals("Committed outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Committed incoming association not found.", 1, c1.getBCSources().size());
	}

	/** Test if deleting a local (i.e. uncommitted) association causes cache problems. */
	public void testDeleteLocalAssociation() throws Throwable {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");
		tx.commit();

		Transaction tx2 = begin();
		KnowledgeAssociation bc1 = newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());
		bc1.delete();
		tx2.commit();

		assertEquals("Deleted outgoing association found.", 0, b1.getBC().size());
		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
	}

	/** Test if deleting a local (i.e. uncommitted) association source causes cache problems. */
	public void testDeleteLocalAssociationSource() throws Throwable {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");
		tx.commit();

		Transaction tx2 = begin();
		newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());
		b1.tDelete();
		tx2.commit();

		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
	}

	/** Test if deleting an association causes cache problems. */
	public void testDeleteAssociation() throws Throwable {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");
		tx.commit();

		Transaction tx2 = begin();
		KnowledgeAssociation bc1 = newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());
		tx2.commit();

		Transaction tx3 = begin();
		bc1.delete();
		assertEquals("Deleted outgoing association found.", 0, b1.getBC().size());
		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
		tx3.commit();

		assertEquals("Deleted outgoing association found.", 0, b1.getBC().size());
		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
	}

	/** Test if deleting an association source causes cache problems. */
	public void testDeleteAssociationSource() throws Throwable {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");
		tx.commit();

		Transaction tx2 = begin();
		newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());
		tx2.commit();

		Transaction tx3 = begin();
		b1.tDelete();
		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
		tx3.commit();

		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
	}

	/** Test if rolling back a delete of an association causes cache problems. */
	public void testRollbackDeleteAssociation() throws Throwable {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");
		tx.commit();

		Transaction tx2 = begin();
		KnowledgeAssociation bc1 = newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());
		tx2.commit();

		Transaction tx3 = begin();
		bc1.delete();
		assertEquals("Deleted outgoing association found.", 0, b1.getBC().size());
		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
		tx3.rollback();

		assertEquals("Deleted outgoing association found.", 1, b1.getBC().size());
		assertEquals("Deleted incoming association found.", 1, c1.getBCSources().size());
	}

	/** Test if rolling back a delete of an association source causes cache problems. */
	public void testRollbackDeleteAssociationSource() throws Throwable {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");
		CObj c1 = CObj.newCObj("c1");
		tx.commit();

		Transaction tx2 = begin();
		newBC(b1.tHandle(), c1.tHandle());
		assertEquals("Local, uncommitted outgoing association not found.", 1, b1.getBC().size());
		assertEquals("Local, uncommitted incoming association not found.", 1, c1.getBCSources().size());
		tx2.commit();

		Transaction tx3 = begin();
		b1.tDelete();
		assertEquals("Deleted incoming association found.", 0, c1.getBCSources().size());
		tx3.rollback();

		assertEquals("Deleted outgoing association found.", 1, b1.getBC().size());
		assertEquals("Deleted incoming association found.", 1, c1.getBCSources().size());
	}

	/**
	 * Tests if two concurrent transactions which create each a new association to a common source
	 * object succeed, i.e. both associations have to exist after the commits. The destination of
	 * the association is a newly, uncommitted object. The association cache is explicitly touched
	 * before the commit.
	 */
	public void testConcurrentTransaction_NewDestination_WithCacheTouch() throws Throwable {
		Transaction tx1 = begin();
		final BObj b = BObj.newBObj("b");
		tx1.commit();

		final Barrier barrierPostNewObj = createBarrier(2);
		final Barrier barrierPostNewAssociation = createBarrier(2);
		final Barrier barrierPreCommit1 = createBarrier(2);
		final Barrier barrierPreCommit2 = createBarrier(2);
		final Barrier barrierPostCommit2 = createBarrier(2);

		TestFuture result1 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				CObj c = CObj.newCObj("c1");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.", 1, b.getBC().size());

				enter(barrierPreCommit1);
				tx2.commit();
				assertEquals("New association was lost through local commit.", 1, b.getBC().size());

				enter(barrierPreCommit2);

				// Commit of other Thread
				enter(barrierPostCommit2);

				assertEquals("Found foreign target before session update.", set(c), b.getBC());
				updateSessionRevision();

				Set bcDests = b.getBC();
				assertTrue("Association was lost through foreign commit.", bcDests.contains(c));
				assertEquals("Foreign association does not exist after foreign commit.", 2, bcDests.size());
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				CObj c = CObj.newCObj("c2");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.",
					1, b.getBC().size());

				enter(barrierPreCommit1);

				// Commit of other Thread
				enter(barrierPreCommit2);

				assertEquals("Found foreign destination before session update.", set(c), b.getBC());
				updateSessionRevision();

				assertEquals("Local, uncommitted association lost through foreign commit.",
					2, b.getBC().size());
				tx2.commit();
				Set bcDests = b.getBC();
				assertTrue("New association was lost through local commit.", bcDests.contains(c));
				assertEquals("Foreign association does not exist after local commit.", 2, bcDests.size());

				enter(barrierPostCommit2);
			}
		});

		result2.check(3 * WAIT_TIMEOUT);
		result1.check(3 * WAIT_TIMEOUT);
	}

	/**
	 * Tests if two concurrent transactions which create each a new association to a common
	 * destination object succeed, i.e. both associations have to exist after the commits. The
	 * source of the association is a newly, uncommitted object. The association cache is not
	 * touched between the commits of the two concurrent transactions.
	 */
	public void testConcurrentTransaction_NewSource_WithCacheTouch() throws Throwable {
		Transaction tx1 = begin();
		final CObj c = CObj.newCObj("c");
		tx1.commit();

		final Barrier barrierPostNewObj = createBarrier(2);
		final Barrier barrierPostNewAssociation = createBarrier(2);
		final Barrier barrierPreCommit1 = createBarrier(2);
		final Barrier barrierPreCommit2 = createBarrier(2);
		final Barrier barrierPostCommit2 = createBarrier(2);

		TestFuture result1 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				BObj b = BObj.newBObj("b1");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.", 1, c.getBCSources().size());

				enter(barrierPreCommit1);
				tx2.commit();
				assertEquals("New association was lost through local commit.", 1, c.getBCSources().size());

				enter(barrierPreCommit2);

				// Commit of other Thread
				enter(barrierPostCommit2);

				assertEquals("Found foreign source before session update.", set(b), c.getBCSources());
				updateSessionRevision();

				Set bcSources = c.getBCSources();
				assertTrue("Association was lost through foreign commit.", bcSources.contains(b));
				assertEquals("Foreign association does not exist after foreign commit.", 2, bcSources.size());
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				BObj b = CObj.newBObj("b2");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.",
					1, c.getBCSources().size());

				enter(barrierPreCommit1);

				// Commit of other Thread
				enter(barrierPreCommit2);
				tx2.commit();
				Set bcSources = c.getBCSources();
				assertTrue("New association was lost through local commit.", bcSources.contains(b));
				assertEquals("Foreign association does not exist after local commit.", 2, bcSources.size());

				enter(barrierPostCommit2);
			}
		});

		result1.check(3 * WAIT_TIMEOUT);
		result2.check(3 * WAIT_TIMEOUT);
	}

	/**
	 * Tests if two concurrent transactions which create each a new association to a common source
	 * object succeed, i.e. both associations have to exist after the commits. The destination of
	 * the association is a newly, uncommitted object. The association cache is not touched between
	 * the commits of the two concurrent transactions.
	 */
	public void testConcurrentTransaction_NewDestination_NoCacheTouchBefore2ndCommit() throws Throwable {
		Transaction tx1 = begin();
		final BObj b = BObj.newBObj("b");
		tx1.commit();

		final Barrier barrierPostNewObj = createBarrier(2);
		final Barrier barrierPostNewAssociation = createBarrier(2);
		final Barrier barrierPreCommit1 = createBarrier(2);
		final Barrier barrierPreCommit2 = createBarrier(2);
		final Barrier barrierPostCommit2 = createBarrier(2);

		TestFuture result1 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				CObj c = CObj.newCObj("c1");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.", 1, b.getBC().size());

				enter(barrierPreCommit1);
				tx2.commit();
				assertEquals("New association was lost through local commit.", 1, b.getBC().size());

				enter(barrierPreCommit2);

				// Commit of other Thread
				enter(barrierPostCommit2);

				assertEquals("Found foreign target before session update.", set(c), b.getBC());
				updateSessionRevision();

				Set bcDests = b.getBC();
				assertTrue("Association was lost through foreign commit.", bcDests.contains(c));
				assertEquals("Foreign association does not exist after foreign commit.", 2, bcDests.size());
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				CObj c = CObj.newCObj("c2");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.",
					1, b.getBC().size());

				enter(barrierPreCommit1);

				// Commit of other Thread
				enter(barrierPreCommit2);
				tx2.commit();
				Set bcDests = b.getBC();
				assertTrue("New association was lost through local commit.", bcDests.contains(c));
				assertEquals("Foreign association does not exist after local commit.", 2, bcDests.size());

				enter(barrierPostCommit2);
			}
		});

		result1.check(3 * WAIT_TIMEOUT);
		result2.check(3 * WAIT_TIMEOUT);
	}

	/**
	 * Tests if two concurrent transactions which create each a new association to a common
	 * destination object succeed, i.e. both associations have to exist after the commits. The
	 * source of the association is a newly, uncommitted object. The association cache is explicitly
	 * touched before the commit.
	 */
	public void testConcurrentTransaction_NewSource_NoCacheTouchBefore2ndCommit() throws Throwable {
		Transaction tx1 = begin();
		final CObj c = CObj.newCObj("c");
		tx1.commit();

		final Barrier barrierPostNewObj = createBarrier(2);
		final Barrier barrierPostNewAssociation = createBarrier(2);
		final Barrier barrierPreCommit1 = createBarrier(2);
		final Barrier barrierPreCommit2 = createBarrier(2);
		final Barrier barrierPostCommit2 = createBarrier(2);

		TestFuture result1 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				BObj b = BObj.newBObj("b1");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.", 1, c.getBCSources().size());

				enter(barrierPreCommit1);
				tx2.commit();
				assertEquals("New association was lost through local commit.", 1, c.getBCSources().size());

				enter(barrierPreCommit2);

				// Commit of other Thread
				enter(barrierPostCommit2);

				assertEquals("Found foreign source before session update.", set(b), c.getBCSources());
				updateSessionRevision();

				Set bcSources = c.getBCSources();
				assertTrue("Association was lost through foreign commit.", bcSources.contains(b));
				assertEquals("Foreign association does not exist after foreign commit.", 2, bcSources.size());
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				BObj b = CObj.newBObj("b2");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				assertEquals("New, uncommitted association not visible or not existing.",
					1, c.getBCSources().size());

				enter(barrierPreCommit1);

				// Commit of other Thread
				enter(barrierPreCommit2);
				tx2.commit();
				Set bcSources = c.getBCSources();
				assertTrue("New association was lost through local commit.", bcSources.contains(b));
				assertEquals("Foreign association does not exist after local commit.", 2, bcSources.size());

				enter(barrierPostCommit2);
			}
		});

		result1.check(3 * WAIT_TIMEOUT);
		result2.check(3 * WAIT_TIMEOUT);
	}

	/**
	 * Tests if two concurrent transactions which create each a new association to a common source
	 * object succeed, i.e. both associations have to exist after the commits. The destination of
	 * the association is a newly, uncommitted object. The association cache is not explicitly
	 * touched before the commit.
	 */
	public void testConcurrentTransaction_NewDestination_NoCacheTouch() throws Throwable {
		Transaction tx1 = begin();
		final BObj b = BObj.newBObj("b");
		tx1.commit();

		final Barrier barrierPostNewObj = createBarrier(2);
		final Barrier barrierPostNewAssociation = createBarrier(2);
		final Barrier barrierPostCommit1 = createBarrier(2);
		final Barrier barrierPostCommit2 = createBarrier(2);

		TestFuture result1 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				CObj c = CObj.newCObj("c1");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				tx2.commit();

				enter(barrierPostCommit1);

				// Commit of other Thread
				enter(barrierPostCommit2);

				assertEquals("Found foreign target before session update.", set(c), b.getBC());
				updateSessionRevision();

				Set bcDests = b.getBC();
				assertTrue("New association was lost through foreign commit.", bcDests.contains(c));
				assertEquals("Foreign association does not exist after foreign commit.", 2, bcDests.size());
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				CObj c = CObj.newCObj("c2");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);

				// Commit of other Thread
				enter(barrierPostCommit1);
				tx2.commit();
				Set bcDests = b.getBC();
				assertTrue("New association was lost through local commit.", bcDests.contains(c));
				assertEquals("Foreign association does not exist after local commit.", 2, bcDests.size());

				enter(barrierPostCommit2);
			}
		});

		result1.check(3 * WAIT_TIMEOUT);
		result2.check(3 * WAIT_TIMEOUT);
	}

	/**
	 * Tests if two concurrent transactions which create each a new association to a common
	 * destination object succeed, i.e. both associations have to exist after the commits. The
	 * source of the association is a newly, uncommitted object. The association cache is not
	 * explicitly touched before the commit.
	 */
	public void testConcurrentTransaction_NewSource_NoCacheTouch() throws Throwable {
		Transaction tx1 = begin();
		final CObj c = CObj.newCObj("c");
		tx1.commit();

		final Barrier barrierPostNewObj = createBarrier(2);
		final Barrier barrierPostNewAssociation = createBarrier(2);
		final Barrier barrierPostCommit1 = createBarrier(2);
		final Barrier barrierPostCommit2 = createBarrier(2);

		TestFuture result1 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				BObj b = BObj.newBObj("b1");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);
				tx2.commit();

				enter(barrierPostCommit1);

				// Commit of other Thread
				enter(barrierPostCommit2);

				assertEquals("Found foreign source before session update.", set(b), c.getBCSources());
				updateSessionRevision();

				Set bcSources = c.getBCSources();
				assertTrue("Association was lost through foreign commit.", bcSources.contains(b));
				assertEquals("Foreign association does not exist after foreign commit.", 2, bcSources.size());
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void run() throws Exception {
				Transaction tx2 = begin();
				BObj b = CObj.newBObj("b2");

				enter(barrierPostNewObj);
				newBC(b.tHandle(), c.tHandle());

				enter(barrierPostNewAssociation);

				// Commit of other Thread
				enter(barrierPostCommit1);
				tx2.commit();
				Set bcSources = c.getBCSources();
				assertTrue("New association was lost through local commit.", bcSources.contains(b));
				assertEquals("Foreign association does not exist after local commit.", 2, bcSources.size());

				enter(barrierPostCommit2);
			}
		});

		result1.check(3 * WAIT_TIMEOUT);
		result2.check(3 * WAIT_TIMEOUT);
	}

	private void enter(Barrier barrier) {
		try {
			boolean success = barrier.enter(WAIT_TIMEOUT);
			if (!success) {
				throw new RuntimeException("Entering barrier timed out after " + WAIT_TIMEOUT + " milliseconds.");
			}
		} catch (InterruptedException ex) {
			throw new RuntimeException("Entering barrier was interrupted: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates the suite of this test, which not only performs the test itself but also takes care
	 * of the test setup and teardown.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (!true) {
			return runOneTest(TestAssociationCachePerformance.class, "testConcurrentTransaction_NewSource_NoCacheTouch");
		}
		return suite(TestAssociationCachePerformance.class);
	}

}
