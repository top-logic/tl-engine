/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.ChannelObjectObserver;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests for {@link ChannelObjectObserver}, which observes the objects a {@link ViewChannel} holds.
 *
 * <p>
 * The changes are made in the {@link com.top_logic.knowledge.service.KnowledgeBase} of the test
 * scenario and delivered as {@link ModelChangeEvent}s by a {@link GlobalModelEventForwarder},
 * which is also the {@link ModelScope} the observer registers its listeners on: what the observer
 * reports is therefore what an application session sees.
 * </p>
 */
public class TestChannelObjectObserver extends AbstractDBKnowledgeBaseTest {

	/** The channel the observed objects are written to. */
	private ViewChannel _input;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The number of changes the observer reported. */
	private int _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_input = new DefaultViewChannel("input");
		_reported = 0;

		UpdateChain updates = kb().getUpdateChain();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), updates, relevance);
	}

	@Override
	protected void tearDown() throws Exception {
		_scope = null;
		_input = null;

		super.tearDown();
	}

	/**
	 * Tests that changing an attribute of the object on the channel is reported, although the
	 * channel value stays the same.
	 */
	public void testObjectChangeIsReported() throws Exception {
		BObj b1 = create("b1");
		_input.set(b1);
		ChannelObjectObserver observer = observer();
		observer.attach(_scope);

		change(b1, "b1 updated");

		assertEquals("Editing the object on the channel is reported.", 1, _reported);
	}

	/**
	 * Tests that a change of the channel value itself is not reported: the holder of the observer
	 * binds to the channel and reacts to a new value there.
	 */
	public void testChannelValueChangeIsNotReported() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		_input.set(b1);
		ChannelObjectObserver observer = observer();
		observer.attach(_scope);

		_input.set(b2);

		assertEquals("A new channel value is no object change.", 0, _reported);
	}

	/**
	 * Tests that a new channel value points the observation at the object now held: the object
	 * dropped from the channel is no longer observed, the one taken up is.
	 */
	public void testObservationFollowsTheChannelValue() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		_input.set(b1);
		ChannelObjectObserver observer = observer();
		observer.attach(_scope);

		_input.set(b2);

		change(b1, "b1 updated");
		assertEquals("The object dropped from the channel is not observed any more.", 0, _reported);

		change(b2, "b2 updated");
		assertEquals("The object now on the channel is observed.", 1, _reported);
	}

	/**
	 * Tests that a change of an object the channel never held is not reported.
	 */
	public void testUnrelatedObjectChangeIsNotReported() throws Exception {
		BObj b1 = create("b1");
		BObj other = create("other");
		_input.set(b1);
		ChannelObjectObserver observer = observer();
		observer.attach(_scope);

		change(other, "other updated");

		assertEquals("An object nobody displays is not observed.", 0, _reported);
	}

	/**
	 * Tests that a detached observer reports nothing.
	 */
	public void testDetachedObserverIsSilent() throws Exception {
		BObj b1 = create("b1");
		_input.set(b1);
		ChannelObjectObserver observer = observer();
		observer.attach(_scope);
		observer.detach();

		change(b1, "b1 updated");

		assertEquals("A suspended observation reports nothing.", 0, _reported);
	}

	/**
	 * Tests that a channel holding a collection of objects observes every member of it.
	 */
	public void testCollectionValueObservesEveryMember() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		_input.set(List.of(b1, b2));
		ChannelObjectObserver observer = observer();
		observer.attach(_scope);

		change(b1, "b1 updated");
		assertEquals("The first member of the value is observed.", 1, _reported);

		change(b2, "b2 updated");
		assertEquals("The second member of the value is observed.", 2, _reported);
	}

	/**
	 * An observer over {@link #_input} counting the changes it reports in {@link #_reported}.
	 */
	private ChannelObjectObserver observer() {
		return new ChannelObjectObserver(List.of(_input), Set.of(), () -> _reported++);
	}

	/**
	 * Creates a committed {@link BObj} with the given name, and delivers the pending events, so
	 * that the creation itself is not among the changes a later observation reports.
	 */
	private BObj create(String name) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
		return result;
	}

	/**
	 * Commits a new name for the given object and delivers the resulting change.
	 */
	private void change(BObj object, String name) throws Exception {
		Transaction tx = begin();
		try {
			object.setA1(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
	}

	/**
	 * Delivers the changes committed since the last call to the listeners of {@link #_scope}.
	 */
	private void deliverChanges() {
		_scope.synthesizeModelEvents();
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestChannelObjectObserver.class);
	}

}
