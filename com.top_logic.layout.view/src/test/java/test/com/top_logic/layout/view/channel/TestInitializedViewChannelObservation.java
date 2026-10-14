/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

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
import com.top_logic.layout.view.channel.InitializedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;

/**
 * Tests that an {@link InitializedViewChannel} is filled again when an object its input holds is
 * stored with a different value of what the initialization function reads, and keeps a written
 * value otherwise.
 *
 * <p>
 * The setup is the one of {@link TestDerivedViewChannelObservation}: the changes are committed to
 * the knowledge base of the test scenario and delivered by a {@link GlobalModelEventForwarder}.
 * </p>
 */
public class TestInitializedViewChannelObservation extends AbstractDBKnowledgeBaseTest {

	/** The attribute value the objects are created with. */
	private static final String OPEN = "open";

	/** A value stored to provoke a refill. */
	private static final String CLOSED = "closed";

	/** A value written into the channel by the user. */
	private static final String WRITTEN = "written";

	/** The channel holding the object the channel is initialized from. */
	private ViewChannel _input;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_input = new DefaultViewChannel("input");

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
	 * Tests that storing the attribute the function reads fills the channel again, replacing a
	 * value written before.
	 */
	public void testObjectChangeRefills() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		InitializedViewChannel channel = channel();
		channel.attach(_scope);
		assertEquals("The channel is filled from the object the input holds.", OPEN, channel.get());

		channel.set(WRITTEN);
		change(b1, CLOSED);

		assertEquals("The channel follows the attribute stored on that object.", CLOSED, channel.get());
	}

	/**
	 * Tests that a change of the object that leaves the result of the function as it is keeps the
	 * value written into the channel.
	 */
	public void testUnreadAttributeChangeKeepsWrittenValue() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		InitializedViewChannel channel = channel();
		channel.attach(_scope);

		channel.set(WRITTEN);
		Transaction tx = begin();
		try {
			b1.setA2("an attribute the function does not read");
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();

		assertEquals(WRITTEN, channel.get());
	}

	/**
	 * Tests that a detached channel is not filled, and that attaching it again catches up with a
	 * change made meanwhile.
	 */
	public void testAttachCatchesUpWithChangeMadeWhileDetached() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		InitializedViewChannel channel = channel();
		channel.attach(_scope);
		channel.detach();

		change(b1, CLOSED);
		assertEquals("A detached channel is not filled.", OPEN, channel.get());

		channel.attach(_scope);
		assertEquals("Attaching fills the channel for what the object is now.", CLOSED, channel.get());
	}

	/**
	 * Tests that attaching again without a change in between keeps a written value: the view
	 * displayed again shows what the user entered.
	 */
	public void testReattachKeepsWrittenValue() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		InitializedViewChannel channel = channel();
		channel.attach(_scope);
		channel.set(WRITTEN);

		channel.detach();
		channel.attach(_scope);

		assertEquals(WRITTEN, channel.get());
	}

	/**
	 * A channel over {@link #_input} initialized with the {@link BObj#getA1() first attribute} of
	 * the object the input holds.
	 */
	private InitializedViewChannel channel() {
		InitializedViewChannel result = new InitializedViewChannel("initialized");
		result.bind(List.of(_input), args -> args[0] instanceof BObj object ? object.getA1() : null, Set.of());
		return result;
	}

	/**
	 * Creates a committed {@link BObj} with the given first attribute, and delivers the pending
	 * events.
	 */
	private BObj create(String a1) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(a1);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
		return result;
	}

	/**
	 * Commits a new first attribute for the given object and delivers the resulting change.
	 */
	private void change(BObj object, String a1) throws Exception {
		Transaction tx = begin();
		try {
			object.setA1(a1);
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
		return suiteDefaultDB(TestInitializedViewChannelObservation.class);
	}

}
