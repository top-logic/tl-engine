/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.knowledge.wrap.MapValueProvider;

/**
 * Test case for {@link MapValueProvider}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestMapValueProvider extends TestCase {

	public void testSetValue() {
		MapValueProvider obj = new MapValueProvider();
		assertNull(obj.getValue("foo"));

		obj.setValue("foo", "bar");
		assertEquals("bar", obj.getValue("foo"));
	}

	public void testInitialValues() {
		Map<String, Object> values = new HashMap<>();
		values.put("foo", "initial");
		MapValueProvider obj = new MapValueProvider(values);
		assertEquals("initial", obj.getValue("foo"));

		obj.setValue("foo", "bar");
		assertEquals("bar", obj.getValue("foo"));
		assertEquals("bar", values.get("foo"));
	}

	public void testDefaultValues() {
		MapValueProvider obj = new MapValueProvider() {
			@Override
			protected Object getDefaultValue(String aKey) {
				return "initial";
			}
		};

		assertEquals("initial", obj.getValue("foo"));

		obj.setValue("foo", "bar");
		assertEquals("bar", obj.getValue("foo"));

		obj.setValue("foo", null);
		assertNull(obj.getValue("foo"));
	}
}
