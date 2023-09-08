/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;

import com.top_logic.basic.col.ObservableMapProxy;

/**
 * Test case for {@link ObservableMapProxy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestObservableMapProxy extends AbstractTestMap {

	private List<ObservableMapProxyTester> _testInstances = new ArrayList<>();

	public TestObservableMapProxy(String testName) {
		super(testName);
	}

	@Override
	public void tearDown() throws Exception {
		for (ObservableMapProxyTester tester : _testInstances) {
			tester.assertObserverCalled();
		}
		super.tearDown();
	}

	@Override
	public Map makeEmptyMap() {
		ObservableMapProxyTester result = new ObservableMapProxyTester();
		_testInstances.add(result);
		return result;
	}

	final class ObservableMapProxyTester extends ObservableMapProxy<Object, Object> {
		private Map<Object, Object> _impl = new HashedMap<>();
	
		private Map<Object, Object> _check = new HashedMap<>();
	
		@Override
		protected Map<Object, Object> impl() {
			return _impl;
		}
	
		@Override
		protected void afterPut(Object key, Object value) {
			super.afterPut(key, value);
			_check.put(key, value);
		}
	
		@Override
		protected void afterRemove(Object key, Object oldValue) {
			super.afterRemove(key, oldValue);
			_check.remove(key);
		}
	
		public void assertObserverCalled() {
			assertTrue(_check.equals(_impl));
		}
	}

}
