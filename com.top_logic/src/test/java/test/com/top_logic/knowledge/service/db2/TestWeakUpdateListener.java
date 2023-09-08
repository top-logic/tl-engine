/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.AbstractWeakUpdateListener;

/**
 * The class {@link TestWeakUpdateListener} tests {@link AbstractWeakUpdateListener}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestWeakUpdateListener extends AbstractDBKnowledgeBaseTest {

	private static class CachingWeakUpdateListener extends AbstractWeakUpdateListener<Object> {

		List<Event> _receivedEvents = new ArrayList<>();

		public static class Event {

			public KnowledgeBase _sender;

			public UpdateEvent _event;

			public Object _referent;

		}

		public CachingWeakUpdateListener(Object referent, ReferenceQueue<? super Object> q) {
			super(referent, q);
		}

		public CachingWeakUpdateListener(Object referent) {
			super(referent);
		}

		@Override
		protected void internalUpdate(KnowledgeBase sender, Object referent, UpdateEvent event) {
			Event event2 = new Event();
			event2._sender = sender;
			event2._referent = referent;
			event2._event = event;
			_receivedEvents.add(event2);
		}

	}

	public void testListener() throws DataObjectException {
		Object o = new Object();
		CachingWeakUpdateListener l = new CachingWeakUpdateListener(o);
		kb().addUpdateListener(l);
		Transaction tx = begin();
		newB("b1");
		commit(tx);
		assertEquals(1, l._receivedEvents.size());
		assertSame(o, l._receivedEvents.get(0)._referent);

		l._receivedEvents.clear();
		kb().removeUpdateListener(l);
		Transaction tx2 = begin();
		newB("b1");
		commit(tx2);
		assertTrue("Received event whereas listener was removed.", l._receivedEvents.isEmpty());
	}

	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestWeakness() throws DataObjectException {
		Object o = new Object();
		ReferenceQueue<Object> q = new ReferenceQueue<>();
		CachingWeakUpdateListener l = new CachingWeakUpdateListener(o, q);
		kb().addUpdateListener(l);
		Transaction tx = begin();
		newB("b1");
		commit(tx);
		assertEquals(1, l._receivedEvents.size());
		assertSame(o, l._receivedEvents.get(0)._referent);

		// drop references to object
		l._receivedEvents.clear();
		o = null;

		provokeOutOfMemory();
		Reference<? extends Object> droppedReference = q.poll();
		assertNotNull("Weak reference was not cleared.", droppedReference);
		assertSame(l, droppedReference);
		Transaction tx2 = begin();
		newB("b1");
		commit(tx2);
		assertTrue("Received event whereas listener was removed as referent was dropped.", l._receivedEvents.isEmpty());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestWeakUpdateListener}.
	 */
	public static Test suite() {
		return suite(TestWeakUpdateListener.class);
	}

}

