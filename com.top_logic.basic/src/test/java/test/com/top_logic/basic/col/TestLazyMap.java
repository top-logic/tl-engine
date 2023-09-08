/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.col.LazyMap;

/**
 * The class {@link TestLazyMap} tests {@link LazyMap}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLazyMap extends TestCase {
	
	public void testLazyMapModifiable() {
		class TestMap extends LazyMap<String, Integer> {
			int initialized = 0;
			Map<String, Integer> impl = new HashMap<>();
			{
				impl.put("k1", 1);
			}

			@Override
			protected Map<String, Integer> initInstance() {
				initialized++;
				return impl;
			}
		};
		
		TestMap m = new TestMap();
		assertEquals("Lazy map is not initailized lazily", 0, m.initialized);
		assertEquals(m.impl.get("k1"), m.get("k1"));
		m.put("k2", 2);
		assertEquals("Setting value to lazy map does not reflect to implementation", m.impl.get("k2"), m.get("k2"));
		m.impl.put("k2", 3);
		assertEquals("Setting value to implementation does not reflect to lazy map", m.impl.get("k2"), m.get("k2"));
		assertEquals("Lazy map is not initailized once", 1, m.initialized);
	}
	
	public void testUnmodifiable() {
		final Map<String,Integer> implementation = new HashMap<>();
		Map<String,Integer> unmodifiableLazyMap = new LazyMap<>() {

			@Override
			protected Map<String, Integer> initInstance() {
				return Collections.unmodifiableMap(implementation);
			}
		};
		
		assertTrue(unmodifiableLazyMap.isEmpty());
		implementation.put("k", 1);
		assertEquals(implementation, unmodifiableLazyMap);
		try {
			unmodifiableLazyMap.put("k", 2);
			fail("implementation is unmodifiable, but lazy map is.");
		} catch (UnsupportedOperationException ex) {
			// lazy map is unmodifiable
		}
		
	}

}

