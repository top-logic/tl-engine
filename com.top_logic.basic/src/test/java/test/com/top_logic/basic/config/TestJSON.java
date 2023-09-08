/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.json.config.JSONBoolean;
import com.top_logic.basic.json.config.JSONConfigValueAnalyzer;
import com.top_logic.basic.json.config.JSONFloat;
import com.top_logic.basic.json.config.JSONInteger;
import com.top_logic.basic.json.config.JSONList;
import com.top_logic.basic.json.config.JSONNull;
import com.top_logic.basic.json.config.JSONObject;
import com.top_logic.basic.json.config.JSONProperty;
import com.top_logic.basic.json.config.JSONString;
import com.top_logic.basic.json.config.JSONValue;

/**
 * Test case for {@link JSON}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestJSON extends BasicTestCase {
	
	public void testInt() {
		assertEquals("42", JSON.toString(Integer.valueOf(42)));
	}

	public void testIntParse() throws ParseException {
		assertParse(Integer.valueOf(42), "42");
	}
	
	public void testNull() {
		assertEquals("null", JSON.toString(null));
	}
	
	public void testNullParse() throws ParseException {
		assertNull(JSON.fromString("null"));
	}
	
	public void testTrue() {
		assertEquals("true", JSON.toString(Boolean.TRUE));
	}
	
	public void testTrueParse() throws ParseException {
		assertParse(Boolean.TRUE, "true");
	}
	
	public void testFalse() {
		assertEquals("false", JSON.toString(Boolean.FALSE));
	}
	
	public void testFalseParse() throws ParseException {
		assertParse(Boolean.FALSE, "false");
		assertParse(Boolean.FALSE, " false ");
		assertParse(Boolean.FALSE, "\t false \t");
	}
	
	public void testDouble() {
		assertEquals("42.13", JSON.toString(Float.valueOf(42.13f)));
	}
	
	public void testDoubleParse() throws ParseException {
		assertParse(Double.valueOf(42.13), "42.13");
	}

	public void testQuoteControlCharacters() {
		assertEquals("\"\\u0000\"", JSON.toString("\u0000"));
		assertEquals("\"\\u001F\"", JSON.toString("\u001F"));
	}

	public void testQuoteTab() {
		assertEquals("\"\\t\"", JSON.toString("\t"));
	}

	public void testQuoteLineFeed() {
		assertEquals("\"\\n\"", JSON.toString("\n"));
	}

	public void testQuoteCarriageReturn() {
		assertEquals("\"\\r\"", JSON.toString("\r"));
	}

	public void testQuoteFormFeed() {
		assertEquals("\"\\f\"", JSON.toString("\f"));
	}

	public void testQuoteBackSpace() {
		assertEquals("\"\\b\"", JSON.toString("\b"));
	}

	public void testQuoteBackslash() {
		assertEquals("\"\\\\\"", JSON.toString("\\"));
	}

	public void testParseUnicode() throws ParseException {
		assertParse("\\", "\"\\u005C\"");
		assertParse("\uD834\uDD1E", "\"\\uD834\\udd1e\"");
	}

	public void testParseQuotedQuote() throws ParseException {
		assertParse("\"", "\"\\\"\"");
	}

	public void testParseQuotedBackslash() throws ParseException {
		assertParse("\\", "\"\\\\\"");
	}

	public void testParseQuotedSlash() throws ParseException {
		assertParse("/", "\"\\/\"");
	}

	public void testParseQuotedBackspace() throws ParseException {
		assertParse("\b", "\"\\b\"");
	}

	public void testParseQuotedFormFeed() throws ParseException {
		assertParse("\f", "\"\\f\"");
	}

	public void testParseQuotedLineFeed() throws ParseException {
		assertParse("\n", "\"\\n\"");
	}

	public void testParseQuotedCariageReturn() throws ParseException {
		assertParse("\r", "\"\\r\"");
	}

	public void testParseQuotedTab() throws ParseException {
		assertParse("\t", "\"\\t\"");
	}

	public void testStringReadBack() throws ParseException {
		assertReadBack("'");
		assertReadBack("\"");
		assertReadBack("\\");
		assertReadBack("\\\"");
		assertReadBack("\\\'");
		assertReadBack("\n\t\r");
	}

	private void assertReadBack(Object obj) throws ParseException {
		assertParse(obj, JSON.toString(obj));
	}

	public void testKeyEscapeBack() throws ParseException {
		assertReadBackKey("'");
		assertReadBackKey("\"");
		assertReadBackKey("\\");
		assertReadBackKey("\\\"");
		assertReadBackKey("\\\'");
		assertReadBackKey("\n\t\r");
	}

	private void assertReadBackKey(String key) throws ParseException {
		assertReadBack(obj().attr(key, 42));
	}

	private static TestObject obj() {
		return new TestObject();
	}

	static class TestObject extends HashMap<String, Object> {
		public TestObject attr(String key, Object value) {
			put(key, value);
			return this;
		}
	}

	public void testNumberReadBack() throws ParseException {
		assertParse(Integer.valueOf(42), JSON.toString(Integer.valueOf(42)));
		assertParse(Double.valueOf(42), JSON.toString(Double.valueOf(42)));
		assertParse(Double.valueOf(42E99), JSON.toString(Double.valueOf(42E99)));
	}
	
	public void testString() {
		assertEquals("\"Hello world!\"", JSON.toString("Hello world!"));
	}
	
	public void testStringParse() throws ParseException {
		assertParse("Hello world!", "\"Hello world!\"");
	}
	
	public void testStringQuote() {
		assertEquals("\"Hello \\\"world\\\"!\"", JSON.toString("Hello \"world\"!"));
	}

	public void testStringQuoteParse() throws ParseException {
		assertParse("Hello \"world\"!", "\"Hello \\\"world\\\"!\"");
	}
	
	public void testList() {
		assertEquals("[]", JSON.toString(Collections.EMPTY_LIST));
		assertEquals("[1]", JSON.toString(Arrays.asList(new Object[] {Integer.valueOf(1)})));
		assertEquals("[1,2,3]", JSON.toString(Arrays.asList(new Object[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)})));
	}
	
	public void testListContents() {
		assertEquals("", JSON.toStringListContents(Collections.EMPTY_LIST));
		assertEquals("1", JSON.toStringListContents(Arrays.asList(new Object[] {Integer.valueOf(1)})));
		assertEquals("1,2,3", JSON.toStringListContents(Arrays.asList(new Object[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)})));
	}
	
	public void testListParse() throws ParseException {
		assertParse(Collections.EMPTY_LIST, "[]");
		assertParse(Arrays.asList(new Object[] {Integer.valueOf(1)}), "[1]");
		assertParse(Arrays.asList(new Object[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}), "[1,2,3]");
	}
	
	public void testListContentsParse() throws ParseException {
		assertParseListContents(Collections.EMPTY_LIST, "");
		assertParseListContents(Collections.EMPTY_LIST, "   \t");
		assertParseListContents(Arrays.asList(new Object[] {Integer.valueOf(1)}), "1");
		assertParseListContents(Arrays.asList(new Object[] {Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}), " \t1,2, \t3\t ");
	}
	
	public void testMap() {
		assertEquals("{}", JSON.toString(Collections.EMPTY_MAP));
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("Hello", "World!");
		assertEquals("{\"Hello\":\"World!\"}", JSON.toString(map));
		map.put("value", Integer.valueOf(42));
		assertEquals("{\"Hello\":\"World!\",\"value\":42}", JSON.toString(map));
		map.put("key-xxx", Integer.valueOf(13));
		map.put("key\"xxx", Integer.valueOf(7));
		assertEquals("{\"Hello\":\"World!\",\"value\":42,\"key-xxx\":13,\"key\\\"xxx\":7}", JSON.toString(map));
	}
	
	public void testMapContents() {
		assertEquals("", JSON.toStringMapContents(Collections.emptyMap()));
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("Hello", "World!");
		assertEquals("\"Hello\":\"World!\"", JSON.toStringMapContents(map));
		map.put("value", Integer.valueOf(42));
		assertEquals("\"Hello\":\"World!\",\"value\":42", JSON.toStringMapContents(map));
		map.put("key-xxx", Integer.valueOf(13));
		map.put("key\"xxx", Integer.valueOf(7));
		assertEquals("\"Hello\":\"World!\",\"value\":42,\"key-xxx\":13,\"key\\\"xxx\":7", JSON.toStringMapContents(map));
	}
	
	public void testDate() throws ParseException {
		doTestDate("2009-08-10T13:23:07.123Z", "2009-08-10 13:23:07.123 GMT");
	}

	private void doTestDate(String normalizedXmlDateTime, String dateSpec) throws ParseException {
		Date testDate = TestXmlDateTimeFormat.date(dateSpec);
		String jsonDate = "date(" + normalizedXmlDateTime + ")";
		assertEquals(jsonDate, JSON.toString(testDate));
		
		Date parsedDate = (Date) JSON.fromString(jsonDate);
		if (parsedDate.getTime() != testDate.getTime()) {
			fail("Dates does not match expected: '" + testDate + "', parsed: '" + parsedDate + "'.");
		}
	}
	
	public void testMapParse() throws ParseException {
		assertParse(Collections.emptyMap(), "{}");
		HashMap<String, Object> map = new HashMap<>();
		map.put("Hello", "World!");
		assertParse(map, "{Hello:\"World!\"}");
		map.put("value", Integer.valueOf(42));
		assertParse(map, "{Hello:\"World!\",value:42}");
		map.put("key", Integer.valueOf(13));
		map.put("xxx", Integer.valueOf(7));
		assertParse(map, "{Hello:\"World!\",value:42,'key':13,\"xxx\":7}");
	}
	
	public void testMapContentsParse() throws ParseException {
		assertParseMapContents(Collections.emptyMap(), "");
		assertParseMapContents(Collections.emptyMap(), "  \t\n");
		HashMap<String, Object> map = new HashMap<>();
		map.put("Hello", "World!");
		assertParseMapContents(map, "  \tHello: \t\"World!\"   ");
		map.put("value", Integer.valueOf(42));
		assertParseMapContents(map, "Hello:\"World!\",value:42");
		map.put("key", Integer.valueOf(13));
		map.put("xxx", Integer.valueOf(7));
		assertParseMapContents(map, "Hello:\"World!\" \t,  \t\n  \tvalue:42,'key':13,\"xxx\":7   ");
	}
	
	public void testParseFailureMissing1() {
		try {
			JSON.fromString("");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureMissing2() {
		try {
			JSON.fromString(" \t");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureAdditional1() {
		try {
			JSON.fromString("falsefalse");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureAdditional2() {
		try {
			JSON.fromString("false false");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureAdditional3() {
		try {
			JSON.fromString("false \tfalse");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureMap1() {
		try {
			JSON.fromString("{hello: world}");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureMap2() {
		try {
			JSON.fromString("{hello!: 'world'}");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureMap3() {
		try {
			JSON.fromString("{hello");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureMap4() {
		try {
			JSON.fromString("{");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureMap5() {
		try {
			JSON.fromString("{hello: 'world',}");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureList1() {
		try {
			JSON.fromString("['hello' 'world']");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureList2() {
		try {
			JSON.fromString("['hello', 'world',]");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureList3() {
		try {
			JSON.fromString("['hello', 'world',");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureList4() {
		try {
			JSON.fromString("['hello', 'world'");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureList5() {
		try {
			JSON.fromString("[");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureNumber1() {
		try {
			JSON.fromString("1.2.3");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}
	
	public void testParseFailureNumber2() {
		try {
			JSON.fromString("1234567890123456789012345678901234567890");
			fail("Invalid format, parsing must fail.");
		} catch (ParseException ex) {
			// Expected
			Logger.debug("Expected failure", ex, TestJSON.class);
		}
	}

    public void testComplexMapContents() throws ParseException { 
	    LinkedHashMap<String,Object> outer = new LinkedHashMap<>(); 
	    LinkedHashMap<String,Object> inner = new LinkedHashMap<>(); 
	    inner.put("keyForList1", Arrays.asList(new String[] {"1","2","3"})); 
	    outer.put("inner1", inner); 
	     
	    inner = new LinkedHashMap<>(); 
	    inner.put("keyForList2", Arrays.asList(new Integer[] {1,2,3})); 
	    outer.put("inner2", inner); 
	
	    String value = JSON.toString(outer); 
		assertEquals("{\"inner1\":{\"keyForList1\":[\"1\",\"2\",\"3\"]},\"inner2\":{\"keyForList2\":[1,2,3]}}", value);
	     
	    assertParse(outer, value); 
	    assertParse(outer, "{inner1:{keyForList1:[\"1\",\"2\",\"3\"]},inner2:{keyForList2:[1,2,3]}}"); 
		assertParse(outer, "{\"inner1\":{\"keyForList1\":[\"1\",\"2\",\"3\"]},\"inner2\":{\"keyForList2\":[1,2,3]}}");
	} 

	public void testMapWithArbitraryKeys() throws ParseException {
		Map<String, Object> map = new MapBuilder<String, Object>()
			.put("Hello world!", 40)
			.put(" {[\"1234567890\"]} ", 42)
			.toMap();

		String value = JSON.toString(map);
		assertEquals("{\"Hello world!\":40,\" {[\\\"1234567890\\\"]} \":42}", value);
		assertParse(map, value);
	}

	public void testConfigTypes() {
		String json = JSON.toString(JSONConfigValueAnalyzer.INSTANCE,
			object(
				property("s", value("foo")),
				property("i", value(42)),
				property("f", value(13.42f)),
				property("bt", value(true)),
				property("bf", value(false)),
				property("n", none()),
				property("l", list(
					value("foo"),
					value(42),
					value(13.42f),
					value(true),
					value(false),
					none(),
					list(value("foo"), value("bar")),
					object(property("foo", value("bar"))))),
				property("o", object(property("foo", value("bar"))))));
		assertEquals(
			"{\"s\":\"foo\",\"i\":42,\"f\":13.42,\"bt\":true,\"bf\":false,\"n\":null,\"l\":[\"foo\",42,13.42,true,false,null,[\"foo\",\"bar\"],{\"foo\":\"bar\"}],\"o\":{\"foo\":\"bar\"}}",
			json);

		System.out.println(json);
	}

	private JSONValue none() {
		JSONNull result = TypedConfiguration.newConfigItem(JSONNull.class);
		return result;
	}

	private JSONValue value(String string) {
		JSONString result = TypedConfiguration.newConfigItem(JSONString.class);
		result.setValue(string);
		return result;
	}

	private JSONValue value(int value) {
		JSONInteger result = TypedConfiguration.newConfigItem(JSONInteger.class);
		result.setValue(value);
		return result;
	}

	private JSONValue value(float value) {
		JSONFloat result = TypedConfiguration.newConfigItem(JSONFloat.class);
		result.setValue(value);
		return result;
	}

	private JSONValue value(boolean value) {
		JSONBoolean result = TypedConfiguration.newConfigItem(JSONBoolean.class);
		result.setValue(value);
		return result;
	}

	private JSONValue list(JSONValue... values) {
		JSONList result = TypedConfiguration.newConfigItem(JSONList.class);
		List<JSONValue> content = result.getContent();

		List<JSONValue> list = Arrays.asList(values);
		content.addAll(list);

		return result;
	}

	private JSONProperty property(String key, JSONValue value) {
		JSONProperty result = TypedConfiguration.newConfigItem(JSONProperty.class);
		result.setKey(key);
		result.setValue(value);
		return result;
	}

	private JSONObject object(JSONProperty... properties) {
		JSONObject result = TypedConfiguration.newConfigItem(JSONObject.class);
		for (JSONProperty property : properties) {
			result.getProperties().put(property.getKey(), property);
		}
		return result;
	}

	private void assertParse(Object expectedValue, String jsonSource) throws ParseException {
		assertEquals(expectedValue, JSON.fromString(jsonSource));
		assertEquals(expectedValue, JSON.read(new StringReader(jsonSource)));
	}

	private void assertParseListContents(Object expectedValue, String jsonSource) throws ParseException {
		assertEquals(expectedValue, JSON.fromStringListContents(jsonSource));
		assertEquals(expectedValue, JSON.readListContents(new StringReader(jsonSource)));
	}
	
	private void assertParseMapContents(Object expectedValue, String jsonSource) throws ParseException {
		assertEquals(expectedValue, JSON.fromStringMapContents(jsonSource));
		assertEquals(expectedValue, JSON.readMapContents(new StringReader(jsonSource)));
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestJSON.class));
	}
	
}
