/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * Tests for {@link JsonTree} and {@link ToolCatalog#parseJson(String)} which delegates to it.
 *
 * <p>
 * Both parsers sit on the LLM-argument execution path; this pins number coercion, null handling
 * and nesting shared by the two former copies.
 * </p>
 */
public class TestJsonTree extends TestCase {

	/** Integral values in int range coerce to Integer, out of range to Long, others to Double. */
	public void testNumberCoercion() throws IOException {
		Map<String, Object> map = JsonTree.parseObject(
			"{\"i\":5,\"big\":5000000000,\"d\":1.5,\"neg\":-3}");
		assertEquals(Integer.valueOf(5), map.get("i"));
		assertEquals(Long.valueOf(5000000000L), map.get("big"));
		assertEquals(Double.valueOf(1.5), map.get("d"));
		assertEquals(Integer.valueOf(-3), map.get("neg"));
	}

	/** Booleans, strings and explicit null are preserved. */
	public void testScalars() throws IOException {
		Map<String, Object> map = JsonTree.parseObject(
			"{\"b\":true,\"s\":\"hi\",\"n\":null}");
		assertEquals(Boolean.TRUE, map.get("b"));
		assertEquals("hi", map.get("s"));
		assertTrue(map.containsKey("n"));
		assertNull(map.get("n"));
	}

	/** Nested objects and arrays produce Map/List trees. */
	@SuppressWarnings("unchecked")
	public void testNesting() throws IOException {
		Map<String, Object> map = JsonTree.parseObject(
			"{\"outer\":{\"list\":[1,2,{\"k\":\"v\"}]}}");
		Map<String, Object> outer = (Map<String, Object>) map.get("outer");
		List<Object> list = (List<Object>) outer.get("list");
		assertEquals(3, list.size());
		assertEquals(Integer.valueOf(1), list.get(0));
		assertEquals("v", ((Map<String, Object>) list.get(2)).get("k"));
	}

	/** parseValue accepts a non-object top-level value. */
	public void testParseValueScalarAndArray() throws IOException {
		assertEquals("bare", JsonTree.parseValue("\"bare\""));
		assertEquals(List.of(Integer.valueOf(1), Integer.valueOf(2)),
			JsonTree.parseValue("[1,2]"));
	}

	/** ToolCatalog.parseJson keeps its null/blank/empty-object fast path. */
	public void testParseJsonBlankGuard() throws IOException {
		assertEquals(Map.of(), ToolCatalog.parseJson(null));
		assertEquals(Map.of(), ToolCatalog.parseJson("   "));
		assertEquals(Map.of(), ToolCatalog.parseJson("{}"));
		assertEquals(Map.of(), ToolCatalog.parseJson(" {} "));
	}

	/** ToolCatalog.parseJson parses a real object identically to JsonTree.parseObject. */
	public void testParseJsonDelegates() throws IOException {
		String json = "{\"a\":1,\"b\":[true,\"x\"]}";
		assertEquals(JsonTree.parseObject(json), ToolCatalog.parseJson(json));
	}
}
