/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that a {@link DefaultViewChannel} drops an object it holds once that object is deleted,
 * while it is attached.
 *
 * <p>
 * The changes are made in the {@link com.top_logic.knowledge.service.KnowledgeBase} of the test
 * scenario and delivered as {@link ModelChangeEvent}s by a {@link GlobalModelEventForwarder}, which
 * is also the {@link ModelScope} the channel observes its objects on.
 * </p>
 */
public class TestDefaultViewChannelDeletion extends AbstractDBKnowledgeBaseTest {

	/** The channel under test. */
	private DefaultViewChannel _channel;

	/** The changes the channel announced, each as the list of its old and its new value. */
	private List<List<Object>> _announced;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** Collects the errors logged while the events are delivered. */
	private AssertNoErrorLogListener _errors;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_channel = new DefaultViewChannel("selection");
		_announced = new ArrayList<>();
		_channel.addListener((sender, oldValue, newValue) -> _announced.add(Arrays.asList(oldValue, newValue)));
		_errors = new AssertNoErrorLogListener(false);

		UpdateChain updates = kb().getUpdateChain();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), updates, relevance);
	}

	@Override
	protected void tearDown() throws Exception {
		_channel.detach();
		_errors.deactivate();
		_errors = null;
		_scope = null;
		_announced = null;
		_channel = null;

		super.tearDown();
	}

	/**
	 * Tests that deleting the object on an attached channel announces the change to
	 * <code>null</code>.
	 */
	public void testDeletedObjectIsDropped() throws Exception {
		BObj b1 = create("b1");
		_channel.set(b1);
		_channel.attach(_scope);
		_announced.clear();

		delete(b1);

		assertNull("The deleted object is no value any more.", _channel.get());
		assertEquals("The deletion is announced as an ordinary change.", List.of(Arrays.asList(b1, null)),
			_announced);
		_errors.assertNoErrorLogged("Delivering a deletion must not fail a listener.");
	}

	/**
	 * Tests that deleting a member of a set on an attached channel announces the set of the
	 * remaining members, in one change.
	 */
	public void testDeletedMemberOfSetIsDropped() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		_channel.set(new LinkedHashSet<>(List.of(b1, b2)));
		_channel.attach(_scope);
		_announced.clear();

		delete(b2);

		assertEquals("The deletion is announced once.", 1, _announced.size());
		Object value = _channel.get();
		assertTrue("A set stays a set, even with a single member left.", value instanceof Set);
		assertEquals(Set.of(b1), value);
		_errors.assertNoErrorLogged("Delivering a deletion must not fail a listener.");
	}

	/**
	 * Tests that a list of which every member is deleted stays a list, an empty one.
	 */
	public void testListOfDeletedMembersStaysAList() throws Exception {
		BObj b1 = create("b1");
		_channel.set(List.of(b1));
		_channel.attach(_scope);
		_announced.clear();

		delete(b1);

		assertEquals("A collection stays a collection, even without members.", List.of(), _channel.get());
		assertEquals(1, _announced.size());
	}

	/**
	 * Tests that a changed object and a value that is no object are left alone.
	 */
	public void testOtherValuesAreUntouched() throws Exception {
		BObj b1 = create("b1");
		BObj other = create("other");
		_channel.set(b1);
		_channel.attach(_scope);
		_announced.clear();

		change(b1, "b1 updated");
		assertSame("A changed object stays the value.", b1, _channel.get());

		_channel.set("text");
		_announced.clear();
		delete(other);

		assertEquals("A value that is no object is not touched.", "text", _channel.get());
		assertEquals(List.of(), _announced);
	}

	/**
	 * Tests that an object the channel no longer holds is not followed any more.
	 */
	public void testObservationFollowsTheValue() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		_channel.set(b1);
		_channel.attach(_scope);
		_channel.set(b2);
		_announced.clear();

		delete(b1);

		assertSame("Deleting an object the channel held before changes nothing.", b2, _channel.get());
		assertEquals(List.of(), _announced);
	}

	/**
	 * Tests that a detached channel keeps a deleted object, and announces its deletion when it is
	 * attached again.
	 */
	public void testDetachedChannelAnnouncesTheDeletionOnAttach() throws Exception {
		BObj b1 = create("b1");
		_channel.set(b1);
		_channel.attach(_scope);
		_channel.detach();
		_announced.clear();

		delete(b1);

		assertSame("A channel nobody displays observes nothing.", b1, _channel.get());
		assertEquals(List.of(), _announced);

		_channel.attach(_scope);

		assertNull("Attaching catches up with the deletion.", _channel.get());
		assertEquals(List.of(Arrays.asList(b1, null)), _announced);
	}

	/**
	 * Tests that a channel attached without a scope - a view built outside a browser window -
	 * observes nothing and fails nothing.
	 */
	public void testAttachWithoutScope() throws Exception {
		BObj b1 = create("b1");
		_channel.set(b1);
		_channel.attach(null);
		_announced.clear();

		delete(b1);

		assertSame("Without a scope, nothing is observed.", b1, _channel.get());
		assertEquals(List.of(), _announced);
		_channel.detach();
		_errors.assertNoErrorLogged("A channel without a scope must not fail.");
	}

	/**
	 * Tests that dropping a deleted object does not ask the veto listeners: nothing can be saved on
	 * an object that is gone.
	 */
	public void testDropIsNotVetoed() throws Exception {
		BObj b1 = create("b1");
		_channel.set(b1);
		List<Object> asked = new ArrayList<>();
		_channel.addVetoListener((sender, oldValue, newValue) -> {
			asked.add(newValue);
			return List.of();
		});
		_channel.attach(_scope);

		delete(b1);

		assertNull(_channel.get());
		assertEquals("The veto listeners are not asked for the drop.", List.of(), asked);
	}

	/**
	 * Creates a committed {@link BObj} with the given name, and delivers the pending events.
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
	 * Deletes the given object and delivers the resulting change.
	 */
	private void delete(BObj object) throws Exception {
		Transaction tx = begin();
		try {
			object.tDelete();
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
		return suiteDefaultDB(TestDefaultViewChannelDeletion.class);
	}

}
