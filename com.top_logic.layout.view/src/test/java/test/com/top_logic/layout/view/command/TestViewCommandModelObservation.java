/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Tests that a {@link ViewCommandModel} follows the object on its input channel: an executability
 * rule deciding by an attribute of that object is re-evaluated when the attribute is stored, while
 * the channel keeps pointing to the same object.
 *
 * <p>
 * The changes are committed to the {@link com.top_logic.knowledge.service.KnowledgeBase} of the
 * test scenario and delivered by a {@link GlobalModelEventForwarder}, which is also the
 * {@link ModelScope} the model attaches to: what the button shows here is therefore what it shows
 * in an application session.
 * </p>
 */
public class TestViewCommandModelObservation extends AbstractDBKnowledgeBaseTest {

	/** The value the rule accepts. */
	private static final String OPEN = "open";

	/** A value the rule rejects. */
	private static final String CLOSED = "closed";

	/** The channel holding the object the command works on. */
	private ViewChannel _input;

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The number of state changes the model reported. */
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
	 * Tests that storing the attribute the rule decides by hides the button, although the channel
	 * keeps holding the same object.
	 */
	public void testObjectChangeReEvaluatesExecutability() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		ViewCommandModel model = attachedModel();

		assertTrue("The rule accepts the object the channel holds.", model.isVisible());

		change(b1, CLOSED);

		assertFalse("The rule rejects the object after its attribute was stored.", model.isVisible());
		assertEquals("The button was told once that its state changed.", 1, _reported);
	}

	/**
	 * Tests that a detached model does not re-evaluate: the button is off screen, so what it would
	 * show is nobody's concern until it attaches again.
	 */
	public void testDetachedModelDoesNotReEvaluate() throws Exception {
		BObj b1 = create(OPEN);
		_input.set(b1);
		ViewCommandModel model = attachedModel();
		model.detach();

		change(b1, CLOSED);

		assertTrue("A detached button keeps the state it was left in.", model.isVisible());
		assertEquals("A detached button reports nothing.", 0, _reported);
	}

	/**
	 * Tests that a change of an object the channel never held leaves the button alone.
	 */
	public void testUnrelatedObjectChangeDoesNotReEvaluate() throws Exception {
		BObj b1 = create(OPEN);
		BObj other = create(OPEN);
		_input.set(b1);
		ViewCommandModel model = attachedModel();

		change(other, CLOSED);

		assertTrue("The object the channel holds is unchanged.", model.isVisible());
		assertEquals("An object nobody displays does not reach the button.", 0, _reported);
	}

	/**
	 * Tests that a new channel value points the observation at the object now held: the object
	 * dropped from the channel no longer re-evaluates the rule, the one taken up does.
	 */
	public void testObservationFollowsTheChannelValue() throws Exception {
		BObj b1 = create(OPEN);
		BObj b2 = create(OPEN);
		_input.set(b1);
		ViewCommandModel model = attachedModel();

		_input.set(b2);
		assertTrue("The rule accepts the object now on the channel.", model.isVisible());
		assertEquals("Both objects are accepted, so the state did not change.", 0, _reported);

		change(b1, CLOSED);
		assertTrue("The object dropped from the channel is not observed any more.", model.isVisible());
		assertEquals("The dropped object does not reach the button.", 0, _reported);

		change(b2, CLOSED);
		assertFalse("The object now on the channel is observed.", model.isVisible());
		assertEquals("The button was told once that its state changed.", 1, _reported);
	}

	/**
	 * A model over {@link #_input}, attached to {@link #_scope} and counting the state changes it
	 * reports in {@link #_reported}.
	 *
	 * <p>
	 * The counting starts after the attachment, so that only the re-evaluations a test provokes are
	 * counted.
	 * </p>
	 */
	private ViewCommandModel attachedModel() {
		ViewCommandModel model = ViewCommandModel.create((context, input) -> HandlerResult.DEFAULT_RESULT,
			TypedConfiguration.newConfigItem(ViewCommand.Config.class), _input, openObjects());
		model.attach(_scope);
		model.addStateChangeListener(() -> _reported++);
		return model;
	}

	/**
	 * A rule offering the command for a {@link BObj} whose {@link BObj#getA1() first attribute} is
	 * {@link #OPEN}, and hiding it for anything else.
	 */
	private static ViewExecutabilityRule openObjects() {
		return input -> input instanceof BObj object && OPEN.equals(object.getA1())
			? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
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
		return suiteDefaultDB(TestViewCommandModelObservation.class);
	}

}
