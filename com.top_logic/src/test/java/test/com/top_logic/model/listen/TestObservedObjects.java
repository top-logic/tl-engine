/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.listen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TransientObject;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.listen.ObservedObjects;

/**
 * Tests for {@link ObservedObjects}, the changing set of objects a {@link ModelListener} observes on
 * a {@link ModelScope}.
 *
 * <p>
 * Which listeners are registered is asserted against a {@link RecordingScope} logging every call
 * {@link ObservedObjects} makes. That the registered listeners are also the ones an application
 * session is informed through is asserted against a {@link GlobalModelEventForwarder} delivering the
 * changes committed to the {@link com.top_logic.knowledge.service.KnowledgeBase} of the test
 * scenario as {@link ModelChangeEvent}s.
 * </p>
 */
public class TestObservedObjects extends AbstractDBKnowledgeBaseTest {

	/**
	 * {@link ModelScope} logging every registration instead of delivering events.
	 */
	private static final class RecordingScope implements ModelScope {

		private final List<String> _calls = new ArrayList<>();

		@Override
		public boolean addModelListener(ModelListener listener) {
			_calls.add("add");
			return true;
		}

		@Override
		public boolean addModelListener(TLStructuredType type, ModelListener listener) {
			_calls.add("add:" + type);
			return true;
		}

		@Override
		public boolean addModelListener(TLObject object, ModelListener listener) {
			_calls.add("add:" + object.tId());
			return true;
		}

		@Override
		public boolean removeModelListener(ModelListener listener) {
			_calls.add("remove");
			return true;
		}

		@Override
		public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
			_calls.add("remove:" + type);
			return true;
		}

		@Override
		public boolean removeModelListener(TLObject object, ModelListener listener) {
			_calls.add("remove:" + object.tId());
			return true;
		}

		/**
		 * The registrations made so far, in the order they happened.
		 */
		public List<String> calls() {
			return _calls;
		}

