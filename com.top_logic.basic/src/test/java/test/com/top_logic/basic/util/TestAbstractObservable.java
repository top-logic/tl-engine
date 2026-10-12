/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.listener.ListenerRegistration;
import com.top_logic.basic.listener.Registration;
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

		public ListenerRegistration<Listener> registerConcreteListener(Listener listener) {
			return register(listener);
		}

		public List<ListenerRegistration<Listener>> getRegistrations() {
			return registrations();
		}

		public boolean hasAnyListener() {
			return hasListeners();
		}

		public boolean hasConcreteListener(Listener listener) {
			return hasListener(listener);
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

		@SuppressWarnings("deprecation")
		boolean removed = list.removeListener(l1);
		assertTrue(removed);
		assertFalse(list.hasRegisteredListeners());
		assertFalse(list.isListenerRegistered(l1));
	}

	public void testDisposeOtherInNotify() {
		Model model = new Model();
		Observer l2 = new Observer();
		Registration[] r2 = new Registration[1];
		Observer l1 = new Observer() {
			@Override
			public void notifyAboutEvent(Event event) {
				super.notifyAboutEvent(event);
				r2[0].dispose();
			}
		};
		model.registerConcreteListener(l1);
		r2[0] = model.registerConcreteListener(l2);
		Observer l3 = new Observer();
		model.registerConcreteListener(l3);

		model.sendEvent();
		assertEquals(1, l1.getCnt());
		assertEquals("Disposed during notification, must not be called.", 0, l2.getCnt());
		assertEquals(1, l3.getCnt());
		assertFalse(r2[0].isActive());
	}

	public void testRemoveOtherInNotify() {
		Model model = new Model();
		Observer l2 = new Observer();
		Observer l1 = new Observer() {
			@Override
			public void notifyAboutEvent(Event event) {
				super.notifyAboutEvent(event);
				event.getSender().removeConcreteListener(l2);
			}
		};
		model.addConcreteListener(l1);
		model.addConcreteListener(l2);

		model.sendEvent();
		assertEquals(1, l1.getCnt());
		assertEquals(0, l2.getCnt());
		assertFalse(model.hasConcreteListener(l2));
	}

	public void testRegisterInNotify() {
		Model model = new Model();
		Observer added = new Observer();
		Observer l1 = new Observer() {
			@Override
			public void notifyAboutEvent(Event event) {
				super.notifyAboutEvent(event);
				if (getCnt() == 1) {
					event.getSender().registerConcreteListener(added);
				}
			}
		};
		model.registerConcreteListener(l1);

		model.sendEvent();
		assertEquals(1, l1.getCnt());
		assertEquals("Not notified in the run it was registered in.", 0, added.getCnt());

		model.sendEvent();
		assertEquals(2, l1.getCnt());
		assertEquals(1, added.getCnt());
	}

	public void testDisposeIdempotent() {
		Model model = new Model();
		Observer l1 = new Observer();
		Observer l2 = new Observer();
		Registration r1 = model.registerConcreteListener(l1);
		model.registerConcreteListener(l2);

		assertTrue(r1.isActive());
		r1.dispose();
		assertFalse(r1.isActive());
		r1.dispose();
		assertFalse(r1.isActive());
		assertTrue(model.hasAnyListener());
		assertEquals(1, model.getRegistrations().size());

		model.sendEvent();
		assertEquals(0, l1.getCnt());
		assertEquals(1, l2.getCnt());
	}

	public void testDuplicateRegister() {
		Model model = new Model();
		Observer l1 = new Observer();
		ListenerRegistration<Listener> r1 = model.registerConcreteListener(l1);
		ListenerRegistration<Listener> r2 = model.registerConcreteListener(l1);
		assertNotSame(r1, r2);
		assertSame(l1, r1.getListener());

		model.sendEvent();
		assertEquals(2, l1.getCnt());

		r1.dispose();
		assertNull(r1.getListener());
		assertTrue(r2.isActive());
		assertTrue(model.hasConcreteListener(l1));
		model.sendEvent();
		assertEquals(3, l1.getCnt());

		r2.dispose();
		assertFalse(model.hasConcreteListener(l1));
		assertFalse(model.hasAnyListener());
	}

	public void testRemoveDisposesAllRegistrations() {
		Model model = new Model();
		Observer l1 = new Observer();
		Registration r1 = model.registerConcreteListener(l1);
		Registration r2 = model.registerConcreteListener(l1);
		assertFalse(model.addConcreteListener(l1));

		assertTrue(model.removeConcreteListener(l1));
		assertFalse(r1.isActive());
		assertFalse(r2.isActive());
		assertFalse(model.hasAnyListener());
	}

	public void testRegistrationsSnapshot() {
		Model model = new Model();
		Observer l1 = new Observer();
		Observer l2 = new Observer();
		model.registerConcreteListener(l1);
		Registration r2 = model.registerConcreteListener(l2);

		List<ListenerRegistration<Listener>> snapshot = model.getRegistrations();
		assertEquals(2, snapshot.size());

		r2.dispose();
		model.registerConcreteListener(new Observer());
		assertEquals(2, snapshot.size());
		assertTrue(snapshot.get(0).isActive());
		assertFalse(snapshot.get(1).isActive());
	}

	public void testDisposeMany() {
		Model model = new Model();
		int cnt = 200_000;
		List<Registration> registrations = new ArrayList<>(cnt);
		Observer observer = new Observer();
		for (int n = 0; n < cnt; n++) {
			registrations.add(model.registerConcreteListener(observer));
		}
		Observer survivor = new Observer();
		model.registerConcreteListener(survivor);

		// Dispose from the end, from the start, and interleaved with notifications.
		for (int n = cnt - 1; n >= cnt / 2; n--) {
			registrations.get(n).dispose();
		}
		model.sendEvent();
		assertEquals(cnt / 2, observer.getCnt());
		for (int n = 0; n < cnt / 2; n++) {
			registrations.get(n).dispose();
		}
		model.sendEvent();
		assertEquals(cnt / 2, observer.getCnt());
		assertEquals(2, survivor.getCnt());
		assertEquals(1, model.getRegistrations().size());
	}

	public void testDisposeAllInNotify() {
		Model model = new Model();
		int cnt = 10;
		List<Registration> registrations = new ArrayList<>();
		List<Observer> observers = new ArrayList<>();
		Observer disposer = new Observer() {
			@Override
			public void notifyAboutEvent(Event event) {
				super.notifyAboutEvent(event);
				// Triggers compaction while the notification iterates.
				for (Registration registration : registrations) {
					registration.dispose();
				}
				event.getSender().registerConcreteListener(observers.get(0));
			}
		};
		Registration self = model.registerConcreteListener(disposer);
		for (int n = 0; n < cnt; n++) {
			Observer observer = new Observer();
			observers.add(observer);
			registrations.add(model.registerConcreteListener(observer));
		}

		model.sendEvent();
		self.dispose();
		for (Observer observer : observers) {
			assertEquals(0, observer.getCnt());
		}

		model.sendEvent();
		assertEquals(1, observers.get(0).getCnt());
		assertEquals(1, model.getRegistrations().size());
	}

	public void testRegisterHandlePublic() {
		Observer l1 = new Observer();
		ListenerList list = new ListenerList();
		Registration registration = list.register(l1);
		assertTrue(list.isListenerRegistered(l1));
		assertEquals(1, list.getRegistrations().size());
		registration.dispose();
		assertFalse(list.hasRegisteredListeners());
		list.notifyListeners(new Event(null));
		assertEquals(0, l1.getCnt());
	}

	public void testSendPublic() {
		Observer l1 = new Observer();
		ListenerList list = new ListenerList();
		list.addListener(l1);
		list.notifyListeners(new Event(null));
		assertEquals(1, l1.getCnt());
	}

}
