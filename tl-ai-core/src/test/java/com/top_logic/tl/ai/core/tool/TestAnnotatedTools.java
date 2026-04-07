/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.ToolContext;

/**
 * Tests for {@link AnnotatedTools}.
 */
public class TestAnnotatedTools extends TestCase {

	enum Color { RED, GREEN, BLUE }

	/** Test provider with multiple tool methods and various parameter types. */
	static class TestTools {

		@ToolMethod(description = "Add two numbers", mode = ToolMode.READ, concurrencySafe = true)
		public String add(@P("first number") int a, @P("second number") int b) {
			return String.valueOf(a + b);
		}

		@ToolMethod(description = "Greet someone", name = "greet_user", mode = ToolMode.READ)
		public String greet(@P("name to greet") String name) {
			return "Hello, " + name + "!";
		}

		@ToolMethod(description = "Pick a color", mode = ToolMode.READ)
		public String pickColor(@P("the color") Color color) {
			return "You picked " + color.name();
		}

		@ToolMethod(description = "Search with optional flag", mode = ToolMode.READ)
		public String search(
				@P("query text") String query,
				@P(value = "include archived", required = false) Boolean includeArchived) {
			boolean archived = includeArchived != null && includeArchived;
			return "Searching: " + query + " (archived=" + archived + ")";
		}

		@ToolMethod(description = "Delete everything")
		public String deleteAll() {
			return "Deleted";
		}
	}

	/** Scans a class with multiple @ToolMethod methods. */
	public void testScanFindsAllMethods() {
		List<Tool> tools = AnnotatedTools.from(new TestTools());
		assertEquals(5, tools.size());
	}

	/** Method name is used as tool name by default. */
	public void testDefaultToolName() {
		Tool tool = findTool("add");
		assertNotNull(tool);
		assertEquals("add", tool.name());
	}

	/** @ToolMethod.name overrides the method name. */
	public void testCustomToolName() {
		Tool tool = findTool("greet_user");
		assertNotNull(tool);
	}

	/** Description comes from @ToolMethod.value. */
	public void testDescription() {
		Tool tool = findTool("add");
		assertEquals("Add two numbers", tool.description());
	}

	/** ToolMode is read from the annotation. */
	public void testMode() {
		assertEquals(ToolMode.READ, findTool("add").mode());
		assertEquals(ToolMode.GLOBAL, findTool("deleteAll").mode());
	}

	/** isConcurrencySafe from annotation. */
	public void testConcurrencySafe() {
		assertTrue(findTool("add").isConcurrencySafe());
		// deleteAll has default concurrencySafe=false
		assertFalse(findTool("deleteAll").isConcurrencySafe());
	}

	/** Schema for int parameters produces JSON Schema type "integer". */
	public void testIntSchema() {
		String schema = findTool("add").schema().inputSchemaJson();
		assertTrue("Schema should contain integer type: " + schema,
			schema.contains("\"type\":\"integer\""));
		assertTrue("Schema should list 'a' as required: " + schema,
			schema.contains("\"a\""));
		assertTrue("Schema should list 'b' as required: " + schema,
			schema.contains("\"b\""));
	}

	/** Schema for String parameters produces JSON Schema type "string". */
	public void testStringSchema() {
		String schema = findTool("greet_user").schema().inputSchemaJson();
		assertTrue("Schema should contain string type: " + schema,
			schema.contains("\"type\":\"string\""));
	}

	/** Schema for enum parameters produces JSON Schema with enum values. */
	public void testEnumSchema() {
		String schema = findTool("pickColor").schema().inputSchemaJson();
		assertTrue("Schema should contain enum values: " + schema,
			schema.contains("\"RED\""));
		assertTrue(schema.contains("\"GREEN\""));
		assertTrue(schema.contains("\"BLUE\""));
	}

	/** Optional parameters (required=false) are not in the required list. */
	public void testOptionalParameter() {
		String schema = findTool("search").schema().inputSchemaJson();
		// "query" should be required, "includeArchived" should not
		assertTrue(schema.contains("\"query\""));
		// The required array should contain "query" but not "includeArchived"
		assertTrue("required should contain query: " + schema,
			schema.contains("\"required\""));
		// Extract the required array portion and verify includeArchived is not in it
		String requiredPart = schema.substring(schema.indexOf("\"required\""));
		assertFalse("required should not contain includeArchived: " + requiredPart,
			requiredPart.contains("\"includeArchived\""));
	}

	/** No-arg tool produces empty properties schema. */
	public void testNoArgTool() {
		String schema = findTool("deleteAll").schema().inputSchemaJson();
		assertTrue(schema.contains("\"type\":\"object\""));
	}

	/** Tool execution with int parameters. */
	public void testExecuteAdd() {
		Tool tool = findTool("add");
		String result = tool.execute(Map.of("a", 17, "b", 25), ToolContext.EMPTY);
		assertEquals("42", result);
	}

	/** Tool execution with String parameter. */
	public void testExecuteGreet() {
		Tool tool = findTool("greet_user");
		String result = tool.execute(Map.of("name", "World"), ToolContext.EMPTY);
		assertEquals("Hello, World!", result);
	}

	/** Tool execution with enum parameter. */
	public void testExecuteEnum() {
		Tool tool = findTool("pickColor");
		String result = tool.execute(Map.of("color", "GREEN"), ToolContext.EMPTY);
		assertEquals("You picked GREEN", result);
	}

	/** Tool execution with optional parameter omitted. */
	public void testExecuteOptionalOmitted() {
		Tool tool = findTool("search");
		String result = tool.execute(Map.of("query", "test"), ToolContext.EMPTY);
		assertEquals("Searching: test (archived=false)", result);
	}

	/** Tool execution with optional parameter provided. */
	public void testExecuteOptionalProvided() {
		Tool tool = findTool("search");
		String result = tool.execute(
			Map.of("query", "test", "includeArchived", true), ToolContext.EMPTY);
		assertEquals("Searching: test (archived=true)", result);
	}

	/** Number coercion: JSON numbers are doubles, must coerce to int. */
	public void testNumberCoercionToInt() {
		Tool tool = findTool("add");
		// JSON parsers often produce Double for numbers
		String result = tool.execute(Map.of("a", 17.0, "b", 25.0), ToolContext.EMPTY);
		assertEquals("42", result);
	}

	/** Tool that throws returns the exception message (not the exception itself). */
	public void testExecuteThrowingMethod() {
		Object provider = new Object() {
			@ToolMethod(description = "Fails always", mode = ToolMode.READ)
			public String fail(@P("input") String input) {
				throw new RuntimeException("Boom");
			}
		};
		Tool tool = AnnotatedTools.from(provider).get(0);
		try {
			tool.execute(Map.of("input", "test"), ToolContext.EMPTY);
			fail("Expected RuntimeException");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("Boom"));
		}
	}

	/** void return type produces "Success". */
	public void testVoidReturn() {
		Object provider = new Object() {
			@ToolMethod(description = "Does nothing", mode = ToolMode.READ)
			public void doNothing(@P("input") String input) {
				// no-op
			}
		};
		Tool tool = AnnotatedTools.from(provider).get(0);
		String result = tool.execute(Map.of("input", "test"), ToolContext.EMPTY);
		assertEquals("Success", result);
	}

	// --- Helper ---

	private static List<Tool> _tools;

	@Override
	protected void setUp() {
		if (_tools == null) {
			_tools = AnnotatedTools.from(new TestTools());
		}
	}

	private Tool findTool(String name) {
		return _tools.stream()
			.filter(t -> t.name().equals(name))
			.findFirst()
			.orElse(null);
	}
}
