/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.listen;

import java.lang.ref.WeakReference;
import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.db2.UpdateChainLink;
import com.top_logic.knowledge.service.db2.UpdateChainView;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelListener;

/**
 * Tests {@link GlobalModelEventForwarder#release()}: a released forwarder delivers no events and
 * keeps no {@link UpdateChainLink} of the {@link com.top_logic.knowledge.service.KnowledgeBase} in
 * memory.
 */
public class TestGlobalModelEventForwarderRelease extends AbstractDBKnowledgeBaseTest {

	/** Number of garbage collection attempts before a link is considered reachable. */
	private static final int GC_ATTEMPTS = 20;

	/** Pause between two garbage collection attempts in milliseconds. */
	private static final long GC_PAUSE_MS = 100;

	/** Number of commits made after the observed one, so that it is strictly behind the tail. */
	private static final int LATER_COMMITS = 3;

	/** The forwarder under test. */
	private GlobalModelEventForwarder _forwarder;

	/** The number of events the listener registered on {@link #_forwarder} received. */
	private int _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_reported = 0;
		UpdateChain updates = kb().getUpdateChain();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_forwarder = new GlobalModelEventForwarder(kb(), updates, relevance);
		ModelListener listener = event -> _reported++;
		_forwarder.addModelListener(listener);
	}

	@Override
	protected void tearDown() throws Exception {
		_forwarder = null;

		super.tearDown();
	}

	/**
	 * Tests that a commit reaches the listener of a forwarder that is not released.
	 */
	public void testCommitIsDelivered() throws Exception {
		create("b1");

		assertTrue("The commit is delivered.", _forwarder.synthesizeModelEvents());
		assertEquals("The listener is informed about the commit.", 1, _reported);
	}

	/**
	 * Tests that a released forwarder delivers no commit.
	 */
	public void testReleasedForwarderDeliversNothing() throws Exception {
		_forwarder.release();

		create("b1");

		assertFalse("A released forwarder delivers nothing.", _forwarder.synthesizeModelEvents());
		assertEquals("The listener of a released forwarder is not informed.", 0, _reported);
	}

	/**
	 * Tests that releasing a forwarder twice is harmless.
	 */
	public void testReleaseIsIdempotent() throws Exception {
		_forwarder.release();
		_forwarder.release();

		create("b1");

		assertFalse("A twice released forwarder delivers nothing.", _forwarder.synthesizeModelEvents());
		assertEquals("The listener of a twice released forwarder is not informed.", 0, _reported);
	}

	/**
	 * Tests that a forwarder that is neither advanced nor released keeps every later commit in
	 * memory.
	 */
	public void testUnreleasedForwarderKeepsLaterCommits() throws Exception {
		WeakReference<UpdateChainLink> link = commitBehindForwarder();

		assertFalse("A forwarder that is not advanced keeps later commits in memory.", isCollected(link));
	}

	/**
	 * Tests that a released forwarder keeps no later commit in memory, although it is still
	 * referenced.
	 */
	public void testReleasedForwarderKeepsNoCommit() throws Exception {
		WeakReference<UpdateChainLink> link = commitBehindForwarder();

		_forwarder.release();

		assertTrue("A released forwarder keeps no commit in memory.", isCollected(link));
		assertFalse("The still referenced forwarder delivers nothing.", _forwarder.synthesizeModelEvents());
	}

	/**
	 * Commits after the position of {@link #_forwarder}, then commits further, so that the
	 * {@link UpdateChainLink} of the first commit is behind the tail of the update chain and behind
	 * the session revision of the test.
	 *
	 * @return A weak reference to the {@link UpdateChainLink} of the first commit.
	 */
	private WeakReference<UpdateChainLink> commitBehindForwarder() throws Exception {
		create("observed");
		WeakReference<UpdateChainLink> result =
			new WeakReference<>(((UpdateChainView) kb().getUpdateChain()).current());
		for (int n = 0; n < LATER_COMMITS; n++) {
			create("later" + n);
		}
		kb().getHistoryManager().updateSessionRevision();
		return result;
	}

	/**
	 * Runs the garbage collector until the given reference is cleared or the attempts are
	 * exhausted.
	 */
	private static boolean isCollected(WeakReference<?> ref) throws InterruptedException {
		for (int n = 0; n < GC_ATTEMPTS; n++) {
			System.gc();
			if (ref.get() == null) {
				return true;
			}
			Thread.sleep(GC_PAUSE_MS);
		}
		return ref.get() == null;
	}

	/**
	 * Creates and commits a {@link BObj} with the given name.
	 */
	private void create(String name) throws Exception {
		Transaction tx = begin();
		try {
			BObj.newBObj(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestGlobalModelEventForwarderRelease.class);
	}

}
