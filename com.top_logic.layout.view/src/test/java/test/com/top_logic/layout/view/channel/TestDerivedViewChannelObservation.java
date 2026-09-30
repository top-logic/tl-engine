/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that a {@link DerivedViewChannel} follows the objects its input channels hold: a function
 * reading an attribute of such an object recomputes when the attribute is stored, while the channel
 * keeps pointing to the same object.
 *
 * <p>
 * The changes are committed to the {@link com.top_logic.knowledge.service.KnowledgeBase} of the
 * test scenario and delivered by a {@link GlobalModelEventForwarder}, which is also the
 * {@link ModelScope} the channel attaches to: what the channel reports here is therefore what it
 * reports in an application session.
 * </p>
 */
public class TestDerivedViewChannelObservation extends AbstractDBKnowledgeBaseTest {

	/** The attribute value the objects are created with. */
	private static final String OPEN = "open";

	/** A value stored to provoke a recomputation. */
	private static final String CLOSED = "closed";

	/** The channel holding the object the derived value is computed from. */
	private ViewChannel _input;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The number of value changes the derived channel reported. */
	private int _reported;

	/** The previous value of the last reported change. */
	private Object _oldValue;

	/** The new value of the last reported change. */
	private Object _newValue;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_input = new DefaultViewChannel("input");
		_reported = 0;
		_oldValue = null;
		_newValue = null;

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
	 * Tests that storing the attribute the function reads recomputes the value and reports the
	 * change, although the channel keeps holding the same object.
	 */
	public void testObjectChangeRecomputes() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		DerivedViewChannel derived = attachedChannel();

		assertEquals("The value is computed from the object the channel holds.", OPEN, derived.get());

		change(b1, CLOSED);

		assertEquals("The value follows the attribute stored on that object.", CLOSED, derived.get());
		assertEquals("The change was reported once.", 1, _reported);
		assertEquals("The report names the value the channel had.", OPEN, _oldValue);
		assertEquals("The report names the value the channel has.", CLOSED, _newValue);
	}

	/**
	 * Tests that a change of the object leaving the derived value as it is reports nothing: what
	 * the display shows did not change.
	 */
	public void testUnreadAttributeChangeIsNotReported() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		DerivedViewChannel derived = attachedChannel();

		Transaction tx = begin();
		try {
			b1.setA2("an attribute the function does not read");
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();

		assertEquals("The recomputed value is the value the channel had.", OPEN, derived.get());
		assertEquals("A recomputation to an equal value is no change.", 0, _reported);
	}

	/**
	 * Tests that a detached channel does not recompute: its view is off screen, so the value it
	 * would show is nobody's concern until the view is displayed again.
	 */
	public void testDetachedChannelDoesNotRecompute() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		DerivedViewChannel derived = attachedChannel();
		derived.detach();

		change(b1, CLOSED);

		assertEquals("A detached channel keeps the value it was left with.", OPEN, derived.get());
		assertEquals("A detached channel reports nothing.", 0, _reported);
	}

	/**
	 * Tests that attaching catches up with a change made while detached: the value the view is
	 * built from is the current one, and the catch-up is reported.
	 */
	public void testAttachCatchesUpWithTheChangeMadeWhileDetached() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);

		DerivedViewChannel derived = channel();
		derived.addListener(this::record);

		change(b1, CLOSED);
		assertEquals("A channel that was never attached observed nothing.", OPEN, derived.get());
		assertEquals("A channel that was never attached reported nothing.", 0, _reported);

		derived.attach(_scope);

		assertEquals("Attaching recomputes for what the object is now.", CLOSED, derived.get());
		assertEquals("The catch-up was reported once.", 1, _reported);
		assertEquals("The report names the value the channel had.", OPEN, _oldValue);
		assertEquals("The report names the value the channel has.", CLOSED, _newValue);
	}

	/**
	 * Tests that a new channel value points the observation at the object now held: the object
	 * dropped from the channel no longer recomputes the value, the one taken up does.
	 */
	public void testObservationFollowsTheChannelValue() throws Exception {
		BObj b1 = create(OPEN);
		BObj b2 = create(OPEN);
		_input.set(b1);
		DerivedViewChannel derived = attachedChannel();

		_input.set(b2);
		assertEquals("Both objects have the same attribute, so the value did not change.", OPEN, derived.get());
		assertEquals("An unchanged value is not reported.", 0, _reported);

		change(b1, CLOSED);
		assertEquals("The object dropped from the channel is not observed any more.", OPEN, derived.get());
		assertEquals("The dropped object does not reach the channel.", 0, _reported);

		change(b2, CLOSED);
		assertEquals("The object now on the channel is observed.", CLOSED, derived.get());
		assertEquals("The change was reported once.", 1, _reported);
	}

	/**
	 * A channel over {@link #_input} computing the {@link BObj#getA1() first attribute} of the
	 * object the channel holds.
	 */
	private DerivedViewChannel channel() {
		DerivedViewChannel result = new DerivedViewChannel("derived");
		result.bind(List.of(_input), args -> args[0] instanceof BObj object ? object.getA1() : null);
		return result;
	}

	/**
	 * A {@link #channel()} attached to {@link #_scope}, reporting its changes to {@link #record}.
	 *
	 * <p>
	 * The reporting starts after the attachment, so that only the changes a test provokes are
	 * counted.
	 * </p>
	 */
	private DerivedViewChannel attachedChannel() {
		DerivedViewChannel result = channel();
		result.attach(_scope);
		result.addListener(this::record);
		return result;
	}

	/**
	 * Records a reported change in {@link #_reported}, {@link #_oldValue} and {@link #_newValue}.
	 */
	private void record(ViewChannel sender, Object oldValue, Object newValue) {
		_reported++;
		_oldValue = oldValue;
		_newValue = newValue;
	}

	/**
	 * Creates a committed {@link BObj} with the given first attribute, and delivers the pending
	 * events, so that the creation itself is not among the changes a later observation reports.
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
		return suiteDefaultDB(TestDerivedViewChannelObservation.class);
	}

}
