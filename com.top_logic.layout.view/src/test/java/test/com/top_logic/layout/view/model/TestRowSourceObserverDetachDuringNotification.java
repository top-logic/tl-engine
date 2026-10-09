/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;

/**
 * Tests that a {@link RowSourceObserver} detached by a listener of the channel it observes is not
 * notified by the notification that detached it.
 *
 * <p>
 * A control replacing its presentation in reaction to a channel change detaches the observer of the
 * outgoing presentation, while that observer is still pending in the running notification of the
 * same channel. The observer displays persistent objects observed through a real
 * {@link com.top_logic.model.listen.ModelScope}, so that running it after the detach would touch
 * the scope it no longer has.
 * </p>
 */
public class TestRowSourceObserverDetachDuringNotification extends AbstractDBKnowledgeBaseTest {

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), kb().getUpdateChain(), relevance);
	}

	@Override
	protected void tearDown() throws Exception {
		_scope = null;

		super.tearDown();
	}

	/**
	 * Tests that an observer whose input is a {@link DerivedViewChannel} is not run after an earlier
	 * listener of that channel has detached it within the same notification.
	 */
	public void testObserverDetachedByEarlierListenerIsNotRun() throws Exception {
		List<BObj> rows = List.of(create("r1"), create("r2"));

		DefaultViewChannel selection = new DefaultViewChannel("selection");
		DerivedViewChannel filter = new DerivedViewChannel("filter");
		filter.bind(List.of(selection), args -> args[0]);

		List<List<BObj>> delivered = new ArrayList<>();
		RowSourceObserver<BObj> observer = new RowSourceObserver<>(rows, args -> rows, Set.of(),
			List.<ViewChannel> of(filter), delivered::add);

		// The control replacing its presentation listens before the observer of that presentation.
		filter.addListener((sender, oldValue, newValue) -> observer.detach());
		observer.attach(_scope);
		assertEquals("The elements at hand are not delivered again.", List.of(), delivered);

		selection.set("other");

		assertEquals("A detached observer delivers nothing.", List.of(), delivered);
	}

	/**
	 * Tests that the observer of the presentation that replaces the detached one takes over: it
	 * follows the channel and the objects it displays.
	 */
	public void testReplacingObserverFollowsTheChannel() throws Exception {
		List<BObj> rows = List.of(create("r1"));

		DefaultViewChannel selection = new DefaultViewChannel("selection");
		DerivedViewChannel filter = new DerivedViewChannel("filter");
		filter.bind(List.of(selection), args -> args[0]);

		List<List<BObj>> outgoing = new ArrayList<>();
		RowSourceObserver<BObj> outgoingObserver = new RowSourceObserver<>(rows, args -> rows, Set.of(),
			List.<ViewChannel> of(filter), outgoing::add);
		List<List<BObj>> incoming = new ArrayList<>();
		RowSourceObserver<BObj> incomingObserver = new RowSourceObserver<>(rows, args -> rows, Set.of(),
			List.<ViewChannel> of(filter), incoming::add);

		filter.addListener((sender, oldValue, newValue) -> {
			outgoingObserver.detach();
			incomingObserver.attach(_scope);
		});
		outgoingObserver.attach(_scope);

		selection.set("other");
		assertEquals(List.of(), outgoing);
		assertEquals("The incoming observer is attached on unchanged elements.", List.of(), incoming);

		selection.set("next");
		assertEquals("The detached observer stays silent.", List.of(), outgoing);
		assertEquals("The incoming observer follows the channel.", List.of(rows), incoming);

		change(rows.get(0), "changed");
		assertEquals("The detached observer does not follow the objects.", List.of(), outgoing);
		assertEquals("The incoming observer follows the objects.", List.of(rows, rows), incoming);
	}

	/**
	 * Creates a committed {@link BObj} and delivers the pending events.
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
		_scope.synthesizeModelEvents();
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
		_scope.synthesizeModelEvents();
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestRowSourceObserverDetachDuringNotification.class);
	}

}
