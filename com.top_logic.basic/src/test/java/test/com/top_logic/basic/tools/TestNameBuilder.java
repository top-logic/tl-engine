/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.tools.NameBuilder;

/**
 * A {@link TestCase} for the {@link NameBuilder}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestNameBuilder extends TestCase {

	/**
	 * Creates a new {@link TestNameBuilder}.
	 */
	public TestNameBuilder() {
		// Just declare this constructor
	}

	/**
	 * Creates a new {@link TestNameBuilder} that executes only the test with the given name.
	 */
	public TestNameBuilder(String name) {
		super(name);
	}

	public void testNull() {
		try {
			new NameBuilder(null);
		} catch (NullPointerException exception) {
			return;
		}
		fail("Expected NullPointerException but none was thrown.");
	}

	public void testObjectEmpty() {
		assertEquals("TestNameBuilder()", new NameBuilder(this).buildName());
	}

	public void testObjectSingleProperty() {
		String actual = new NameBuilder(this).add("exampleName", "exampleValue").buildName();
		assertEquals("TestNameBuilder(exampleName = exampleValue)", actual);
	}

	public void testObjectTwoProperties() {
		String actual = new NameBuilder(this)
			.add("firstExampleName", "firstExampleValue")
			.add("secondExampleName", "secondExampleValue")
			.buildName();
		String expected = "TestNameBuilder("
			+ "firstExampleName = firstExampleValue, "
			+ "secondExampleName = secondExampleValue)";
		assertEquals(expected, actual);
	}

	public void testObjectThreeProperties() {
		String actual = new NameBuilder(this)
			.add("firstExampleName", "firstExampleValue")
			.add("secondExampleName", "secondExampleValue")
			.add("thirdExampleName", "thirdExampleValue")
			.buildName();
		String expected = "TestNameBuilder("
			+ "firstExampleName = firstExampleValue, "
			+ "secondExampleName = secondExampleValue, "
			+ "thirdExampleName = thirdExampleValue)";
		assertEquals(expected, actual);
	}

	public void testListPropertiesEmpty() {
		ArrayList<String> collection = new ArrayList<>(Collections.<String> emptyList());
		String actual = new NameBuilder(this).add("exampleName", collection).buildName();
		String expected = "TestNameBuilder(exampleName = ArrayList())";
		assertEquals(expected, actual);
	}

	public void testListPropertySingleEntry() {
		ArrayList<String> collection = new ArrayList<>(Collections.singletonList("exampleValue"));
		String actual = new NameBuilder(this).add("exampleName", collection).buildName();
		String expected = "TestNameBuilder(exampleName = ArrayList(exampleValue))";
		assertEquals(expected, actual);
	}

	public void testListPropertyTwoEntries() {
		ArrayList<String> collection = new ArrayList<>(Arrays.asList(
			"firstExampleValue",
			"secondExampleValue"));
		String actual = new NameBuilder(this).add("exampleName", collection).buildName();
		String expected = "TestNameBuilder(exampleName = "
			+ "ArrayList(firstExampleValue, secondExampleValue))";
		assertEquals(expected, actual);
	}

	public void testListPropertyThreeEntries() {
		ArrayList<String> collection = new ArrayList<>(Arrays.asList(
			"firstExampleValue",
			"secondExampleValue",
			"thirdExampleValue"));
		String actual = new NameBuilder(this).add("exampleName", collection).buildName();
		String expected = "TestNameBuilder(exampleName = "
			+ "ArrayList(firstExampleValue, secondExampleValue, thirdExampleValue))";
		assertEquals(expected, actual);
	}

	public void testMapPropertyEmpty() {
		Map<String, String> map =
			new LinkedHashMap<>(Collections.<String, String> emptyMap());
		String actual = new NameBuilder(this).add("exampleName", map).buildName();
		String expected = "TestNameBuilder(exampleName = LinkedHashMap())";
		assertEquals(expected, actual);
	}

	public void testMapPropertySingleEntry() {
		Map<String, String> map =
			new LinkedHashMap<>(Collections.singletonMap("firstExampleKey", "firstExampleValue"));
		String actual = new NameBuilder(this).add("exampleName", map).buildName();
		String expected = "TestNameBuilder(exampleName = LinkedHashMap(firstExampleKey -> firstExampleValue))";
		assertEquals(expected, actual);
	}

	public void testMapPropertyTwoEntries() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("firstExampleKey", "firstExampleValue");
		map.put("secondExampleKey", "secondExampleValue");
		String actual = new NameBuilder(this).add("exampleName", map).buildName();
		String expected = "TestNameBuilder(exampleName = LinkedHashMap("
			+ "firstExampleKey -> firstExampleValue, "
			+ "secondExampleKey -> secondExampleValue))";
		assertEquals(expected, actual);
	}

	public void testMapPropertyThreeEntries() {
		Map<String, String> map = new LinkedHashMap<>();
		map.put("firstExampleKey", "firstExampleValue");
		map.put("secondExampleKey", "secondExampleValue");
		map.put("thirdExampleKey", "thirdExampleValue");
		String actual = new NameBuilder(this).add("exampleName", map).buildName();
		String expected = "TestNameBuilder(exampleName = LinkedHashMap("
			+ "firstExampleKey -> firstExampleValue, "
			+ "secondExampleKey -> secondExampleValue, "
			+ "thirdExampleKey -> thirdExampleValue))";
		assertEquals(expected, actual);
	}

	public void testClassProperty() {
		String actual = new NameBuilder(this).add("exampleName", Void.class).buildName();
		assertEquals("TestNameBuilder(exampleName = Class(Void))", actual);
	}

	public void testNullAsStringProperty() {
		String actual = new NameBuilder(this).add("exampleName", (String) null).buildName();
		assertEquals("TestNameBuilder(exampleName = null)", actual);
	}

	public void testNullAsCollectionProperty() {
		String actual = new NameBuilder(this).add("exampleName", (Collection<String>) null).buildName();
		assertEquals("TestNameBuilder(exampleName = null)", actual);
	}

	public void testNullAsMapProperty() {
		String actual = new NameBuilder(this).add("exampleName", (Map<String, String>) null).buildName();
		assertEquals("TestNameBuilder(exampleName = null)", actual);
	}

	public void testNullAsClassProperty() {
		String actual = new NameBuilder(this).add("exampleName", (Class<?>) null).buildName();
		assertEquals("TestNameBuilder(exampleName = null)", actual);
	}

	public void testNullAsEnumProperty() {
		String actual = new NameBuilder(this).add("exampleName", (Enum<?>) null).buildName();
		assertEquals("TestNameBuilder(exampleName = null)", actual);
	}

}
