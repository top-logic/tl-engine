/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.listener;

import junit.framework.TestCase;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Test for {@link PropertyObservableBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TestPropertyObservable<O extends TestPropertyObservable.Observable> extends TestCase {

	/**
	 * The observable interface.
	 */
	interface Observable extends PropertyObservable {

		/**
		 * The {@link EventType} for registering {@link Listener}s observing {@link #getProperty1()}.
		 */
		EventType<Listener, Observable, String> PROPERTY_1 =
			new EventType<>("property1") {
				@Override
				public Bubble dispatch(Listener listener, Observable sender, String oldValue, String newValue) {
					return listener.handleProperty1Change(sender, oldValue, newValue);
				}
			};

		/**
		 * {@link EventType} for registering {@link Listener}s observing {@link #getProperty2()}.
		 */
		EventType<Listener, Observable, Integer> PROPERTY_2 =
			new EventType<>("property2") {
				@Override
				public Bubble dispatch(Listener listener, Observable sender, Integer oldValue, Integer newValue) {
					return listener.handleProperty2Change(sender, oldValue, newValue);
				}
			};

		/**
		 * The listener interface for notifying about changes to the
		 * {@link TestPropertyObservable.Observable#getProperty1()}.
		 */
		interface Listener extends PropertyListener {

			Bubble handleProperty1Change(Observable sender, String oldValue, String newValue);

			Bubble handleProperty2Change(Observable sender, Integer oldValue, Integer newValue);

		}

		/**
		 * Getter for observable string property.
		 */
		String getProperty1();

		/**
		 * Setter for the observable property {@link #getProperty1()}.
		 */
		void setProperty1(String newValue);

		/**
		 * Getter for observable int property.
		 */
		int getProperty2();

		/**
		 * Setter for the observable property {@link #getProperty1()}.
		 */
		void setProperty2(int newValue);
	}

	/**
	 * Some arbitrary {@link Observable.Listener} implementation.
	 */
	static class CountingListener implements Observable.Listener, GenericPropertyListener {

		private EventType<?, ?, ?> _type;

		private boolean _generic;

		private Observable _sender;

		private Object _oldValue;

		private Object _newValue;

		private int _calls;

		private Bubble _buble = Bubble.BUBBLE;

		private boolean _remove;

		@Override
		public Bubble handleProperty1Change(Observable sender, String oldValue, String newValue) {
			_type = Observable.PROPERTY_1;
			_generic = false;
			_sender = sender;
			_oldValue = oldValue;
			_newValue = newValue;
			_calls++;

			if (_remove) {
				sender.removeListener(Observable.PROPERTY_1, this);
			}
			return _buble;
		}

		@Override
		public Bubble handleProperty2Change(Observable sender, Integer oldValue, Integer newValue) {
			_type = Observable.PROPERTY_2;
			_generic = false;
			_sender = sender;
			_oldValue = oldValue;
			_newValue = newValue;
			_calls++;

			if (_remove) {
				sender.removeListener(Observable.PROPERTY_2, this);
			}
			return _buble;
		}

		@Override
		public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
			_type = type;
			_generic = true;
			_sender = (Observable) sender;
			_oldValue = oldValue;
			_newValue = newValue;
			_calls++;

			if (_remove) {
				assertTrue(((PropertyObservable) sender).removeListener(PropertyObservable.GLOBAL_LISTENER_TYPE, this));
			}
			return _buble;
		}

		public void cancelBuble() {
			_buble = Bubble.CANCEL_BUBBLE;
		}

		public void requestRemove() {
			_remove = true;
		}

		public EventType<?, ?, ?> getLastType() {
			return _type;
		}

		public boolean generic() {
			return _generic;
		}

		public Observable getLastSender() {
			return _sender;
		}

		public Object getLastOldValue() {
			return _oldValue;
		}

		public Object getLastNewValue() {
			return _newValue;
		}

		public int getCalls() {
			return _calls;
		}
	}

	/**
	 * Testing listener registration.
	 */
	public void testRegistration() {
		O observable = createObservable();

		assertNotHasListeners(observable);

		CountingListener l1 = new CountingListener();
		assertTrue(observable.addListener(Observable.PROPERTY_1, l1));
		assertHasListeners(observable);
		assertFalse(observable.addListener(Observable.PROPERTY_1, l1));
		assertHasListeners(observable);

		CountingListener l2 = new CountingListener();
		assertTrue(observable.addListener(Observable.PROPERTY_1, l2));
		assertHasListeners(observable);

		assertTrue(observable.removeListener(Observable.PROPERTY_1, l1));
		assertHasListeners(observable);
		assertFalse(observable.removeListener(Observable.PROPERTY_1, l1));
		assertHasListeners(observable);

		assertTrue(observable.removeListener(Observable.PROPERTY_1, l2));
		assertNotHasListeners(observable);
	}

	/**
	 * Creates the {@link Observable} under test.
	 */
	protected abstract O createObservable();

	private void assertNotHasListeners(O observable) {
		assertFalse(hasListeners(observable, Observable.PROPERTY_1));
		assertFalse(hasListeners(observable, PropertyObservable.GLOBAL_LISTENER_TYPE));
		assertFalse(hasListeners(observable));
	}

	private void assertHasListeners(O observable) {
		assertTrue(hasListeners(observable));
		assertTrue(hasListeners(observable, PropertyObservable.GLOBAL_LISTENER_TYPE) ||
			hasListeners(observable, Observable.PROPERTY_1));
	}

	/**
	 * Inspect the given {@link Observable} whether it contains any listeners.
	 */
	protected abstract boolean hasListeners(O observable);

	/**
	 * Inspect the given {@link Observable} whether it contains listeners of the given type.
	 */
	protected abstract boolean hasListeners(O observable, EventType<?, ?, ?> type);

	/**
	 * Test event dispatch.
	 */
	public void testNotification() {
		Observable observable = createObservable();

		CountingListener l1 = new CountingListener();
		observable.addListener(Observable.PROPERTY_1, l1);

		observable.setProperty1("foo");
		assertEquals(1, l1.getCalls());

		// Add again (no effect).
		observable.addListener(Observable.PROPERTY_1, l1);

		observable.setProperty1("bar");
		assertEquals(2, l1.getCalls());

		CountingListener l2 = new CountingListener();
		observable.addListener(Observable.PROPERTY_1, l2);

		observable.setProperty1("foobar");
		assertEquals(3, l1.getCalls());
		assertEquals(1, l2.getCalls());

		observable.removeListener(Observable.PROPERTY_1, l2);

		observable.setProperty1("yetanother");
		assertEquals(4, l1.getCalls());
		assertEquals(1, l2.getCalls());
	}

	/**
	 * Test generic event dispatch.
	 */
	public void testGenericNotification() {
		O observable = createObservable();

		CountingListener l1 = new CountingListener();
		CountingListener l2 = new CountingListener();

		// Dummy listeners to have more than one for each type.
		CountingListener l3 = new CountingListener();
		CountingListener l4 = new CountingListener();

		observable.addListener(Observable.PROPERTY_1, l1);
		observable.addListener(Observable.PROPERTY_1, l3);
		observable.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, l2);
		observable.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, l4);

		assertHasListeners(observable);

		observable.setProperty1("foo");
		
		assertEquals(1, l1.getCalls());
		assertEquals(1, l2.getCalls());
		assertEquals(1, l3.getCalls());
		assertEquals(1, l4.getCalls());
		assertFalse(l1.generic());
		assertTrue(l2.generic());
		assertEquals(Observable.PROPERTY_1, l1.getLastType());
		assertEquals(Observable.PROPERTY_1, l2.getLastType());

		assertHasListeners(observable);

		l1.requestRemove();
		l2.requestRemove();
		
		observable.setProperty1("bar");
		
		assertEquals(2, l1.getCalls());
		assertEquals(2, l2.getCalls());
		assertEquals(2, l3.getCalls());
		assertEquals(2, l4.getCalls());
		
		observable.setProperty1("foobar");

		// Only the listeners that did not request removal are still notified.
		assertEquals(2, l1.getCalls());
		assertEquals(2, l2.getCalls());
		assertEquals(3, l3.getCalls());
		assertEquals(3, l4.getCalls());

		assertTrue(observable.removeListener(Observable.PROPERTY_1, l3));
		assertTrue(observable.removeListener(PropertyObservable.GLOBAL_LISTENER_TYPE, l4));

		assertNotHasListeners(observable);
	}

	/**
	 * Test event dispatch when observing multiple properties with separate listeners.
	 */
	public void testMultipleProperties() {
		O observable = createObservable();

		CountingListener l1 = new CountingListener();
		CountingListener l2 = new CountingListener();
		CountingListener l3 = new CountingListener();

		observable.addListener(Observable.PROPERTY_1, l1);
		observable.addListener(Observable.PROPERTY_2, l2);
		observable.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, l3);

		observable.setProperty1("foo");

		assertEquals(1, l1.getCalls());
		assertEquals(0, l2.getCalls());
		assertEquals(1, l3.getCalls());
		assertEquals("foo", l1.getLastNewValue());
		assertEquals("foo", l3.getLastNewValue());

		observable.setProperty2(42);

		assertEquals(1, l1.getCalls());
		assertEquals(1, l2.getCalls());
		assertEquals(2, l3.getCalls());
		assertEquals(42, l2.getLastNewValue());
		assertEquals(42, l3.getLastNewValue());
	}

}