		/**
		 * Forgets the {@link #calls()} made so far.
		 */
		public void reset() {
			_calls.clear();
		}

	}

	/** The scope logging the registrations. */
	private RecordingScope _log;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The listener that is registered for the observed objects. */
	private ModelListener _listener;

	/** The number of changes the {@link #_listener} received. */
	private int _reported;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_log = new RecordingScope();
		_reported = 0;
		_listener = event -> _reported++;

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
		_listener = null;
		_log = null;

		super.tearDown();
	}

	/**
	 * Tests that objects given while attached are registered right away.
	 */
	public void testObserveWhileAttachedRegisters() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);
		assertEquals("Nothing is registered before an object is given.", List.of(), _log.calls());

		observed.observe(List.of(b1));

		assertEquals("The given object is registered.", List.of("add:" + b1.tId()), _log.calls());
	}

	/**
	 * Tests that objects given before attaching are registered when attaching.
	 */
	public void testObserveBeforeAttachRegistersOnAttach() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);

		observed.observe(List.of(b1));
		assertEquals("Nothing is registered while detached.", List.of(), _log.calls());
		assertEquals("The object is remembered while detached.", List.of(b1),
			new ArrayList<>(observed.observedObjects()));

		observed.attach(_log);

		assertEquals("The remembered object is registered when attaching.", List.of("add:" + b1.tId()),
			_log.calls());
	}

	/**
	 * Tests that an object observed before and after a second call keeps its single registration,
	 * while the object dropped is deregistered and the object added is registered.
	 */
	public void testObserveIsADifference() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		BObj b3 = create("b3");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);
		observed.observe(List.of(b1, b2));
		_log.reset();

		observed.observe(List.of(b2, b3));

		assertEquals("The dropped object is deregistered, the added one registered, the kept one untouched.",
			List.of("remove:" + b1.tId(), "add:" + b3.tId()), _log.calls());
		assertEquals("The observed objects are the ones given last.", List.of(b2, b3),
			new ArrayList<>(observed.observedObjects()));
	}

	/**
	 * Tests that giving the same objects again registers nothing.
	 */
	public void testObserveTheSameObjectsAgainChangesNothing() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);
		observed.observe(List.of(b1));
		_log.reset();

		observed.observe(List.of(b1));

		assertEquals("An unchanged set keeps its registrations.", List.of(), _log.calls());
	}

	/**
	 * Tests that the same object listed twice is registered once.
	 */
	public void testDuplicateObjectIsObservedOnce() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);

		observed.observe(List.of(b1, b1));

		assertEquals("The duplicate registers a single listener.", List.of("add:" + b1.tId()), _log.calls());
		assertEquals("The duplicate is held once.", 1, observed.observedObjects().size());
	}

	/**
	 * Tests that an object without an identity is skipped.
	 */
	public void testObjectWithoutIdentityIsSkipped() throws Exception {
		BObj b1 = create("b1");
		TLObject transientObject = new TransientObject() {
			// An object without an identity.
		};
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);

		observed.observe(List.of(transientObject, b1));

		assertEquals("Only the object with an identity is registered.", List.of("add:" + b1.tId()), _log.calls());
		assertEquals("Only the object with an identity is held.", List.of(b1),
			new ArrayList<>(observed.observedObjects()));
	}

	/**
	 * Tests that a value is normalized to the objects it consists of.
	 */
	public void testObserveValueTakesTheObjectsOfTheValue() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);

		observed.observeValue(b1);
		assertEquals("A single object is observed.", List.of(b1), new ArrayList<>(observed.observedObjects()));

		observed.observeValue(List.of(b1, b2));
		assertEquals("Every object of a collection is observed.", List.of(b1, b2),
			new ArrayList<>(observed.observedObjects()));

		observed.observeValue("no object at all");
		assertEquals("A value holding no object observes none.", List.of(),
			new ArrayList<>(observed.observedObjects()));
	}

	/**
	 * Tests that detaching deregisters everything, keeps the set, and that attaching again
	 * registers it.
	 */
	public void testDetachDeregistersAndAttachRegistersAgain() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);
		observed.observe(List.of(b1));
		_log.reset();

		observed.detach();
		assertEquals("Detaching deregisters the observed object.", List.of("remove:" + b1.tId()), _log.calls());
		assertFalse("Detaching ends the observation.", observed.isAttached());
		assertEquals("Detaching keeps the observed objects.", List.of(b1),
			new ArrayList<>(observed.observedObjects()));
		_log.reset();

		observed.attach(_log);
		assertTrue("Attaching begins the observation.", observed.isAttached());
		assertEquals("Attaching again registers the kept object.", List.of("add:" + b1.tId()), _log.calls());
	}

	/**
	 * Tests that a set given while detached is registered when attaching again.
	 */
	public void testObserveWhileDetachedRegistersNothing() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);
		observed.observe(List.of(b1));
		observed.detach();
		_log.reset();

		observed.observe(List.of(b2));
		assertEquals("A detached observation registers nothing.", List.of(), _log.calls());

		observed.attach(_log);
		assertEquals("Attaching registers the object given while detached.", List.of("add:" + b2.tId()),
			_log.calls());
	}

	/**
	 * Tests that attaching and detaching twice changes nothing.
	 */
	public void testAttachAndDetachAreIdempotent() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_log);
		observed.observe(List.of(b1));
		_log.reset();

		observed.attach(_log);
		assertEquals("Attaching again registers nothing.", List.of(), _log.calls());

		observed.detach();
		_log.reset();
		observed.detach();
		assertEquals("Detaching again deregisters nothing.", List.of(), _log.calls());
	}

	/**
	 * Tests that a scope-less observation registers nothing and stays consistent.
	 */
	public void testObservationWithoutScope() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);

		observed.attach(null);
		observed.observe(List.of(b1));
		assertTrue("A scope-less observation is attached like any other.", observed.isAttached());
		assertEquals("A scope-less observation holds its objects.", List.of(b1),
			new ArrayList<>(observed.observedObjects()));

		observed.detach();
		assertFalse("A scope-less observation can be detached.", observed.isAttached());
	}

	/**
	 * Tests that a change of an observed object reaches the listener, while a change of an object
	 * never observed does not.
	 */
	public void testChangeOfObservedObjectIsReported() throws Exception {
		BObj b1 = create("b1");
		BObj other = create("other");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_scope);
		observed.observe(List.of(b1));

		change(b1, "b1 updated");
		assertEquals("Editing the observed object is reported.", 1, _reported);

		change(other, "other updated");
		assertEquals("An object nobody displays is not observed.", 1, _reported);
	}

	/**
	 * Tests that the object dropped from the observed set is not reported any more, while the one
	 * taken up is.
	 */
	public void testChangeOfDroppedObjectIsNotReported() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_scope);
		observed.observe(List.of(b1));

		observed.observe(List.of(b2));

		change(b1, "b1 updated");
		assertEquals("The dropped object is not observed any more.", 0, _reported);

		change(b2, "b2 updated");
		assertEquals("The object taken up is observed.", 1, _reported);
	}

	/**
	 * Tests that a detached observation reports nothing, and reports again after attaching.
	 */
	public void testDetachedObservationIsSilent() throws Exception {
		BObj b1 = create("b1");
		ObservedObjects observed = new ObservedObjects(_listener);
		observed.attach(_scope);
		observed.observe(List.of(b1));
		observed.detach();

		change(b1, "b1 updated");
		assertEquals("A suspended observation reports nothing.", 0, _reported);

		observed.attach(_scope);
		change(b1, "b1 updated again");
		assertEquals("A resumed observation reports again.", 1, _reported);
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
		return suiteDefaultDB(TestObservedObjects.class);
	}

}
