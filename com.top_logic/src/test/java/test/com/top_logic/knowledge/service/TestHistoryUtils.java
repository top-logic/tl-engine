/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.merge.MergeConflictException;

/**
 * Tests {@link HistoryUtils}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestHistoryUtils extends AbstractDBKnowledgeBaseTest {

	public void testLastRevisions() throws DataObjectException {

		Transaction tx1 = begin();
		newB("b1");
		commit(tx1);
		Revision rev1 = tx1.getCommitRevision();

		assertEquals(list(), HistoryUtils.getLastRevisions(0));
		assertEquals(list(rev1), HistoryUtils.getLastRevisions(1));

		Transaction tx2 = begin();
		newB("b2");
		commit(tx2);
		Revision rev2 = tx2.getCommitRevision();

		assertEquals(list(), HistoryUtils.getLastRevisions(0));
		assertEquals(list(rev2), HistoryUtils.getLastRevisions(1));
		assertEquals(list(rev2, rev1), HistoryUtils.getLastRevisions(2));
	}

	public void testGetRevisions() throws DataObjectException {
		Transaction tx1 = begin();
		newB("b1");
		commit(tx1);
		Revision rev1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		newB("b2");
		commit(tx2);
		Revision rev2 = tx2.getCommitRevision();

		Transaction tx3 = begin();
		newB("b2");
		commit(tx3);
		Revision rev3 = tx3.getCommitRevision();

		Transaction tx4 = begin();
		newB("b2");
		commit(tx4);
		Revision rev4 = tx4.getCommitRevision();

		assertEquals(list(rev3, rev2), getRevisionList(rev2.getCommitNumber(), rev3.getCommitNumber() + 1));
		assertEquals(list(rev4, rev3, rev2, rev1), getRevisionList(rev1.getCommitNumber(), Revision.CURRENT_REV));
		assertEquals(list(), getRevisionList(rev1.getCommitNumber(), rev1.getCommitNumber()));

		assertEquals(list(rev3, rev2, rev1), HistoryUtils.getRevisions(rev2.getCommitNumber(), 1, 1));
	}

	public void testGetRevisionsByDate() throws DataObjectException, InterruptedException {
		Transaction tx1 = begin();
		newB("b1");
		commit(tx1);
		Revision rev1 = tx1.getCommitRevision();

		// ensure new commit date
		Thread.sleep(1);

		Transaction tx2 = begin();
		newB("b2");
		commit(tx2);
		Revision rev2 = tx2.getCommitRevision();

		Thread.sleep(10);
		long nonCommitTime = System.currentTimeMillis();
		Thread.sleep(10);

		Transaction tx3 = begin();
		newB("b2");
		commit(tx3);
		Revision rev3 = tx3.getCommitRevision();

		// ensure new commit date
		Thread.sleep(1);

		Transaction tx4 = begin();
		newB("b2");
		commit(tx4);
		Revision rev4 = tx4.getCommitRevision();

		assertEquals(list(rev3, rev2), getRevisionList(new Date(rev2.getDate()), new Date(rev3.getDate() + 1)));
		assertEquals(list(rev4, rev3, rev2, rev1),
			getRevisionList(new Date(rev1.getDate()), new Date(System.currentTimeMillis() + 5)));
		assertEquals(list(), getRevisionList(new Date(rev1.getDate()), new Date(rev1.getDate())));

		assertEquals(list(rev3), getRevisionList(new Date(nonCommitTime), new Date(rev3.getDate() + 1)));
		assertEquals(list(rev3, rev2, rev1), HistoryUtils.getRevisions(new Date(nonCommitTime), 1, 1));
	}

	private List<Revision> getRevisionList(long minCommitNumber, long maxCommitNumber) {
		try (CloseableIterator<Revision> revisionsIter = HistoryUtils.getRevisions(minCommitNumber, maxCommitNumber)) {
			return CollectionUtil.toList(revisionsIter);
		}
	}

	private List<Revision> getRevisionList(Date minDate, Date maxDate) {
		try (CloseableIterator<Revision> revisionsIter = HistoryUtils.getRevisions(minDate, maxDate)) {
			return CollectionUtil.toList(revisionsIter);
		}
	}

	public void testSimpleSetContextBranch() {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Branch b = kb().createBranch(trunk(), lastRevision(), null);
		Branch formerBranch = HistoryUtils.setContextBranch(b);
		assertEquals(b, HistoryUtils.getContextBranch());
		HistoryUtils.setContextBranch(formerBranch);
		assertEquals(formerBranch, HistoryUtils.getContextBranch());

	}

	public void testSessionRevision() throws MergeConflictException {
		Revision sessionRevision = HistoryUtils.getSessionRevision();
		final AtomicReference<Revision> rev = new AtomicReference<>();
		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction tx = begin();
				newB("b1");
				commit(tx);
				rev.set(tx.getCommitRevision());
			}
		});
		assertEquals("Session revision must not be changed spontaneously.", sessionRevision,
			HistoryUtils.getSessionRevision());
		Revision newSessionRevision = HistoryUtils.updateSessionRevision();
		assertEquals(rev.get(), newSessionRevision);
	}

	public static Test suite() {
		return suite(TestHistoryUtils.class);
	}

}

