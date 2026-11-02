/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.listen;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.SC;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.listener.Registration;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.impl.DefaultModelScope;
import com.top_logic.model.listen.impl.EventBuilder;
import com.top_logic.model.listen.impl.ModelChangeSummary;
import com.top_logic.model.listen.impl.ModelEventSettings;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link DefaultModelScope#isAffectedBy(ModelChangeSummary)} with summaries built by
 * {@link EventBuilder#summarize(ModelEventSettings, UpdateEvent)} from committed changes.
 */
public class TestModelScopeAffectedBy extends AbstractDBKnowledgeBaseTest {

	/**
	 * A change committed by {@link TestModelScopeAffectedBy#commit(Change)}.
	 */
	private interface Change {

		/**
		 * Performs the change in the current transaction.
		 */
		void run() throws Exception;

	}

	private static final ModelListener LISTENER = change -> {
		// Only registered, never notified in this test.
	};

	private UpdateChain _updates;

	private DefaultModelScope _scope;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new DefaultModelScope(ModelEventSettings.forKnowledgeBase(kb(), relevance));
		_updates = kb().getUpdateChain();
	}

	@Override
	protected void tearDown() throws Exception {
		_scope = null;
		_updates = null;

		super.tearDown();
	}

	/**
	 * An object listener is affected by an update of its object, but not by an update of another
	 * object.
	 */
	public void testObjectUpdate() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		Registration registration = _scope.addModelListener(b1, LISTENER);

		assertTrue("An update of the observed object affects the scope.",
			_scope.isAffectedBy(commit(() -> b1.setA1("b1 updated"))));
		assertFalse("An update of an object nobody observes does not affect the scope.",
			_scope.isAffectedBy(commit(() -> b2.setA1("b2 updated"))));

		registration.dispose();
		assertFalse("A disposed registration does not affect the scope.",
			_scope.isAffectedBy(commit(() -> b1.setA1("b1 updated again"))));
	}

	/**
	 * An object listener is affected by a link change that has its object as reference end.
	 */
	public void testLinkChangeOfReferenceEnd() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		BObj b3 = create("b3");
		_scope.addModelListener(b2, LISTENER);

		ModelChangeSummary linkAdded = commit(() -> b1.addAB(b2));
		assertEquals("Both ends of the created link are updated.", Set.of(b1.tId(), b2.tId()),
			linkAdded.getChangedObjects());
		assertTrue("Linking the observed object affects the scope.", _scope.isAffectedBy(linkAdded));

		assertTrue("Unlinking the observed object affects the scope.",
			_scope.isAffectedBy(commit(() -> b1.removeAB(b2))));
		assertFalse("A link between objects nobody observes does not affect the scope.",
			_scope.isAffectedBy(commit(() -> b1.addAB(b3))));
	}

	/**
	 * An update of a reference attribute affects the listeners of the former and of the new
	 * referenced object.
	 */
	public void testReferenceUpdateAffectsFormerAndNewTarget() throws Exception {
		DObj former = createD("former");
		DObj target = createD("target");
		DObj unrelated = createD("unrelated");
		SC[] referer = new SC[1];
		commit(() -> referer[0] = SC.newSCObj("referer", former.tHandle()));

		Registration formerRegistration = _scope.addModelListener(former, LISTENER);
		ModelChangeSummary retargeted = commit(() -> referer[0].tHandle()
			.setAttributeValue(KnowledgeBaseTestScenarioConstants.S_REFERENCE_ATTR, target.tHandle()));
		assertEquals("The referer and both the former and the new referenced object are updated.",
			Set.of(referer[0].tId(), former.tId(), target.tId()), retargeted.getChangedObjects());
		assertTrue("Changing the reference away from the observed object affects the scope.",
			_scope.isAffectedBy(retargeted));

		formerRegistration.dispose();

		Registration targetRegistration = _scope.addModelListener(target, LISTENER);
		assertTrue("Changing the reference to the observed object affects the scope.",
			_scope.isAffectedBy(retargeted));
		targetRegistration.dispose();

		_scope.addModelListener(unrelated, LISTENER);
		assertFalse("Changing a reference between other objects does not affect the scope.",
			_scope.isAffectedBy(retargeted));
	}

	/**
	 * An object listener is affected by the deletion of its object, a creation affects no object
	 * listener.
	 */
	public void testObjectDeletionAndCreation() throws Exception {
		BObj b1 = create("b1");
		_scope.addModelListener(b1, LISTENER);

		assertFalse("A creation of another object does not affect the scope.",
			_scope.isAffectedBy(commit(() -> BObj.newBObj("b2"))));
		assertTrue("A deletion of the observed object affects the scope.",
			_scope.isAffectedBy(commit(() -> b1.tDelete())));
	}

	/**
	 * A listener for all changes is affected by any change, until it is disposed.
	 */
	public void testGlobalListener() throws Exception {
		BObj b1 = create("b1");
		Registration registration = _scope.addModelListener(LISTENER);

		assertTrue("A creation affects a global listener.",
			_scope.isAffectedBy(commit(() -> BObj.newBObj("b2"))));
		assertTrue("An update affects a global listener.",
			_scope.isAffectedBy(commit(() -> b1.setA1("b1 updated"))));
		assertTrue("Even an empty change affects a global listener.",
			_scope.isAffectedBy(new ModelChangeSummary(Set.of(), Set.of())));

		registration.dispose();
		assertFalse("A disposed global listener is not affected.",
			_scope.isAffectedBy(commit(() -> b1.setA1("b1 updated again"))));
	}

	/**
	 * A type listener is affected by a change of an object of the type or one of its
	 * specializations, but not by a change of an object of an unrelated type.
	 */
	public void testTypeListener() {
		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		TLClass ticket = TLModelUtil.addClass(module, "Ticket");
		TLClass bug = TLModelUtil.addClass(module, "Bug");
		bug.getGeneralizations().add(ticket);
		TLClass criticalBug = TLModelUtil.addClass(module, "CriticalBug");
		criticalBug.getGeneralizations().add(bug);
		TLClass other = TLModelUtil.addClass(module, "Other");

		ModelChangeSummary criticalBugChanged = new ModelChangeSummary(Set.of(), List.of(criticalBug));
		ModelChangeSummary ticketChanged = new ModelChangeSummary(Set.of(), List.of(ticket));
		ModelChangeSummary otherChanged = new ModelChangeSummary(Set.of(), List.of(other));
		assertEquals("A change reaches the listeners of all generalizations.", Set.of(criticalBug, bug, ticket),
			criticalBugChanged.getAffectedTypes());

		Registration registration = _scope.addModelListener(ticket, LISTENER);
		assertTrue("A change of a transitive specialization affects a listener of the generalization.",
			_scope.isAffectedBy(criticalBugChanged));
		assertTrue("A change of the type itself affects a listener of the type.",
			_scope.isAffectedBy(ticketChanged));
		assertFalse("A change of an unrelated type does not affect the scope.",
			_scope.isAffectedBy(otherChanged));
		registration.dispose();
		assertFalse("A disposed type listener is not affected.", _scope.isAffectedBy(criticalBugChanged));

		_scope.addModelListener(bug, LISTENER);
		assertFalse("A change of a generalization does not affect a listener of a specialization.",
			_scope.isAffectedBy(ticketChanged));
	}

	private BObj create(String name) throws Exception {
		BObj[] result = new BObj[1];
		commit(() -> result[0] = BObj.newBObj(name));
		return result[0];
	}

	private DObj createD(String name) throws Exception {
		DObj[] result = new DObj[1];
		commit(() -> result[0] = DObj.newDObj(name));
		return result[0];
	}

	/**
	 * Commits the given change and summarizes it.
	 */
	private ModelChangeSummary commit(Change change) throws Exception {
		Transaction tx = begin();
		try {
			change.run();
			tx.commit();
		} finally {
			tx.rollback();
		}
		assertTrue("A commit creates an event.", _updates.next());
		ModelChangeSummary result = EventBuilder.summarize(_scope.settings(), _updates.getUpdateEvent());
		assertFalse("A commit creates a single event.", _updates.next());
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestModelScopeAffectedBy.class);
	}

}
