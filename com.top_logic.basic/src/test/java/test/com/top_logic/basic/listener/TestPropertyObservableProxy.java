/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.listener;

import test.com.top_logic.basic.listener.TestPropertyObservableBase.ObservableImpl;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.listener.PropertyObservableProxy;

/**
 * Test for {@link PropertyObservableProxy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestPropertyObservableProxy extends TestPropertyObservable<TestPropertyObservableProxy.ObservableProxy> {

	/**
	 * Default implementation of the {@link TestPropertyObservable.Observable} interface.
	 */
	static class ObservableProxy extends PropertyObservableProxy implements TestPropertyObservable.Observable {

		private ObservableImpl _impl = new ObservableImpl();

		@Override
		public String getProperty1() {
			return _impl.getProperty1();
		}

		@Override
		public void setProperty1(String newValue) {
			_impl.setProperty1(newValue);
		}

		@Override
		public int getProperty2() {
			return _impl.getProperty2();
		}

		@Override
		public void setProperty2(int newValue) {
			_impl.setProperty2(newValue);
		}

		@Override
		protected PropertyObservable impl() {
			return _impl;
		}

		public boolean hasListenersAccess() {
			return _impl.hasListenersAccess();
		}

		public boolean hasListenersAccess(EventType<?, ?, ?> type) {
			return _impl.hasListenersAccess(type);
		}
	}

	@Override
	protected ObservableProxy createObservable() {
		return new ObservableProxy();
	}

	@Override
	protected boolean hasListeners(ObservableProxy observable) {
		return observable.hasListenersAccess();
	}

	@Override
	protected boolean hasListeners(ObservableProxy observable, EventType<?, ?, ?> type) {
		return observable.hasListenersAccess(type);
	}

}
