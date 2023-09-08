/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * Abstract test class for {@link TypedAnnotatable}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractTypedAnnotationTest extends TestCase {

	/**
	 * Creates a {@link TypedAnnotatable} for test.
	 */
	protected abstract TypedAnnotatable newAnnotatable();

	public void testProperty() {
		Property<String> fooProperty = TypedAnnotatable.property(String.class, "foo");
		assertEquals("foo", fooProperty.getName());
		assertEquals("foo : String", fooProperty.toString());
	}

	public void testAnnotate() {
		TypedAnnotatable container = newAnnotatable();
		Property<String> fooProperty = TypedAnnotatable.property(String.class, "foo");

		assertFalse(container.isSet(fooProperty));
		assertNull(container.get(fooProperty));

		assertNull(container.set(fooProperty, "value"));
		assertTrue(container.isSet(fooProperty));
		assertEquals("value", container.get(fooProperty));

		assertEquals("value", container.reset(fooProperty));
		assertFalse(container.isSet(fooProperty));
		assertNull(container.get(fooProperty));
	}

	public void testTypeCheck() {
		TypedAnnotatable container = newAnnotatable();
		Property<String> fooProperty = TypedAnnotatable.property(String.class, "foo");

		try {
			// Simulate wrong generic cast.
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Property<Object> genericProperty = (Property) fooProperty;

			container.set(genericProperty, 13);
			fail("Must fail due to failing type check.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public void testAnnotateWithDefault() {
		TypedAnnotatable container = newAnnotatable();
		Property<String> fooProperty = TypedAnnotatable.property(String.class, "foo", "default");

		assertFalse(container.isSet(fooProperty));
		assertEquals("default", container.get(fooProperty));

		assertEquals("default", container.set(fooProperty, "value"));
		assertTrue(container.isSet(fooProperty));
		assertEquals("value", container.get(fooProperty));

		assertEquals("value", container.reset(fooProperty));
		assertFalse(container.isSet(fooProperty));
		assertEquals("default", container.get(fooProperty));

		assertEquals("default", container.set(fooProperty, null));
		assertTrue(container.isSet(fooProperty));
		assertNull(container.get(fooProperty));
	}

	public void testDynamicDefault() {
		TypedAnnotatable container = newAnnotatable();
		Property<String> defaultProp = TypedAnnotatable.property(String.class, "foo");
		Property<String> fooProp = TypedAnnotatable.propertyDynamic(String.class, "foo", x -> x.get(defaultProp));

		container.set(defaultProp, "Hello");
		assertEquals("Hello", container.get(defaultProp));
		assertEquals("Hello", container.get(fooProp));

		container.set(fooProp, "World");
		assertEquals("Hello", container.get(defaultProp));
		assertEquals("World", container.get(fooProp));
	}

	public void testListProperty() {
		TypedAnnotatable container = newAnnotatable();
		Property<List<String>> property = TypedAnnotatable.propertyList("foo");

		assertEquals(list(), container.get(property));

		container.mkList(property).add("Hello");
		container.mkList(property).add("World");

		assertEquals(list("Hello", "World"), container.get(property));
	}

	public void testSetProperty() {
		TypedAnnotatable container = newAnnotatable();
		Property<Set<String>> property = TypedAnnotatable.propertySet("foo");

		assertEquals(set(), container.get(property));

		container.mkSet(property).add("Hello");
		container.mkSet(property).add("World");

		assertEquals(set("Hello", "World"), container.get(property));
	}

	public void testMapProperty() {
		TypedAnnotatable container = newAnnotatable();
		Property<Map<String, String>> property = TypedAnnotatable.propertyMap("foo");

		assertEquals(Collections.emptyMap(), container.get(property));

		container.mkMap(property).put("Hello", "World");
		container.mkMap(property).put("foo", "bar");

		assertEquals("World", container.get(property).get("Hello"));
		assertEquals("bar", container.get(property).get("foo"));
	}

	public void testSetIfAbsent() {
		Object defaultPropValue = new NamedConstant("default property value");

		TypedAnnotatable container = newAnnotatable();

		NamedConstant val1 = new NamedConstant("val1");
		NamedConstant val2 = new NamedConstant("val2");
		Property<Object> property = TypedAnnotatable.property(Object.class, "p", defaultPropValue);

		assertEquals(defaultPropValue, container.get(property));

		container.set(property, val1);
		assertEquals(val1, container.get(property));
		Object res = container.setIfAbsent(property, val2);
		assertEquals(val1, res);
		assertEquals(val1, container.get(property));

		container.reset(property);
		assertEquals(defaultPropValue, container.get(property));
		res = container.setIfAbsent(property, val1);
		assertEquals(null, res);
		assertEquals(val1, container.get(property));

		container.reset(property);
		container.set(property, null);
		res = container.setIfAbsent(property, val2);
		assertEquals(null, res);
		assertEquals(null, container.get(property));
	}

}

