/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests for {@link AnnotatedTools} schema generation and argument coercion with complex types.
 */
public class AnnotatedToolsTest extends TestCase {

    public record Item(String name, int qty) {}
    public record Order(String customer, List<Item> items) {}

    public static class TestTools {
        @ToolMethod(description = "Submit an order")
        public String submit(@P("the order") Order order) {
            return "ok:" + order.customer() + ":" + order.items().size();
        }
    }

    /** */
    public void testRecordSchemaIsNestedObject() {
        List<Tool> tools = AnnotatedTools.from(new TestTools());
        assertEquals(1, tools.size());
        String schema = tools.get(0).schema().inputSchemaJson();
        // Order is a record → should be an object with properties, not a String fallback.
        assertTrue("Schema should contain 'type:object' for order: " + schema,
            schema.contains("\"type\":\"object\""));
        assertTrue("Schema should describe customer and items: " + schema,
            schema.contains("\"customer\"") && schema.contains("\"items\""));
        // Nested record (Item) inside the array should also be an object.
        assertTrue("Schema should describe nested Item fields: " + schema,
            schema.contains("\"qty\""));
    }

    /** */
    public void testCoercionAcceptsMap() {
        Map<String, Object> orderArg = Map.of(
            "customer", "Alice",
            "items", List.of(Map.of("name", "Book", "qty", 2)));
        Tool tool = AnnotatedTools.from(new TestTools()).get(0);
        ToolResult result = tool.execute(Map.of("order", orderArg), null);
        assertTrue(result.text().startsWith("ok:Alice:1"));
    }

    /** */
    public void testCoercionAcceptsStringifiedJson() {
        // Some LLMs stringify nested args. We must handle that.
        String stringifiedOrder = "{\"customer\":\"Bob\",\"items\":[{\"name\":\"Pen\",\"qty\":5}]}";
        Tool tool = AnnotatedTools.from(new TestTools()).get(0);
        ToolResult result = tool.execute(Map.of("order", stringifiedOrder), null);
        assertTrue("Expected ok:Bob:1, got " + result.text(),
            result.text().startsWith("ok:Bob:1"));
    }

    /** Proves each element in a {@code List<Record>} is coerced, not left as a raw Map. */
    public void testListRecordCoercionWithMultipleElements() {
        Map<String, Object> orderArg = Map.of(
            "customer", "Carol",
            "items", List.of(
                Map.of("name", "Pen", "qty", 3),
                Map.of("name", "Notebook", "qty", 7),
                Map.of("name", "Eraser", "qty", 1)));
        Tool tool = AnnotatedTools.from(new TestTools()).get(0);
        ToolResult result = tool.execute(Map.of("order", orderArg), null);
        assertEquals("ok:Carol:3", result.text());
    }
}
