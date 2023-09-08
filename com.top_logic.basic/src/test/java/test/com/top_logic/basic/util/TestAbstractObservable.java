/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import junit.framework.TestCase;

import com.top_logic.basic.util.AbstractListeners;
import com.top_logic.basic.util.AbstractObservable;

/**
 * Test case for {@link AbstractObservable}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAbstractObservable extends TestCase {

	interface Listener {

		void notifyAboutEvent(Event event);

	}

	static class Event {

		private final Model _model;

		public Event(Model model) {
			super();
			_model = model;
		}

		public Model getSender() {
			return _model;
		}

	}

	static final class Model extends AbstractObservable<Listener, Event> {

		public boolean addConcreteListener(Listener listener) {
			return addListener(listener);
		}

		public boolean removeConcreteListener(Listener listener) {
			return removeListener(listener);
		}

		public void sendEvent() {
			notifyListeners(new Event(this));
		}

		@Override
		protected void sendEvent(Listener listener, Event event) {
			listener.notifyAboutEvent(event);
		}
	}

	static class ListenerList extends AbstractListeners<Listener, Event> {
	
		@Override
		protected final void sendEvent(Listener listener, Event event) {
			listener.notifyAboutEvent(event);
		}
	
	}

	static class Observer implements Listener {
		private int _cnt;

		@Override
		public void notifyAboutEvent(Event event) {
			_cnt++;
		}

		public int getCnt() {
			return _cnt;
		}
	}

	static class RemoveOnNotify extends Observer {

		@Override
		public void notifyAboutEvent(Event event) {
			super.notifyAboutEvent(event);

			Model sender = event.getSender();
			sender.removeConcreteListener(this);
		}

	}

	static class TriggerOnNotify extends Observer {

		@Override
		public void notifyAboutEvent(Event event) {
			super.notifyAboutEvent(event);

			if (getCnt() == 1) {
				Model sender = event.getSender();
				sender.sendEvent();
			}
		}

	}

	public void testModifyInNotify() {
		Model model = new Model();

		RemoveOnNotify l1 = new RemoveOnNotify();
		RemoveOnNotify l2 = new RemoveOnNotify();

		model.addConcreteListener(l1);
		model.addConcreteListener(l2);

		model.sendEvent();
		assertEquals(1, l1.getCnt());
		assertEquals(1, l2.getCnt());
		model.sendEvent();
		assertEquals(1, l1.getCnt());
		assertEquals(1, l2.getCnt());
	}

	public void testRecursiveNotify() {
		Model model = new Model();
		Observer l1 = new Observer();
		TriggerOnNotify l2 = new TriggerOnNotify();
		Observer l3 = new Observer();
		model.addConcreteListener(l1);
		model.addConcreteListener(l2);
		model.addConcreteListener(l3);

		model.sendEvent();

		assertEquals(2, l1.getCnt());
		assertEquals(2, l2.getCnt());
		assertEquals(2, l3.getCnt());

		model.sendEvent();

		assertEquals(3, l1.getCnt());
		assertEquals(3, l2.getCnt());
		assertEquals(3, l3.getCnt());
	}

	public void testRegister() {
		Model model = new Model();
		RemoveOnNotify l1 = new RemoveOnNotify();

		assertTrue(model.addConcreteListener(l1));
		assertFalse(model.addConcreteListener(l1));
		assertTrue(model.removeConcreteListener(l1));
		assertFalse(model.removeConcreteListener(l1));
	}

	public void testRegisterPublic() {
		Observer l1 = new Observer();

		ListenerList list = new ListenerList();
		assertFalse(list.hasRegisteredListeners());
		assertFalse(list.isListenerRegistered(l1));

		list.addListener(l1);
		assertTrue(list.hasRegisteredListeners());
		assertTrue(list.isListenerRegistered(l1));

		list.removeListener(l1);
		assertFalse(list.hasRegisteredListeners());
		assertFalse(list.isListenerRegistered(l1));
	}

	public void testSendPublic() {
		Observer l1 = new Observer();
		ListenerList list = new ListenerList();
		list.addListener(l1);
		list.notifyListeners(new Event(null));
		assertEquals(1, l1.getCnt());
	}

}
