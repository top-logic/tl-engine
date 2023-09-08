/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.TestCase;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;

/**
 * The class {@link TestMappings} tests the class {@link Mappings}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestMappings extends TestCase {

	public void testIdentityMapping() {
		Object input = new Object();
		assertSame(input, Mappings.identity().map(input));
		input = Long.valueOf(2L);
		assertSame(input, Mappings.identity().map(input));
		assertSame(null, Mappings.identity().map(null));
		assertSame(Mappings.identity(), Mappings.identity().map(Mappings.identity()));
	}

	public void testEntries() {
		Map<Object, Object> map = new HashMap<>();
		map.put("1", null);
		map.put(null, null);
		map.put(new Object(), new Object());

		for (Entry<Object, Object> entry : map.entrySet()) {
			assertSame(entry.getKey(), Mappings.entryToKey().map(entry));
			assertSame(entry.getValue(), Mappings.entryToValue().map(entry));
		}
	}

	public void testMap() {
		Mapping<String, Integer> mapping = new Mapping<>() {

			@Override
			public Integer map(String input) {
				return Integer.valueOf(input);
			}
		};
		List<String> input = list("0", "2", "1");
		assertEquals(list(0, 2, 1), Mappings.map(mapping, input));
		assertEquals(Collections.<Integer>emptyList(), Mappings.map(mapping, Collections.<String>emptyList()));

		assertNotSame("Expected new list", Collections.<Integer>emptyList(), Mappings.map(mapping, Collections.<String>emptyList()));
	}
	
	public void testMapBasedMapping1() {
		Map<Object, Object> map = new HashMap<>();
		map.put("1", null);
		map.put(null, null);
		map.put(new Object(), new Object());
		final Object defaultValue = new Object();
		final Mapping<Object, Object> mapping = Mappings.createMapBasedMapping(map, defaultValue);
		checkCorrectMapping(map, mapping);
		
		assertSame(defaultValue, mapping.map("noKey"));
	}

	public void testMapBasedMapping2() {
		Map<Object, Object> map = new HashMap<>();
		map.put("1", null);
		map.put(null, null);
		map.put(new Object(), new Object());
		Mapping<Object, Object> defaultMapping = new Mapping<>() {
			
			@Override
			public Object map(Object input) {
				if (input.equals("2")) {
					return 2;
				}
				if (input.equals("3")) {
					return 3;
				}
				return input;
			}
		};
		final Mapping<Object, Object> mapping = Mappings.createMapBasedMapping(map, defaultMapping);
		checkCorrectMapping(map, mapping);
		
		assertEquals("noKey", mapping.map("noKey"));
		assertEquals(2, mapping.map(2));
		assertEquals(2, mapping.map("2"));
		assertEquals(3, mapping.map("3"));
	}
	
	private <S,D> void checkCorrectMapping(Map<S, ? extends D> map, Mapping<? super S, ? extends D> mapping) {
		for (Entry<S, ? extends D> entry : map.entrySet()) {
			assertEquals(entry.getValue(), mapping.map(entry.getKey()));
		}
	}

}
