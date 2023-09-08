/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.listener;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.listener.PropertyObservableBase;

/**
 * Test for {@link PropertyObservableBase}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPropertyObservableBase extends TestPropertyObservable<TestPropertyObservableBase.ObservableImpl> {

	/**
	 * Default implementation of the {@link TestPropertyObservable.Observable} interface.
	 */
	static class ObservableImpl extends PropertyObservableBase implements TestPropertyObservable.Observable {

		private String _property1;

		private final ObservableImpl _outer;

		private int _property2;

		private Set<EventType<?, ?, ?>> _observedTypes = new HashSet<>();

		public ObservableImpl() {
			this(null);
		}

		public ObservableImpl(ObservableImpl outer) {
			_outer = outer;
		}

		@Override
		public String getProperty1() {
			return _property1;
		}

		@Override
		public void setProperty1(String newValue) {
			String oldValue = _property1;
			_property1 = newValue;

			notifyChange(this, PROPERTY_1, newValue, oldValue);
		}

		@Override
		public int getProperty2() {
			return _property2;
		}

		@Override
		public void setProperty2(int newValue) {
			int oldValue = _property2;
			_property2 = newValue;

			notifyChange(this, PROPERTY_2, newValue, oldValue);
		}

		/**
		 * Potentially bubbling event notification.
		 */
		private <T> void notifyChange(ObservableImpl changed, EventType<Listener, Observable, T> property, T newValue,
				T oldValue) {
			Bubble bubble = notifyListeners(property, changed, oldValue, newValue);
			if (bubble == Bubble.BUBBLE && _outer != null) {
				_outer.notifyChange(changed, property, newValue, oldValue);
			}
		}

		protected final boolean hasListenersAccess() {
			return super.hasListeners();
		}

		protected final boolean hasListenersAccess(EventType<?, ?, ?> eventType) {
			return super.hasListeners(eventType);
		}

		@Override
		protected void firstListenerAdded(EventType<?, ?, ?> type) {
			super.firstListenerAdded(type);

			boolean success = _observedTypes.add(type);
			assertTrue(success);
		}

		@Override
		protected void lastListenerRemoved(EventType<?, ?, ?> type) {
			super.lastListenerRemoved(type);

			boolean success = _observedTypes.remove(type);
			assertTrue(success);
		}

		public Set<EventType<?, ?, ?>> getObservedTypes() {
			return _observedTypes;
		}

	}

	@Override
	protected ObservableImpl createObservable() {
		return new ObservableImpl();
	}

	@Override
	protected boolean hasListeners(ObservableImpl observable) {
		return observable.hasListenersAccess();
	}

	@Override
	protected boolean hasListeners(ObservableImpl observable, EventType<?, ?, ?> type) {
		return observable.hasListenersAccess(type);
	}

	/**
	 * Test for event bubbling up a hierarchy.
	 */
	public void testBubbling() {
		ObservableImpl parent = new ObservableImpl();
		ObservableImpl observable = new ObservableImpl(parent);

		CountingListener l1 = new CountingListener();
		CountingListener l2 = new CountingListener();

		observable.addListener(Observable.PROPERTY_1, l1);
		parent.addListener(Observable.PROPERTY_1, l2);

		observable.setProperty1("foo");

		assertEquals(1, l1.getCalls());
		assertEquals(observable, l1.getLastSender());
		assertEquals(1, l2.getCalls());
		assertEquals(observable, l2.getLastSender());

		l1.cancelBuble();

		observable.setProperty1("bar");

		assertEquals(2, l1.getCalls());
		assertEquals(observable, l1.getLastSender());
		assertEquals(1, l2.getCalls());
	}

	/**
	 * Test for event bubbling up a hierarchy.
	 */
	public void testObservingRegistration() {
		ObservableImpl observable = new ObservableImpl();

		CountingListener l1 = new CountingListener();
		CountingListener l2 = new CountingListener();

		assertTrue(observable.getObservedTypes().isEmpty());

		observable.addListener(Observable.PROPERTY_1, l1);
		assertEquals(set(Observable.PROPERTY_1), observable.getObservedTypes());

		observable.addListener(Observable.PROPERTY_1, l2);
		assertEquals(set(Observable.PROPERTY_1), observable.getObservedTypes());

		observable.removeListener(Observable.PROPERTY_1, l1);
		assertEquals(set(Observable.PROPERTY_1), observable.getObservedTypes());

		observable.removeListener(Observable.PROPERTY_1, l2);
		assertEquals(set(), observable.getObservedTypes());

		observable.addListener(Observable.PROPERTY_1, l1);
		observable.addListener(Observable.PROPERTY_2, l1);
		observable.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, l1);
		assertEquals(set(Observable.PROPERTY_1, Observable.PROPERTY_2, PropertyObservable.GLOBAL_LISTENER_TYPE),
			observable.getObservedTypes());

		observable.removeListener(Observable.PROPERTY_1, l1);
		observable.removeListener(Observable.PROPERTY_2, l1);
		assertEquals(set(PropertyObservable.GLOBAL_LISTENER_TYPE), observable.getObservedTypes());
	}

}
