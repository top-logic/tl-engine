/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.basic.TLID;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.event.BranchEvent;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.CommitEvent;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.TLContext;

/**
 * The class {@link TestReplayWithBusinessCode} tests a {@link KnowledgeBase} replay with business
 * code execution between the replays which do additional commits.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReplayWithBusinessCode extends AbstractDBKnowledgeBaseMigrationTest implements
		KnowledgeBaseMigrationTestScenario {

	long currentBranch = TLContext.TRUNK_ID;

	private BranchEvent branch(int revision, long baseBranchId, long baseRevisionNumber, Set<String> typeNames) {
		final BranchEvent branchEvent = new BranchEvent(revision, ++currentBranch, baseBranchId, baseRevisionNumber);
		branchEvent.setBranchedTypeNames(typeNames);
		return branchEvent;
	}

	private BranchEvent branch(int revision, long baseBranchId, long baseRevisionNumber) {
		return branch(revision, baseBranchId, baseRevisionNumber, Collections.singleton(B_NAME));
	}

	private ObjectCreation create(int rev, TLID id) {
		return create(rev, new ObjectBranchId(1, type(B_NAME), id));
	}

	private ObjectCreation create(int rev, ObjectBranchId objectId) {
		final ObjectCreation result = new ObjectCreation(rev, objectId);
		result.setValue(A1_NAME, null, "a1-initial");
		return result;
	}

	private CommitEvent commit(int rev) {
		return TestKnowledgeEvent.newCommit(rev, TestKnowledgeEvent.class);
	}

	private void write(EventWriter replay, ChangeSet... changeSets) {
		for (ChangeSet cs : changeSets) {
			replay.write(cs);
		}
	}

	private EventWriter _writer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_writer = KBUtils.getReplayWriter(kbNode2());
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			_writer.close();
		} finally {
			/* Ensure that KnowledgBase is shut down in each case. */
			super.tearDown();
		}
	}

	public void testBranchCreation() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final TLID a1 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)));

		try {
			kbNode2().createBranch(kbNode2().getTrunk(), HistoryUtils.getLastRevision(kbNode2().getHistoryManager()),
				null);
		} catch (KnowledgeBaseRuntimeException ex) {
			/* Exptected due to the known bug in ticket #5503: Duplicate revision entry when
			 * creating branch. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #5503: Duplicate revision entry when creating branch.");

		write(_writer,
			new ChangeSet(3).setCommit(commit(3)));
	}

	public void testSeparateTX() throws DataObjectException {
		final TLID a1 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)));

		final Transaction tx = kbNode2().beginTransaction();
		final KnowledgeObject obj = newBNode2("a2");
		tx.commit();

		final TLID a2 = kbNode2().createID();
		try {
			write(_writer,
				new ChangeSet(4).add(create(4, a2)));
			fail("Business-Code-Commit must not increase revision number");
		} catch (RuntimeException ex) {
			// expected
		}

		write(_writer,
			new ChangeSet(3).setCommit(commit(3)),
			new ChangeSet(4).add(create(4, a2)).setCommit(commit(4)));

		assertEquals(3L, obj.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));

	}

	public void testBranchReplay() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		final TLID a1 = kbNode2().createID();
		final TLID a2 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)),
			new ChangeSet(3).add(branch(3, 1, 1)).setCommit(commit(3)),
			new ChangeSet(4).add(create(4, a2)).setCommit(commit(4)));
	}

	/**
	 * Tests deprecated API
	 */
	@SuppressWarnings("deprecation")
	public void testAnonymousTX() throws DataObjectException {
		final TLID a1 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)));

		kbNode2().begin();
		final KnowledgeObject obj = newBNode2("a2");
		kbNode2().commit();

		final TLID a2 = kbNode2().createID();
		write(_writer,
			new ChangeSet(3).add(create(3, a2)).setCommit(commit(3)));

		assertEquals(3L, obj.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
	}

	/**
	 * Tests deprecated API
	 */
	@SuppressWarnings("deprecation")
	public void testAutoBeginCommit() throws DataObjectException {
		final TLID a1 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)));

		final KnowledgeObject obj = newBNode2("a2");
		kbNode2().commit();

		final TLID a2 = kbNode2().createID();
		write(_writer,
			new ChangeSet(3).add(create(3, a2)).setCommit(commit(3)));
		assertEquals(3L, obj.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
	}

	public void testTXBeforeCommit() throws DataObjectException {
		final TLID a1 = kbNode2().createID();
		final TLID a2 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)),
			new ChangeSet(3).add(create(3, a2)));

		final Transaction tx = kbNode2().beginTransaction();
		final KnowledgeObject obj = newBNode2("a2");
		tx.commit();

		write(_writer,
			new ChangeSet(3).setCommit(commit(3)));
		assertEquals(3L, obj.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
	}

	public void testReplayAfterBusinessCode() throws DataObjectException {
		final TLID a1 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)));

		final Transaction tx = kbNode2().beginTransaction();
		final KnowledgeObject obj = newBNode2("a2");
		tx.commit();

		final TLID a2 = kbNode2().createID();
		write(_writer,
			new ChangeSet(3).add(create(3, a2)).setCommit(commit(3)));
		assertEquals(3L, obj.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
	}

	/**
	 * Tests deprecated API
	 */
	@SuppressWarnings("deprecation")
	public void testAnonymousTXBeforeCommit() throws DataObjectException {
		final TLID a1 = kbNode2().createID();
		final TLID a2 = kbNode2().createID();
		write(_writer,
			new ChangeSet(2).add(create(2, a1)).setCommit(commit(2)),
			new ChangeSet(3).add(create(3, a2)));

		kbNode2().begin();
		final KnowledgeObject obj = newBNode2("a2");
		kbNode2().commit();

		write(_writer,
			new ChangeSet(3).setCommit(commit(3)));
		assertEquals(3L, obj.getAttributeValue(BasicTypes.REV_CREATE_ATTRIBUTE_NAME));
	}

	public static Test suite() {
		return suite(TestReplayWithBusinessCode.class);
	}
}
